package com.github.wadleeduhwan.ghworkflows.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JobsResponse(
    @SerialName("total_count") val totalCount: Int,
    val jobs: List<Job>,
)

@Serializable
data class Job(
    val id: Long,
    val name: String,
    val status: String? = null,
    val conclusion: String? = null,
    @SerialName("html_url") val htmlUrl: String,
    @SerialName("run_id") val runId: Long,
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("completed_at") val completedAt: String? = null,
)
