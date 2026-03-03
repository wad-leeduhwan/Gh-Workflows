package com.github.wadleeduhwan.ghworkflows.actions

import com.github.wadleeduhwan.ghworkflows.WorkflowBundle
import com.github.wadleeduhwan.ghworkflows.toolWindow.WorkflowPanel
import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class OpenInBrowserAction(
    private val panel: WorkflowPanel,
) : AnAction(
    WorkflowBundle.message("action.openInBrowser"),
    WorkflowBundle.message("action.openInBrowser.description"),
    AllIcons.General.Web,
) {
    override fun actionPerformed(e: AnActionEvent) {
        val run = panel.getSelectedRun()
        if (run != null) {
            BrowserUtil.browse(run.htmlUrl)
            return
        }
        val workflow = panel.getSelectedWorkflow()
        if (workflow != null) {
            BrowserUtil.browse(workflow.htmlUrl)
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = panel.getSelectedRun() != null || panel.getSelectedWorkflow() != null
    }
}
