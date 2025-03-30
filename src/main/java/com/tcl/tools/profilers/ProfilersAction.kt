package com.tcl.tools.profilers

import com.google.common.util.concurrent.MoreExecutors
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys.PROJECT
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.tcl.tools.ProjectHolder
import com.tcl.tools.idea.profilers.AndroidProfilerService
import com.tcl.tools.idea.profilers.AndroidProfilerToolWindow
import com.tcl.tools.profilers.memory.*
import com.tcl.tools.profilers.memory.adapters.CaptureObject
import com.tcl.tools.profilers.memory.adapters.HeapDumpCaptureObject
import com.tcl.tools.test.FakeCaptureObject
import java.io.File
import java.util.function.Supplier

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


    class AndroidProfilerToolWindowFactory : DumbAware, ToolWindowFactory {
        private fun makeFakeCapture() = FakeCaptureObject.Builder()
            .setCaptureName("SAMPLE_CAPTURE1")
            .setStartTime(0)
            .setEndTime(10)
            .setInfoMessage("Foo")
            .build()

        private fun createStageWithMemoryCaptureStageViewLoaded(profilers:StudioProfilers, mockLoader: CaptureObjectLoader, capture: CaptureObject) = MemoryCaptureStage(
            profilers,
            mockLoader,
            CaptureDurationData(1, false, false, CaptureEntry(Any(), Supplier { capture })),
            MoreExecutors.directExecutor()
        ).apply {
            enter()
            captureSelection.refreshSelectedHeap()
        }

        override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
            ProjectHolder.project = project
            val file = File("D:\\idea_source_code\\idea-9536633b03339050d6bd0b67517133838a37d449.tar\\idea-9536633b03339050d6bd0b67517133838a37d449\\profilers\\testData\\hprofs\\displayingbitmaps_leakedActivity.hprof")
            val timestampsNs = HprofUtils.computeImportedFileStartEndTimestampsNs(file)
            val stage = LeeMemoryCaptureStage(null,CaptureObjectLoader(),
                timestampsNs.first,
                1,
                HeapDumpCaptureObject(project, file, timestampsNs.first, timestampsNs.second)
            )
            val stageView = LeeMemoryCaptureStageView(stage,timestampsNs.first,timestampsNs.second)
            stage.enter()
            val contentFactory = ContentFactory.SERVICE.getInstance()
            val content = contentFactory.createContent(stageView.mainPanel, "TCL AI Profiler", false)
            toolWindow.contentManager.addContent(content)
            toolWindow.activate(null)
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