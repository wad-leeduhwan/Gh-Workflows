package com.github.wadleeduhwan.ghworkflows.api

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import org.yaml.snakeyaml.Yaml
import java.io.File

data class WorkflowInput(
    val name: String,
    val description: String = "",
    val required: Boolean = false,
    val default: String = "",
    val type: String = "string",
    val options: List<String> = emptyList(),
)

object WorkflowInputParser {

    private val LOG = logger<WorkflowInputParser>()

    /**
     * 프로젝트의 워크플로우 파일에서 workflow_dispatch inputs를 파싱한다.
     * @param workflowPath 예: ".github/workflows/deploy.yml"
     */
    fun parseInputs(project: Project, workflowPath: String): List<WorkflowInput> {
        val basePath = project.basePath ?: return emptyList()
        val file = File(basePath, workflowPath)
        if (!file.exists()) return emptyList()

        return try {
            val yaml = Yaml()
            val doc = yaml.load(file.readText()) as? Map<*, *> ?: return emptyList()
            extractInputs(doc)
        } catch (e: Exception) {
            LOG.warn("Failed to parse workflow file: $workflowPath", e)
            emptyList()
        }
    }

    /**
     * workflow_dispatch 트리거가 있는지 확인
     */
    fun hasWorkflowDispatch(project: Project, workflowPath: String): Boolean {
        val basePath = project.basePath ?: return false
        val file = File(basePath, workflowPath)
        if (!file.exists()) return false

        return try {
            val yaml = Yaml()
            val doc = yaml.load(file.readText()) as? Map<*, *> ?: return false
            val on = doc["on"] ?: doc[true] ?: return false
            when (on) {
                is Map<*, *> -> on.containsKey("workflow_dispatch")
                is List<*> -> on.contains("workflow_dispatch")
                is String -> on == "workflow_dispatch"
                else -> false
            }
        } catch (e: Exception) {
            LOG.warn("Failed to check workflow_dispatch: $workflowPath", e)
            false
        }
    }

    private fun extractInputs(doc: Map<*, *>): List<WorkflowInput> {
        val on = doc["on"] ?: doc[true] ?: return emptyList()
        if (on !is Map<*, *>) return emptyList()

        val workflowDispatch = on["workflow_dispatch"] ?: return emptyList()
        if (workflowDispatch !is Map<*, *>) return emptyList()

        val inputs = workflowDispatch["inputs"] ?: return emptyList()
        if (inputs !is Map<*, *>) return emptyList()

        return inputs.mapNotNull { (key, value) ->
            val name = key?.toString() ?: return@mapNotNull null
            if (value !is Map<*, *>) {
                return@mapNotNull WorkflowInput(name = name)
            }

            val options = (value["options"] as? List<*>)?.map { it.toString() } ?: emptyList()

            WorkflowInput(
                name = name,
                description = value["description"]?.toString() ?: "",
                required = value["required"]?.toString()?.toBooleanStrictOrNull() ?: false,
                default = value["default"]?.toString() ?: "",
                type = value["type"]?.toString() ?: "string",
                options = options,
            )
        }
    }
}
