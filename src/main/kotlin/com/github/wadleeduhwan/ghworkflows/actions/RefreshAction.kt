package com.github.wadleeduhwan.ghworkflows.actions

import com.github.wadleeduhwan.ghworkflows.WorkflowBundle
import com.github.wadleeduhwan.ghworkflows.toolWindow.WorkflowPanel
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class RefreshAction(
    private val panel: WorkflowPanel,
) : AnAction(
    WorkflowBundle.message("action.refresh"),
    WorkflowBundle.message("action.refresh.description"),
    AllIcons.Actions.Refresh,
) {
    override fun actionPerformed(e: AnActionEvent) {
        panel.refresh()
    }
}
