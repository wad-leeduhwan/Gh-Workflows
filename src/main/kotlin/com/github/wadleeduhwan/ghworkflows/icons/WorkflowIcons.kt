package com.github.wadleeduhwan.ghworkflows.icons

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object WorkflowIcons {
    @JvmField val ToolWindow: Icon = IconLoader.getIcon("/icons/github-actions.svg", WorkflowIcons::class.java)
    @JvmField val Success: Icon = IconLoader.getIcon("/icons/success.svg", WorkflowIcons::class.java)
    @JvmField val Failure: Icon = IconLoader.getIcon("/icons/failure.svg", WorkflowIcons::class.java)
    @JvmField val InProgress: Icon = IconLoader.getIcon("/icons/in-progress.svg", WorkflowIcons::class.java)
    @JvmField val Queued: Icon = IconLoader.getIcon("/icons/queued.svg", WorkflowIcons::class.java)
    @JvmField val Cancelled: Icon = IconLoader.getIcon("/icons/cancelled.svg", WorkflowIcons::class.java)
    @JvmField val Skipped: Icon = IconLoader.getIcon("/icons/skipped.svg", WorkflowIcons::class.java)
}
