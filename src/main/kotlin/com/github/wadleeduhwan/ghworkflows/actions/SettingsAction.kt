package com.github.wadleeduhwan.ghworkflows.actions

import com.github.wadleeduhwan.ghworkflows.WorkflowBundle
import com.github.wadleeduhwan.ghworkflows.auth.GitHubTokenManager
import com.github.wadleeduhwan.ghworkflows.git.GitHubRepo
import com.github.wadleeduhwan.ghworkflows.services.GitHubWorkflowService
import com.github.wadleeduhwan.ghworkflows.services.WorkflowSettingsState
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

class SettingsAction : AnAction(
    WorkflowBundle.message("action.settings"),
    WorkflowBundle.message("action.settings.description"),
    AllIcons.General.Settings,
) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val service = project.getService(GitHubWorkflowService::class.java)

        val dialog = SettingsDialog(project, service)
        if (dialog.showAndGet()) {
            val token = dialog.getManualToken()
            if (token.isBlank()) {
                GitHubTokenManager.clearManualToken()
            } else {
                GitHubTokenManager.setManualToken(token)
            }

            val repoText = dialog.getRepoOverride()
            service.overrideRepo = parseOwnerRepo(repoText)

            // Save auto-refresh settings
            val settings = WorkflowSettingsState.getInstance(project)
            settings.autoRefreshEnabled = dialog.getAutoRefreshEnabled()
            settings.autoRefreshIntervalMinutes = dialog.getAutoRefreshInterval()
            service.startAutoRefresh()
        }
    }

    private fun parseOwnerRepo(text: String): GitHubRepo? {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return null
        val parts = trimmed.split("/")
        if (parts.size != 2 || parts[0].isBlank() || parts[1].isBlank()) return null
        return GitHubRepo(parts[0], parts[1])
    }
}

private class SettingsDialog(
    project: Project,
    private val service: GitHubWorkflowService,
) : DialogWrapper(project) {

    private val manualTokenField = JBTextField(20).apply {
        text = GitHubTokenManager.getManualToken() ?: ""
    }

    private val repoField = JBTextField(20).apply {
        text = service.overrideRepo?.fullName ?: ""
    }

    private val settings = WorkflowSettingsState.getInstance(project)

    private val autoRefreshCheckBox = JCheckBox(
        WorkflowBundle.message("dialog.autoRefresh.label"),
    ).apply {
        isSelected = settings.autoRefreshEnabled
    }

    private val intervalSpinner = JSpinner(
        SpinnerNumberModel(settings.autoRefreshIntervalMinutes, 1, 120, 1),
    ).apply {
        isEnabled = autoRefreshCheckBox.isSelected
    }

    init {
        title = WorkflowBundle.message("dialog.settings.title")
        autoRefreshCheckBox.addActionListener {
            intervalSpinner.isEnabled = autoRefreshCheckBox.isSelected
        }
        init()
    }

    override fun createCenterPanel(): JComponent {
        val builder = FormBuilder.createFormBuilder()

        // IntelliJ GitHub 계정 상태 표시
        val accountName = GitHubTokenManager.getIntelliJAccountName()
        if (accountName != null) {
            builder.addComponent(JBLabel(WorkflowBundle.message("dialog.intellij.account", accountName)).apply {
                icon = AllIcons.General.InspectionsOK
            })
            builder.addTooltip(WorkflowBundle.message("dialog.intellij.account.hint"))
        } else {
            builder.addComponent(JBLabel(WorkflowBundle.message("dialog.intellij.account.none")).apply {
                foreground = JBColor.ORANGE
                icon = AllIcons.General.Warning
            })
            builder.addTooltip(WorkflowBundle.message("dialog.intellij.account.setup"))
        }

        builder.addSeparator()

        // 수동 PAT 입력 (fallback)
        builder.addLabeledComponent(WorkflowBundle.message("dialog.token.label"), manualTokenField)
        builder.addTooltip(WorkflowBundle.message("dialog.token.fallback.hint"))

        builder.addSeparator()

        // Repository override
        val detected = service.getDetectedRepo()
        val detectedText = if (detected != null) {
            WorkflowBundle.message("dialog.repo.detected", detected.fullName)
        } else {
            WorkflowBundle.message("status.noRepository")
        }

        builder.addLabeledComponent(WorkflowBundle.message("dialog.repo.label"), repoField)
        builder.addComponent(JBLabel(detectedText).apply {
            foreground = JBColor.GRAY
        })
        builder.addTooltip(WorkflowBundle.message("dialog.repo.hint"))

        builder.addSeparator()

        // Auto-refresh settings
        builder.addComponent(autoRefreshCheckBox)
        builder.addLabeledComponent(
            WorkflowBundle.message("dialog.autoRefresh.interval.label"),
            intervalSpinner,
        )
        builder.addTooltip(WorkflowBundle.message("dialog.autoRefresh.interval.hint"))

        return builder.panel
    }

    fun getManualToken(): String = manualTokenField.text.trim()

    fun getRepoOverride(): String = repoField.text.trim()

    fun getAutoRefreshEnabled(): Boolean = autoRefreshCheckBox.isSelected

    fun getAutoRefreshInterval(): Int = intervalSpinner.value as Int
}
