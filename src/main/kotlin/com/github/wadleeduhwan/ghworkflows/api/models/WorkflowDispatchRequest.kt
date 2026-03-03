package com.github.wadleeduhwan.ghworkflows.api.models

import kotlinx.serialization.Serializable

@Serializable
data class WorkflowDispatchRequest(
    val ref: String,
    val inputs: Map<String, String> = emptyMap(),
)
