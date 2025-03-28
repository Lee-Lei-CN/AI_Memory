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
import com.intellij.ui.JBSplitter
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.tcl.tools.ProjectHolder
import com.tcl.tools.adtui.model.Range
import com.tcl.tools.idea.profilers.AndroidProfilerService
import com.tcl.tools.idea.profilers.AndroidProfilerToolWindow
import com.tcl.tools.profilers.memory.*
import com.tcl.tools.profilers.memory.adapters.CaptureObject
import com.tcl.tools.profilers.memory.adapters.classifiers.AllHeapSet
import com.tcl.tools.profilers.memory.adapters.classifiers.HeapSet
import com.tcl.tools.test.FakeCaptureObject
import com.tcl.tools.test.FakeIdeProfilerServices
import com.tcl.tools.test.FakeInstanceObject
import com.tcl.tools.test.FakeTimer
import io.grpc.inprocess.InProcessChannelBuilder
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
//            val channel =
//                InProcessChannelBuilder.forName("MemoryProfilerStageViewTestChannel").usePlaintext().directExecutor()
//                    .build()
//            val myTimer = FakeTimer()
//            val ideProfilerServices = FakeIdeProfilerServices()
//            val profilers = StudioProfilers(ProfilerClient(channel), ideProfilerServices, myTimer)
//            profilers.setPreferredProcess("lee", "lee process", null)

            val capture = makeFakeCapture()
            val heap1 = HeapSet(capture, "heap1", 1)
            val heap2 = HeapSet(capture, "heap2", 2)
            val allHeap = AllHeapSet(capture, arrayOf(heap1, heap2)).also { it.clearClassifierSets() }

            val insts1 = arrayOf(
                FakeInstanceObject.Builder(capture, 1, "obj").setHeapId(1).setShallowSize(4).build(),
                FakeInstanceObject.Builder(capture, 2, "int").setHeapId(1).setShallowSize(8).build(),
                FakeInstanceObject.Builder(capture, 3, "str").setHeapId(1).setShallowSize(14).build()
            )
            val insts2 = arrayOf(
                FakeInstanceObject.Builder(capture, 4, "cat").setHeapId(2).setShallowSize(3).build(),
                FakeInstanceObject.Builder(capture, 5, "dog").setHeapId(2).setShallowSize(5).build(),
                FakeInstanceObject.Builder(capture, 6, "rat").setHeapId(2).setShallowSize(7).build()
            )
            val insts = insts1 + insts2
            insts.forEach { allHeap.addDeltaInstanceObject(it) }


            val selection = MemoryCaptureSelection(null)
            val panel = CapturePanel(selection, null, Range(), true)

            val instanceDetailsSplitter = JBSplitter(false).apply {
                isOpaque = true
                firstComponent = panel.classSetView.component
                secondComponent = panel.instanceDetailsView.component
            }
            val contentFactory = ContentFactory.SERVICE.getInstance()
            val content = contentFactory.createContent(instanceDetailsSplitter, "TCL AI Profiler", false)
            toolWindow.contentManager.addContent(content)
//            toolWindow.setIcon(StudioIcons.Shell.ToolWindows.ANDROID_PROFILER)
            selection.selectCaptureEntry(CaptureEntry(Any()) { capture })
            selection.finishSelectingCaptureObject(capture)
            selection.selectHeapSet(heap1)
            selection.refreshSelectedHeap()
            // Forcibly synchronize the Tool Window to a visible state. Otherwise, the Tool Window may not auto-hide correctly.
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