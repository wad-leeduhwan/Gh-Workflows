package com.github.wadleeduhwan.ghworkflows.services

import com.github.wadleeduhwan.ghworkflows.api.GitHubApiClient
import com.github.wadleeduhwan.ghworkflows.api.models.Job
import com.github.wadleeduhwan.ghworkflows.api.models.Workflow
import com.github.wadleeduhwan.ghworkflows.api.models.WorkflowRun
import com.github.wadleeduhwan.ghworkflows.git.GitHubRepo
import com.github.wadleeduhwan.ghworkflows.git.GitRepositoryHelper
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@Service(Service.Level.PROJECT)
class GitHubWorkflowService(private val project: Project) : Disposable {

    private val apiClient = GitHubApiClient()
    private val listeners = CopyOnWriteArrayList<WorkflowDataListener>()
    private val scheduler: ScheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor { r ->
            Thread(r, "gh-workflows-auto-refresh").apply { isDaemon = true }
        }

    @Volatile
    private var scheduledTask: ScheduledFuture<*>? = null

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
    var workflowRunJobs: Map<Long, List<Job>> = emptyMap()
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

                // Load jobs for failed runs
                val jobsMap = mutableMapOf<Long, List<Job>>()
                for (runs in runsMap.values) {
                    for (run in runs) {
                        if (run.conclusion == "failure") {
                            apiClient.listWorkflowRunJobs(repo.owner, repo.name, run.id)
                                .onSuccess { jobs -> jobsMap[run.id] = jobs }
                                .onFailure { e ->
                                    thisLogger().warn("Failed to load jobs for run #${run.runNumber}", e)
                                }
                        }
                    }
                }
                workflowRunJobs = jobsMap

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

    fun rerunWorkflowRun(runId: Long): Result<Unit> {
        val repo = getRepo() ?: return Result.failure(IllegalStateException("No GitHub repository"))
        return apiClient.rerunWorkflowRun(repo.owner, repo.name, runId)
    }

    fun rerunFailedJobs(runId: Long): Result<Unit> {
        val repo = getRepo() ?: return Result.failure(IllegalStateException("No GitHub repository"))
        return apiClient.rerunFailedJobs(repo.owner, repo.name, runId)
    }

    fun cancelWorkflowRun(runId: Long): Result<Unit> {
        val repo = getRepo() ?: return Result.failure(IllegalStateException("No GitHub repository"))
        return apiClient.cancelWorkflowRun(repo.owner, repo.name, runId)
    }

    fun deleteWorkflowRun(runId: Long): Result<Unit> {
        val repo = getRepo() ?: return Result.failure(IllegalStateException("No GitHub repository"))
        return apiClient.deleteWorkflowRun(repo.owner, repo.name, runId)
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

    fun startAutoRefresh() {
        stopAutoRefresh()
        val settings = WorkflowSettingsState.getInstance(project)
        if (!settings.autoRefreshEnabled) return

        val intervalMinutes = settings.autoRefreshIntervalMinutes.toLong().coerceAtLeast(1)
        scheduledTask = scheduler.scheduleWithFixedDelay(
            { refreshAll() },
            intervalMinutes,
            intervalMinutes,
            TimeUnit.MINUTES,
        )
    }

    fun stopAutoRefresh() {
        scheduledTask?.cancel(false)
        scheduledTask = null
    }

    override fun dispose() {
        stopAutoRefresh()
        scheduler.shutdownNow()
        listeners.clear()
    }

    fun interface WorkflowDataListener {
        fun onDataChanged()
    }
}
