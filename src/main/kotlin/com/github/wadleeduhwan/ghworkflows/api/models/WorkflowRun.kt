package com.github.wadleeduhwan.ghworkflows.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WorkflowRunsResponse(
    @SerialName("total_count") val totalCount: Int,
    @SerialName("workflow_runs") val workflowRuns: List<WorkflowRun>,
)

@Serializable
data class WorkflowRun(
    val id: Long,
    val name: String? = null,
    @SerialName("run_number") val runNumber: Int,
    @SerialName("display_title") val displayTitle: String = "",
    val status: String? = null,
    val conclusion: String? = null,
    @SerialName("html_url") val htmlUrl: String,
    @SerialName("workflow_id") val workflowId: Long,
    @SerialName("head_branch") val headBranch: String? = null,
    @SerialName("head_sha") val headSha: String = "",
    val event: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("run_started_at") val runStartedAt: String? = null,
    val actor: Actor? = null,
)

@Serializable
data class Actor(
    val login: String,
    @SerialName("avatar_url") val avatarUrl: String = "",
)
