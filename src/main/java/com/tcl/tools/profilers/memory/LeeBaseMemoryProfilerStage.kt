/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tcl.tools.profilers.memory

import com.google.common.util.concurrent.ListenableFuture
import com.tcl.tools.profilers.ProfilerMode
import com.tcl.tools.profilers.StreamingStage
import com.tcl.tools.profilers.StudioProfilers
import com.tcl.tools.profilers.memory.adapters.CaptureObject
import com.tcl.tools.profilers.memory.adapters.classifiers.HeapSet
import com.google.common.util.concurrent.MoreExecutors
import com.google.wireless.android.sdk.stats.AndroidProfilerEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.sun.jna.Memory
import com.tcl.tools.adtui.model.Range
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit


abstract class LeeBaseMemoryProfilerStage(profilers: StudioProfilers?, protected val loader: CaptureObjectLoader) :
    StreamingStage(profilers) {

    val captureSelection = MemoryCaptureSelection(profilers?.ideServices)
    protected var pendingCaptureStartTime = INVALID_START_TIME
    protected var updateCaptureOnSelection = true
    val isPendingCapture get() = pendingCaptureStartTime != INVALID_START_TIME

    companion object {
        const val INVALID_START_TIME = -1L

        private val logger
            get() = Logger.getInstance(BaseMemoryProfilerStage::class.java)
    }

    override fun getStageType() = AndroidProfilerEvent.Stage.MEMORY_STAGE

    protected fun doSelectCaptureDuration(startTime: Long, duration: Long, captureObject: CaptureObject) {
        pendingCaptureStartTime = INVALID_START_TIME
        if (!captureSelection.selectCaptureEntry(captureObject)) {
            return
        }

        updateCaptureOnSelection = false

        val captureObject = captureSelection.selectedCapture
        val clear = Runnable {
            captureSelection.selectCaptureEntry(null)
            timeline?.selectionRange?.clear()
            captureSelection.aspect.changed(CaptureSelectionAspect.CURRENT_LOADED_CAPTURE)
            profilerMode = ProfilerMode.NORMAL
        }
        if (captureObject == null) {
            // Loading a capture can fail, in which case we reset everything.
            clear.run()
            return
        }
        captureSelection.classGroupingModel.update()

        // Synchronize selection with the capture object. Do so only if the capture object is not ongoing.
        if (startTime > 0 && duration != Long.MAX_VALUE) {
            // TODO: (revisit) we have an special case in interacting with RangeSelectionModel
            //  where if the user tries to select a heap dump that is on
            // top of an ongoing live allocation capture (duration == Long.MAX_VALUE),
            // the live capture would take precedence given it always
            // intersects with the previous selection. Here we clear the previous selection first to avoid said interaction.
//            timeline?.selectionRange?.clear()
            val startTimeUs = TimeUnit.NANOSECONDS.toMicros(captureObject.startTimeNs).toDouble()
            val endTimeUs = TimeUnit.NANOSECONDS.toMicros(captureObject.endTimeNs).toDouble()
//            timeline?.selectionRange?.set(startTimeUs, endTimeUs)
        }

        updateCaptureOnSelection = true

        val queryRange = Range(captureObject.startTimeNs.toDouble(), captureObject.endTimeNs.toDouble())
        val load = Runnable {

            // TODO: (revisit) - do we want to pass in data range to loadCapture as well?
            val future = loader.loadCapture(captureObject, queryRange, null)
            future.addListener(Runnable {
                val dispatchThread = ApplicationManager.getApplication().isDispatchThread()
                System.out.println(" isDispatchThread $dispatchThread")
                if (!dispatchThread) {
                    ApplicationManager.getApplication().invokeLater {
                        val dispatchThread = ApplicationManager.getApplication().isDispatchThread()
                        System.out.println(" isDispatchThread2 $dispatchThread")
                        doRenderView(future, startTime, duration, captureObject)
                    }
                } else {
                    doRenderView(future, startTime, duration, captureObject)
                }
            }, MoreExecutors.directExecutor())
//            profilerMode = ProfilerMode.EXPANDED
        }
        ApplicationManager.getApplication().invokeAndWait {
            val dispatchThread = ApplicationManager.getApplication().isDispatchThread
            System.out.println(" invokeAndWait $dispatchThread")
            load.run()
        }
    }

    private fun doRenderView(
        future: ListenableFuture<CaptureObject>,
        startTime: Long,
        duration: Long,
        captureObject: CaptureObject
    ) {
        try {

            val loadedCaptureObject = future.get()
            if (captureSelection.finishSelectingCaptureObject(loadedCaptureObject)) {
                captureSelection.selectHeapSet((loadedCaptureObject.heapSets).getDefault())
            } else {
                // Capture loading failed.
                // TODO: loading has somehow failed - we need to inform users about the error status.
                doSelectCaptureDuration(startTime, duration, captureObject)
            }
            // Triggers the aspect to inform listeners that the heap content/filter has changed.
            captureSelection.refreshSelectedHeap()
        } catch (exception: InterruptedException) {
            Thread.currentThread().interrupt()
            doSelectCaptureDuration(startTime, duration, captureObject)
        } catch (exception: ExecutionException) {
            doSelectCaptureDuration(startTime, duration, captureObject)
            logger.error(exception)
        } catch (ignored: CancellationException) {
            // No-op: a previous load-capture task is canceled due to another capture being selected and loaded.
        }
    }
}

private fun Collection<HeapSet>.getDefault(): HeapSet? =
    find { it.name == "app" } ?: find { it.name == "default" } ?: if (!isEmpty()) toTypedArray()[0]
    else null