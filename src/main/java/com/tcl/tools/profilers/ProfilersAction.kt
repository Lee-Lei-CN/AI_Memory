package com.tcl.tools.profilers

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys.PROJECT
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.tcl.tools.idea.profilers.AndroidProfilerService
import com.tcl.tools.idea.profilers.AndroidProfilerToolWindow
import com.tcl.tools.idea.profilers.ToolWindowWrapper
import com.tcl.tools.idea.profilers.ToolWindowWrapperImpl
import icons.StudioIcons
import kotlin.collections.set

class ProfilersAction : AnAction() {
    companion object {
        private const val TCL_AI_PROFILERS_ID = "TCL_AI_Profilers"
    }

    override fun actionPerformed(actionEvent: AnActionEvent) {
        val project = actionEvent.getData(PROJECT)
        println("Project $project ")
        if (project == null) {
            return
        }
        AndroidProfilerToolWindowFactory.openProfilerWindow(project)
    }


    class AndroidProfilerToolWindowFactory : DumbAware,
        ToolWindowFactory {
        override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
            val wrapper: ToolWindowWrapper = ToolWindowWrapperImpl(project, toolWindow)
            val view = AndroidProfilerToolWindow(wrapper, project)
            val contentFactory = ContentFactory.SERVICE.getInstance()
            val content = contentFactory.createContent(view.component, "", false)
            Disposer.register(project, view)
            toolWindow.contentManager.addContent(content)
            toolWindow.setIcon(StudioIcons.Shell.ToolWindows.ANDROID_PROFILER)
            PROJECT_PROFILER_MAP[content] = view
            Disposer.register(content) {
                PROJECT_PROFILER_MAP.remove(
                    content
                )
            }

            // Forcibly synchronize the Tool Window to a visible state. Otherwise, the Tool Window may not auto-hide correctly.
            toolWindow.show(null)
        }

        override fun init(toolWindow: ToolWindow) {
            toolWindow.stripeTitle = PROFILER_TOOL_WINDOW_TITLE
            toolWindow.isShowStripeButton = true
            AndroidProfilerService.getInstance()
        }

        fun removeContent(toolWindow: ToolWindow) {
            if (toolWindow.contentManager.contentCount > 0) {
                val content = toolWindow.contentManager.getContent(0)
                PROJECT_PROFILER_MAP.remove(content)
                toolWindow.contentManager.removeAllContents(true)
            }
        }

        companion object {
            const val ID = "Android Profiler"
            private const val PROFILER_TOOL_WINDOW_TITLE = "Profiler"
            private val PROJECT_PROFILER_MAP: MutableMap<Content?, AndroidProfilerToolWindow> = java.util.HashMap()

            fun openProfilerWindow(project: Project) {
                val toolWindowManager = ToolWindowManager.getInstance(project)
                var toolWindow = toolWindowManager.getToolWindow(TCL_AI_PROFILERS_ID)
                if (toolWindow == null) {
                    toolWindow = toolWindowManager.registerToolWindow(
                        TCL_AI_PROFILERS_ID,
                        false,
                        ToolWindowAnchor.BOTTOM,
                        project as Disposable,
                        true
                    )
                    val tclProfilersToolWindow = AndroidProfilerToolWindowFactory()
                    tclProfilersToolWindow.createToolWindowContent(project, toolWindow)
                }
            }
        }
    }
}