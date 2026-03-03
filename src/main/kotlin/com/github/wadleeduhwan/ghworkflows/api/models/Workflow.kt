package com.github.wadleeduhwan.ghworkflows.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WorkflowsResponse(
    @SerialName("total_count") val totalCount: Int,
    val workflows: List<Workflow>,
)

@Serializable
data class Workflow(
    val id: Long,
    val name: String,
    val path: String,
    val state: String,
    @SerialName("html_url") val htmlUrl: String,
    @SerialName("badge_url") val badgeUrl: String = "",
)
