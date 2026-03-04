package com.github.wadleeduhwan.ghworkflows.toolWindow

import com.github.wadleeduhwan.ghworkflows.actions.OpenInBrowserAction
import com.github.wadleeduhwan.ghworkflows.actions.RefreshAction
import com.github.wadleeduhwan.ghworkflows.actions.SettingsAction
import com.github.wadleeduhwan.ghworkflows.actions.TriggerWorkflowAction
import com.github.wadleeduhwan.ghworkflows.auth.GitHubTokenManager
import com.github.wadleeduhwan.ghworkflows.services.GitHubWorkflowService
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class WorkflowToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = WorkflowPanel(project)

        // Setup toolbar actions
        val actionGroup = DefaultActionGroup().apply {
            add(RefreshAction(panel))
            add(TriggerWorkflowAction(panel, project))
            addSeparator()
            add(OpenInBrowserAction(panel))
            addSeparator()
            add(SettingsAction())
        }

        val toolbar = ActionManager.getInstance().createActionToolbar(
            "GhWorkflows.Toolbar",
            actionGroup,
            true
        )
        toolbar.targetComponent = panel
        toolWindow.setTitleActions(listOf(RefreshAction(panel)))

        val contentPanel = javax.swing.JPanel(java.awt.BorderLayout()).apply {
            add(toolbar.component, java.awt.BorderLayout.NORTH)
            add(panel, java.awt.BorderLayout.CENTER)
        }

        val content = ContentFactory.getInstance().createContent(contentPanel, null, false)
        toolWindow.contentManager.addContent(content)

        // Auto-refresh if token is configured
        if (GitHubTokenManager.hasToken()) {
            panel.refresh()
        }

        // Start auto-refresh timer based on persisted settings
        project.getService(GitHubWorkflowService::class.java).startAutoRefresh()
    }

    override fun shouldBeAvailable(project: Project) = true
}
