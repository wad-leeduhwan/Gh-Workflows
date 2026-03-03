package com.github.wadleeduhwan.ghworkflows.toolWindow.components

import com.github.wadleeduhwan.ghworkflows.api.models.Workflow
import com.github.wadleeduhwan.ghworkflows.api.models.WorkflowRun
import com.github.wadleeduhwan.ghworkflows.icons.WorkflowIcons
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

class WorkflowTreeCellRenderer : ColoredTreeCellRenderer() {

    override fun customizeCellRenderer(
        tree: JTree,
        value: Any?,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean,
    ) {
        val node = value as? DefaultMutableTreeNode ?: return
        when (val userObj = node.userObject) {
            is WorkflowTreeNode.WorkflowNode -> {
                icon = WorkflowIcons.ToolWindow
                append(userObj.workflow.name)
                append("  ${userObj.workflow.path.substringAfterLast("/")}", SimpleTextAttributes.GRAYED_ATTRIBUTES)
            }
            is WorkflowTreeNode.RunNode -> {
                val run = userObj.run
                icon = StatusIcon.forRun(run.status, run.conclusion)
                append("#${run.runNumber} ", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
                append(run.displayTitle.ifBlank { run.name ?: "" })
                val timeAgo = formatTimeAgo(run.createdAt)
                if (timeAgo.isNotEmpty()) {
                    append("  $timeAgo", SimpleTextAttributes.GRAYED_ATTRIBUTES)
                }
                run.headBranch?.let {
                    append("  ($it)", SimpleTextAttributes.GRAYED_ITALIC_ATTRIBUTES)
                }
            }
            is String -> {
                append(userObj, SimpleTextAttributes.GRAYED_ITALIC_ATTRIBUTES)
            }
        }
    }

    private fun formatTimeAgo(dateStr: String): String {
        if (dateStr.isBlank()) return ""
        return try {
            val date = ZonedDateTime.parse(dateStr)
            val now = ZonedDateTime.now()
            val duration = Duration.between(date, now)
            when {
                duration.toDays() > 30 -> "${duration.toDays() / 30}mo ago"
                duration.toDays() > 0 -> "${duration.toDays()}d ago"
                duration.toHours() > 0 -> "${duration.toHours()}h ago"
                duration.toMinutes() > 0 -> "${duration.toMinutes()}m ago"
                else -> "just now"
            }
        } catch (_: DateTimeParseException) {
            ""
        }
    }
}

sealed class WorkflowTreeNode {
    data class WorkflowNode(val workflow: Workflow) : WorkflowTreeNode() {
        override fun toString() = workflow.name
    }

    data class RunNode(val run: WorkflowRun) : WorkflowTreeNode() {
        override fun toString() = "#${run.runNumber} ${run.displayTitle}"
    }
}
