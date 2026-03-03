package com.github.wadleeduhwan.ghworkflows.toolWindow.components

import com.github.wadleeduhwan.ghworkflows.icons.WorkflowIcons
import javax.swing.Icon

object StatusIcon {

    fun forRun(status: String?, conclusion: String?): Icon {
        if (status == "in_progress" || status == "queued" || status == "waiting") {
            return when (status) {
                "in_progress" -> WorkflowIcons.InProgress
                else -> WorkflowIcons.Queued
            }
        }
        return when (conclusion) {
            "success" -> WorkflowIcons.Success
            "failure", "timed_out", "action_required" -> WorkflowIcons.Failure
            "cancelled" -> WorkflowIcons.Cancelled
            "skipped" -> WorkflowIcons.Skipped
            else -> WorkflowIcons.Queued
        }
    }
}
