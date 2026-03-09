package com.github.wadleeduhwan.ghworkflows.toolWindow.components

import com.github.wadleeduhwan.ghworkflows.api.models.Job
import com.github.wadleeduhwan.ghworkflows.api.models.Workflow
import com.github.wadleeduhwan.ghworkflows.api.models.WorkflowRun
import com.github.wadleeduhwan.ghworkflows.icons.WorkflowIcons
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleColoredComponent
import com.intellij.ui.SimpleTextAttributes
import com.intellij.util.ui.EmptyIcon
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Component
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeCellRenderer

class WorkflowTreeCellRenderer : TreeCellRenderer {

    private val singleLineRenderer = SingleLineCellRenderer()
    private val runRenderer = RunNodeRenderer()

    override fun getTreeCellRendererComponent(
        tree: JTree,
        value: Any?,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean,
    ): Component {
        val node = value as? DefaultMutableTreeNode
        val userObj = node?.userObject

        return if (userObj is WorkflowTreeNode.RunNode) {
            runRenderer.configure(tree, userObj.run, selected, hasFocus)
        } else {
            singleLineRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus)
        }
    }
}

private class SingleLineCellRenderer : ColoredTreeCellRenderer() {

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
            is WorkflowTreeNode.JobNode -> {
                val job = userObj.job
                icon = StatusIcon.forRun(job.status, job.conclusion)
                append(job.name)
                job.conclusion?.let {
                    append("  [$it]", SimpleTextAttributes.GRAYED_ATTRIBUTES)
                }
            }
            is String -> {
                append(userObj, SimpleTextAttributes.GRAYED_ITALIC_ATTRIBUTES)
            }
        }
    }
}

private class RunNodeRenderer : JPanel() {

    private val topLine = SimpleColoredComponent()
    private val bottomLine = SimpleColoredComponent()

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        isOpaque = true
        add(topLine)
        add(bottomLine)
    }

    fun configure(tree: JTree, run: WorkflowRun, selected: Boolean, hasFocus: Boolean): Component {
        topLine.clear()
        bottomLine.clear()

        // First line: status icon + #number + title
        topLine.icon = StatusIcon.forRun(run.status, run.conclusion)
        topLine.append("#${run.runNumber} ", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
        topLine.append(run.displayTitle.ifBlank { run.name ?: "" })

        // Second line: actor · time ago · branch (aligned via empty icon matching status icon width)
        val iconWidth = topLine.icon?.iconWidth ?: 16
        bottomLine.icon = EmptyIcon.create(iconWidth)
        val parts = mutableListOf<String>()
        run.actor?.let { parts.add(it.login) }
        val timeAgo = formatTimeAgo(run.createdAt)
        if (timeAgo.isNotEmpty()) parts.add(timeAgo)
        run.headBranch?.let { parts.add(it) }
        bottomLine.append(parts.joinToString("  ·  "), SimpleTextAttributes.GRAYED_ATTRIBUTES)

        // Selection highlighting
        val bg = if (selected) UIUtil.getTreeSelectionBackground(hasFocus) else tree.background
        background = bg
        topLine.isOpaque = false
        bottomLine.isOpaque = false

        return this
    }

    companion object {
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
}

sealed class WorkflowTreeNode {
    data class WorkflowNode(val workflow: Workflow) : WorkflowTreeNode() {
        override fun toString() = workflow.name
    }

    data class RunNode(val run: WorkflowRun) : WorkflowTreeNode() {
        override fun toString() = "#${run.runNumber} ${run.displayTitle}"
    }

    data class JobNode(val job: Job) : WorkflowTreeNode() {
        override fun toString() = job.name
    }
}
