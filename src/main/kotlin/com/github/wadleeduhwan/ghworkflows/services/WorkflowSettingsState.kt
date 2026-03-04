package com.github.wadleeduhwan.ghworkflows.services

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
@State(
    name = "GhWorkflowsSettings",
    storages = [Storage("GhWorkflowsSettings.xml")],
)
class WorkflowSettingsState : PersistentStateComponent<WorkflowSettingsState.State> {

    data class State(
        var autoRefreshEnabled: Boolean = true,
        var autoRefreshIntervalMinutes: Int = 10,
    )

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    var autoRefreshEnabled: Boolean
        get() = myState.autoRefreshEnabled
        set(value) { myState.autoRefreshEnabled = value }

    var autoRefreshIntervalMinutes: Int
        get() = myState.autoRefreshIntervalMinutes
        set(value) { myState.autoRefreshIntervalMinutes = value }

    companion object {
        fun getInstance(project: Project): WorkflowSettingsState =
            project.getService(WorkflowSettingsState::class.java)
    }
}
