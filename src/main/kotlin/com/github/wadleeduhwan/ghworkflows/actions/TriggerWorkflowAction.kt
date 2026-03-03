package com.github.wadleeduhwan.ghworkflows.actions

import com.github.wadleeduhwan.ghworkflows.WorkflowBundle
import com.github.wadleeduhwan.ghworkflows.api.WorkflowInput
import com.github.wadleeduhwan.ghworkflows.api.WorkflowInputParser
import com.github.wadleeduhwan.ghworkflows.api.models.Workflow
import com.github.wadleeduhwan.ghworkflows.git.GitRepositoryHelper
import com.github.wadleeduhwan.ghworkflows.services.GitHubWorkflowService
import com.github.wadleeduhwan.ghworkflows.toolWindow.WorkflowPanel
import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.ui.JBColor
import com.intellij.ui.SeparatorComponent
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import java.awt.Dimension
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent

class TriggerWorkflowAction(
    private val panel: WorkflowPanel,
    private val project: Project,
) : AnAction(
    WorkflowBundle.message("action.trigger"),
    WorkflowBundle.message("action.trigger.description"),
    AllIcons.Actions.Execute,
) {
    override fun actionPerformed(e: AnActionEvent) {
        val workflow = panel.getSelectedWorkflow()
        if (workflow == null) {
            Messages.showWarningDialog(
                project,
                WorkflowBundle.message("dialog.trigger.selectFirst"),
                WorkflowBundle.message("dialog.trigger.title"),
            )
            return
        }

        // workflow_dispatch 지원 여부 확인
        val hasDispatch = WorkflowInputParser.hasWorkflowDispatch(project, workflow.path)
        if (!hasDispatch) {
            Messages.showWarningDialog(
                project,
                WorkflowBundle.message("dialog.trigger.noDispatch"),
                WorkflowBundle.message("dialog.trigger.title"),
            )
            return
        }

        val service = project.getService(GitHubWorkflowService::class.java)
        val inputs = WorkflowInputParser.parseInputs(project, workflow.path)
        val branches = GitRepositoryHelper.getBranches(project)
        val tags = GitRepositoryHelper.getTags(project)
        val defaultBranch = service.getDefaultBranch()

        val dialog = TriggerDialog(project, workflow, inputs, branches, tags, defaultBranch)
        if (!dialog.showAndGet()) return

        val ref = dialog.getRef()
        val inputValues = dialog.getInputValues()

        Thread({
            service.triggerWorkflow(workflow.id, ref, inputValues)
                .onSuccess {
                    notify(
                        WorkflowBundle.message("notification.trigger.success"),
                        NotificationType.INFORMATION,
                    )
                    Thread.sleep(2000)
                    panel.refresh()
                }
                .onFailure { ex ->
                    notify(
                        WorkflowBundle.message("notification.trigger.failure", ex.message ?: "Unknown error"),
                        NotificationType.ERROR,
                    )
                }
        }, "gh-workflows-trigger").start()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = panel.getSelectedWorkflow() != null
    }

    private fun notify(content: String, type: NotificationType) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("GhWorkflows.Notification")
            .createNotification(content, type)
            .notify(project)
    }
}

private class TriggerDialog(
    project: Project,
    private val workflow: Workflow,
    private val inputs: List<WorkflowInput>,
    branches: List<String>,
    tags: List<String>,
    defaultBranch: String,
) : DialogWrapper(project) {

    private val refCombo: ComboBox<String>
    private val inputComponents = mutableMapOf<String, InputComponent>()

    init {
        title = WorkflowBundle.message("dialog.trigger.titleWithName", workflow.name)
        setOKButtonText(WorkflowBundle.message("dialog.trigger.runButton"))

        // 브랜치 + 태그 목록 (Branch: xxx, Tag: xxx 로 구분)
        val refItems = mutableListOf<String>()
        refItems.addAll(branches)
        if (tags.isNotEmpty()) {
            for (tag in tags) {
                refItems.add("tag:$tag")
            }
        }
        refCombo = ComboBox(DefaultComboBoxModel(refItems.toTypedArray())).apply {
            preferredSize = Dimension(350, 30)
            isEditable = true
            selectedItem = defaultBranch
        }

        init()
    }

    override fun createCenterPanel(): JComponent {
        val builder = FormBuilder.createFormBuilder()

        // 워크플로우 정보
        builder.addComponent(JBLabel(workflow.path.substringAfterLast("/")).apply {
            icon = AllIcons.Vcs.Branch
            foreground = JBColor.GRAY
        })
        builder.addVerticalGap(8)

        // Use workflow from - 브랜치/태그 선택
        builder.addLabeledComponent(
            WorkflowBundle.message("dialog.trigger.useWorkflowFrom"),
            refCombo,
        )

        // Inputs 섹션
        if (inputs.isNotEmpty()) {
            builder.addVerticalGap(12)
            builder.addComponent(SeparatorComponent())
            builder.addVerticalGap(4)

            for (input in inputs) {
                val label = buildString {
                    append(input.description.ifBlank { input.name })
                    if (input.required) append(" *")
                }

                when (input.type) {
                    "choice" -> {
                        val combo = ComboBox(DefaultComboBoxModel(input.options.toTypedArray())).apply {
                            preferredSize = Dimension(350, 30)
                            if (input.default.isNotBlank() && input.default in input.options) {
                                selectedItem = input.default
                            }
                        }
                        inputComponents[input.name] = InputComponent.Choice(combo)
                        builder.addLabeledComponent(label, combo)
                    }
                    "boolean" -> {
                        val checkbox = JBCheckBox(input.name).apply {
                            isSelected = input.default.equals("true", ignoreCase = true)
                        }
                        inputComponents[input.name] = InputComponent.Bool(checkbox)
                        builder.addLabeledComponent(label, checkbox)
                    }
                    else -> { // string, number, environment
                        val field = JBTextField(input.default).apply {
                            preferredSize = Dimension(350, 30)
                            if (input.description.isNotBlank()) {
                                emptyText.text = input.description
                            }
                        }
                        inputComponents[input.name] = InputComponent.Text(field)
                        builder.addLabeledComponent(label, field)
                    }
                }

                // 타입 힌트
                if (input.type != "string") {
                    builder.addComponent(JBLabel("(${input.type})").apply {
                        foreground = JBColor.GRAY
                        font = font.deriveFont(font.size - 1f)
                        border = JBUI.Borders.emptyLeft(4)
                    })
                }
            }
        }

        return builder.panel.apply {
            preferredSize = Dimension(420, preferredSize.height)
        }
    }

    fun getRef(): String {
        val selected = refCombo.selectedItem?.toString()?.trim() ?: "main"
        // "tag:v1.0" 형식이면 태그 이름만 추출
        return if (selected.startsWith("tag:")) selected.removePrefix("tag:") else selected
    }

    fun getInputValues(): Map<String, String> {
        val result = mutableMapOf<String, String>()
        for ((name, component) in inputComponents) {
            val value = when (component) {
                is InputComponent.Text -> component.field.text.trim()
                is InputComponent.Choice -> component.combo.selectedItem?.toString() ?: ""
                is InputComponent.Bool -> component.checkbox.isSelected.toString()
            }
            if (value.isNotBlank()) {
                result[name] = value
            }
        }
        return result
    }

    private sealed class InputComponent {
        data class Text(val field: JBTextField) : InputComponent()
        data class Choice(val combo: ComboBox<String>) : InputComponent()
        data class Bool(val checkbox: JBCheckBox) : InputComponent()
    }
}
