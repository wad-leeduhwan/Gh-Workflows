package com.github.wadleeduhwan.ghworkflows.api

import com.github.wadleeduhwan.ghworkflows.api.models.*
import com.github.wadleeduhwan.ghworkflows.auth.GitHubTokenManager
import com.intellij.util.io.HttpRequests
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection

class GitHubApiClient {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val baseUrl = "https://api.github.com"

    fun listWorkflows(owner: String, repo: String): Result<List<Workflow>> = runCatching {
        val url = "$baseUrl/repos/$owner/$repo/actions/workflows?per_page=100"
        val response = get(url)
        json.decodeFromString<WorkflowsResponse>(response).workflows
            .filter { it.state == "active" }
    }

    fun listWorkflowRuns(
        owner: String,
        repo: String,
        workflowId: Long,
        perPage: Int = 10,
    ): Result<List<WorkflowRun>> = runCatching {
        val url = "$baseUrl/repos/$owner/$repo/actions/workflows/$workflowId/runs?per_page=$perPage"
        val response = get(url)
        json.decodeFromString<WorkflowRunsResponse>(response).workflowRuns
    }

    fun triggerWorkflowDispatch(
        owner: String,
        repo: String,
        workflowId: Long,
        ref: String,
        inputs: Map<String, String> = emptyMap(),
    ): Result<Unit> = runCatching {
        val url = "$baseUrl/repos/$owner/$repo/actions/workflows/$workflowId/dispatches"
        val body = json.encodeToString(WorkflowDispatchRequest(ref, inputs))
        post(url, body)
    }

    private fun get(url: String): String {
        val token = GitHubTokenManager.getToken()
            ?: throw IllegalStateException("GitHub token not configured")

        return HttpRequests.request(url)
            .tuner { connection ->
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.setRequestProperty("Accept", "application/vnd.github+json")
                connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            }
            .readString()
    }

    private fun post(url: String, body: String) {
        val token = GitHubTokenManager.getToken()
            ?: throw IllegalStateException("GitHub token not configured")

        HttpRequests.post(url, "application/json")
            .tuner { connection ->
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.setRequestProperty("Accept", "application/vnd.github+json")
                connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            }
            .connect { request ->
                request.write(body)
                val connection = request.connection as HttpURLConnection
                val responseCode = connection.responseCode
                if (responseCode !in 200..299) {
                    val errorBody = connection.errorStream?.bufferedReader()?.readText() ?: ""
                    throw RuntimeException("HTTP $responseCode: $errorBody")
                }
            }
    }
}
