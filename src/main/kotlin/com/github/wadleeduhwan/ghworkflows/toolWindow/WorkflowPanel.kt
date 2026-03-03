package com.github.wadleeduhwan.ghworkflows.toolWindow

import com.github.wadleeduhwan.ghworkflows.WorkflowBundle
import com.github.wadleeduhwan.ghworkflows.api.models.Workflow
import com.github.wadleeduhwan.ghworkflows.api.models.WorkflowRun
import com.github.wadleeduhwan.ghworkflows.auth.GitHubTokenManager
import com.github.wadleeduhwan.ghworkflows.services.GitHubWorkflowService
import com.github.wadleeduhwan.ghworkflows.toolWindow.components.WorkflowTreeCellRenderer
import com.github.wadleeduhwan.ghworkflows.toolWindow.components.WorkflowTreeNode
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import java.awt.BorderLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.SwingConstants
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

class WorkflowPanel(
    private val project: Project,
) : JPanel(BorderLayout()), Disposable {

    private val service = project.getService(GitHubWorkflowService::class.java)
    private val rootNode = DefaultMutableTreeNode("Workflows")
    private val treeModel = DefaultTreeModel(rootNode)
    private val tree = Tree(treeModel)
    private val statusLabel = JBLabel("", SwingConstants.CENTER)
    private val repoLabel = JBLabel("", SwingConstants.LEFT)
    private val progressBar = JProgressBar().apply {
        isIndeterminate = true
        isVisible = false
    }

    private val dataListener = GitHubWorkflowService.WorkflowDataListener {
        ApplicationManager.getApplication().invokeLater { updateTree() }
    }

    init {
        tree.cellRenderer = WorkflowTreeCellRenderer()
        tree.isRootVisible = false
        tree.showsRootHandles = true

        tree.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    val path = tree.getPathForLocation(e.x, e.y) ?: return
                    val node = path.lastPathComponent as? DefaultMutableTreeNode ?: return
                    when (val userObj = node.userObject) {
                        is WorkflowTreeNode.RunNode -> BrowserUtil.browse(userObj.run.htmlUrl)
                        is WorkflowTreeNode.WorkflowNode -> BrowserUtil.browse(userObj.workflow.htmlUrl)
                    }
                }
            }
        })

        repoLabel.foreground = java.awt.Color.GRAY
        val bottomPanel = JPanel(BorderLayout()).apply {
            add(repoLabel, BorderLayout.WEST)
            add(statusLabel, BorderLayout.CENTER)
        }

        add(progressBar, BorderLayout.NORTH)
        add(JBScrollPane(tree), BorderLayout.CENTER)
        add(bottomPanel, BorderLayout.SOUTH)

        service.addListener(dataListener)

        if (!GitHubTokenManager.hasToken()) {
            showStatus(WorkflowBundle.message("status.noToken"))
        }
    }

    fun refresh() {
        progressBar.isVisible = true
        Thread({ service.refreshAll() }, "gh-workflows-refresh").start()
    }

    fun getSelectedWorkflow(): Workflow? {
        val path = tree.selectionPath ?: return null
        for (i in path.pathCount - 1 downTo 0) {
            val node = path.getPathComponent(i) as? DefaultMutableTreeNode ?: continue
            val userObj = node.userObject
            if (userObj is WorkflowTreeNode.WorkflowNode) return userObj.workflow
        }
        return null
    }

    fun getSelectedRun(): WorkflowRun? {
        val node = tree.lastSelectedPathComponent as? DefaultMutableTreeNode ?: return null
        return (node.userObject as? WorkflowTreeNode.RunNode)?.run
    }

    private fun updateTree() {
        // 로딩 중: progress bar만 표시, 기존 트리 유지
        if (service.isLoading) {
            progressBar.isVisible = true
            statusLabel.isVisible = false
            return
        }

        progressBar.isVisible = false

        // 에러: 기존 트리 유지, 상태 메시지만 갱신
        service.lastError?.let { error ->
            showStatus(WorkflowBundle.message("status.error", error))
            return
        }

        // 데이터 없음
        if (service.workflows.isEmpty()) {
            rootNode.removeAllChildren()
            treeModel.reload()
            showStatus(WorkflowBundle.message("status.noWorkflows"))
            return
        }

        // 성공: 선택 상태 저장 → 트리 갱신 → 선택 복원
        val selection = saveSelection()

        statusLabel.isVisible = false

        val repo = service.getRepo()
        repoLabel.text = if (repo != null) " ${repo.fullName}" else ""
        repoLabel.isVisible = repo != null

        rootNode.removeAllChildren()

        for (workflow in service.workflows) {
            val workflowNode = DefaultMutableTreeNode(WorkflowTreeNode.WorkflowNode(workflow))
            val runs = service.workflowRuns[workflow.id] ?: emptyList()
            if (runs.isEmpty()) {
                workflowNode.add(DefaultMutableTreeNode(WorkflowBundle.message("status.noRuns")))
            } else {
                for (run in runs) {
                    workflowNode.add(DefaultMutableTreeNode(WorkflowTreeNode.RunNode(run)))
                }
            }
            rootNode.add(workflowNode)
        }

        treeModel.reload()

        for (i in 0 until tree.rowCount) {
            tree.expandRow(i)
        }

        restoreSelection(selection)
    }

    private fun saveSelection(): Pair<Long?, Long?> {
        val path = tree.selectionPath ?: return null to null
        var workflowId: Long? = null
        var runId: Long? = null
        for (i in 0 until path.pathCount) {
            val node = path.getPathComponent(i) as? DefaultMutableTreeNode ?: continue
            when (val obj = node.userObject) {
                is WorkflowTreeNode.WorkflowNode -> workflowId = obj.workflow.id
                is WorkflowTreeNode.RunNode -> runId = obj.run.id
            }
        }
        return workflowId to runId
    }

    private fun restoreSelection(saved: Pair<Long?, Long?>) {
        val (workflowId, runId) = saved
        if (workflowId == null) return

        for (i in 0 until rootNode.childCount) {
            val wfNode = rootNode.getChildAt(i) as? DefaultMutableTreeNode ?: continue
            val wfObj = wfNode.userObject as? WorkflowTreeNode.WorkflowNode ?: continue
            if (wfObj.workflow.id != workflowId) continue

            if (runId != null) {
                for (j in 0 until wfNode.childCount) {
                    val runNode = wfNode.getChildAt(j) as? DefaultMutableTreeNode ?: continue
                    val runObj = runNode.userObject as? WorkflowTreeNode.RunNode ?: continue
                    if (runObj.run.id == runId) {
                        tree.selectionPath = TreePath(runNode.path)
                        return
                    }
                }
            }
            tree.selectionPath = TreePath(wfNode.path)
            return
        }
    }

    private fun showStatus(message: String) {
        statusLabel.text = message
        statusLabel.isVisible = true
    }

    override fun dispose() {
        service.removeListener(dataListener)
    }
}
