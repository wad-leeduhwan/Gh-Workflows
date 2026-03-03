package com.github.wadleeduhwan.ghworkflows.services

import com.github.wadleeduhwan.ghworkflows.api.GitHubApiClient
import com.github.wadleeduhwan.ghworkflows.api.models.Workflow
import com.github.wadleeduhwan.ghworkflows.api.models.WorkflowRun
import com.github.wadleeduhwan.ghworkflows.git.GitHubRepo
import com.github.wadleeduhwan.ghworkflows.git.GitRepositoryHelper
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import java.util.concurrent.CopyOnWriteArrayList

@Service(Service.Level.PROJECT)
class GitHubWorkflowService(private val project: Project) : Disposable {

    private val apiClient = GitHubApiClient()
    private val listeners = CopyOnWriteArrayList<WorkflowDataListener>()

    /** 수동으로 지정한 repo. null이면 IDE Git에서 자동 감지 */
    @Volatile
    var overrideRepo: GitHubRepo? = null

    @Volatile
    var workflows: List<Workflow> = emptyList()
        private set

    @Volatile
    var workflowRuns: Map<Long, List<WorkflowRun>> = emptyMap()
        private set

    @Volatile
    var isLoading: Boolean = false
        private set

    @Volatile
    var lastError: String? = null
        private set

    /** IDE Git remote에서 자동 감지된 repo */
    fun getDetectedRepo(): GitHubRepo? = GitRepositoryHelper.getGitHubRepo(project)

    /** override가 있으면 override, 없으면 자동 감지 */
    fun getRepo(): GitHubRepo? = overrideRepo ?: getDetectedRepo()

    fun getDefaultBranch(): String = GitRepositoryHelper.getDefaultBranch(project)

    fun refreshAll() {
        val repo = getRepo()
        if (repo == null) {
            lastError = "No GitHub repository detected"
            notifyListeners()
            return
        }

        isLoading = true
        lastError = null
        notifyListeners()

        apiClient.listWorkflows(repo.owner, repo.name)
            .onSuccess { wfs ->
                workflows = wfs
                // Load runs for each workflow
                val runsMap = mutableMapOf<Long, List<WorkflowRun>>()
                for (wf in wfs) {
                    apiClient.listWorkflowRuns(repo.owner, repo.name, wf.id)
                        .onSuccess { runs -> runsMap[wf.id] = runs }
                        .onFailure { e ->
                            thisLogger().warn("Failed to load runs for workflow ${wf.name}", e)
                        }
                }
                workflowRuns = runsMap
                isLoading = false
                lastError = null
                notifyListeners()
            }
            .onFailure { e ->
                thisLogger().warn("Failed to load workflows", e)
                isLoading = false
                lastError = e.message
                notifyListeners()
            }
    }

    fun triggerWorkflow(
        workflowId: Long,
        ref: String,
        inputs: Map<String, String> = emptyMap(),
    ): Result<Unit> {
        val repo = getRepo() ?: return Result.failure(IllegalStateException("No GitHub repository"))
        return apiClient.triggerWorkflowDispatch(repo.owner, repo.name, workflowId, ref, inputs)
    }

    fun addListener(listener: WorkflowDataListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: WorkflowDataListener) {
        listeners.remove(listener)
    }

    private fun notifyListeners() {
        for (listener in listeners) {
            listener.onDataChanged()
        }
    }

    override fun dispose() {
        listeners.clear()
    }

    fun interface WorkflowDataListener {
        fun onDataChanged()
    }
}
