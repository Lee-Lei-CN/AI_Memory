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
package com.tcl.tools.test

import com.tcl.tools.adtui.StatLabel
import com.tcl.tools.adtui.TreeWalker
import com.tcl.tools.profilers.ProfilerClient
import com.tcl.tools.profilers.StudioProfilers
import com.tcl.tools.profilers.StudioProfilersView
import com.tcl.tools.profilers.memory.CaptureEntry
import com.tcl.tools.profilers.memory.CapturePanel
import com.tcl.tools.profilers.memory.MainMemoryProfilerStage
import com.tcl.tools.profilers.memory.MemoryCaptureSelection
import com.tcl.tools.profilers.memory.adapters.classifiers.AllHeapSet
import com.tcl.tools.profilers.memory.adapters.classifiers.HeapSet
import io.grpc.inprocess.InProcessChannelBuilder
import org.junit.Before
import org.junit.Test

class CapturePanelTest {

  private lateinit var profilers: StudioProfilers
  private lateinit var ideProfilerServices: FakeIdeProfilerServices
  private val myTimer = FakeTimer()

  @Before
  fun setupBase() {
    val channel = InProcessChannelBuilder.forName("MemoryProfilerStageViewTestChannel").usePlaintext().directExecutor().build()

    ideProfilerServices = FakeIdeProfilerServices()
    profilers = StudioProfilers(ProfilerClient(channel), ideProfilerServices, myTimer)
    profilers.setPreferredProcess("lee", "lee process", null)
  }

  @Test
  fun `panel shows numbers for selected heap`() {
    val capture = FakeCaptureObject.Builder().build()
    val heap1 = HeapSet(capture, "heap1", 1)
    val heap2 = HeapSet(capture, "heap2", 2)
    val allHeap = AllHeapSet(capture, arrayOf(heap1, heap2)).also { it.clearClassifierSets() }

    val insts1 = arrayOf(FakeInstanceObject.Builder(capture, 1, "obj").setHeapId(1).setShallowSize(4).build(),
                         FakeInstanceObject.Builder(capture, 2, "int").setHeapId(1).setShallowSize(8).build(),
                         FakeInstanceObject.Builder(capture, 3, "str").setHeapId(1).setShallowSize(14).build())
    val insts2 = arrayOf(FakeInstanceObject.Builder(capture, 4, "cat").setHeapId(2).setShallowSize(3).build(),
                         FakeInstanceObject.Builder(capture, 5, "dog").setHeapId(2).setShallowSize(5).build(),
                         FakeInstanceObject.Builder(capture, 6, "rat").setHeapId(2).setShallowSize(7).build())
    val insts = insts1 + insts2
    insts.forEach { allHeap.addDeltaInstanceObject(it) }


    val selection = MemoryCaptureSelection(profilers.ideServices)
    val panel = CapturePanel(selection, null, profilers.timeline.selectionRange, true)

    selection.selectCaptureEntry(CaptureEntry(Any()) { capture })
    selection.finishSelectingCaptureObject(capture)

    selection.selectHeapSet(heap1)
  }
}