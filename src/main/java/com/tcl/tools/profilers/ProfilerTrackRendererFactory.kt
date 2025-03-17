/*
 * Copyright (C) 2019 The Android Open Source Project
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
package com.tcl.tools.profilers

import com.tcl.tools.adtui.trackgroup.TrackRendererFactory
import com.tcl.tools.profilers.cpu.AndroidFrameEventTrackRenderer
import com.tcl.tools.profilers.cpu.BufferQueueTrackRenderer
import com.tcl.tools.profilers.cpu.CpuCoreTrackRenderer
import com.tcl.tools.profilers.cpu.CpuFrequencyTrackRenderer
import com.tcl.tools.profilers.cpu.CpuThreadTrackRenderer
import com.tcl.tools.profilers.cpu.FramesTrackRenderer
import com.tcl.tools.profilers.cpu.RssMemoryTrackRenderer
import com.tcl.tools.profilers.cpu.SurfaceflingerTrackRenderer
import com.tcl.tools.profilers.cpu.VsyncTrackRenderer
import com.tcl.tools.profilers.customevent.CustomEventTrackRenderer
import com.tcl.tools.profilers.event.LifecycleTrackRenderer
import com.tcl.tools.profilers.event.UserEventTrackRenderer
import java.util.function.BooleanSupplier

/**
 * Implementation of [TrackRendererFactory] for creating track renderers used in profilers.
 */
class ProfilerTrackRendererFactory(private val myProfilersView: StudioProfilersView,
                                   private val vsyncEnabler: BooleanSupplier) : TrackRendererFactory<ProfilerTrackRendererType> {
  override fun createRenderer(rendererType: ProfilerTrackRendererType) = when (rendererType) {
    ProfilerTrackRendererType.APP_LIFECYCLE -> LifecycleTrackRenderer()
    ProfilerTrackRendererType.USER_INTERACTION -> UserEventTrackRenderer()
    ProfilerTrackRendererType.FRAMES -> FramesTrackRenderer(vsyncEnabler)
    ProfilerTrackRendererType.SURFACEFLINGER -> SurfaceflingerTrackRenderer(vsyncEnabler)
    ProfilerTrackRendererType.VSYNC -> VsyncTrackRenderer(vsyncEnabler)
    ProfilerTrackRendererType.BUFFER_QUEUE -> BufferQueueTrackRenderer(vsyncEnabler)
    ProfilerTrackRendererType.CPU_THREAD -> CpuThreadTrackRenderer(myProfilersView, vsyncEnabler)
    ProfilerTrackRendererType.CPU_CORE -> CpuCoreTrackRenderer()
    ProfilerTrackRendererType.CPU_FREQUENCY -> CpuFrequencyTrackRenderer()
    ProfilerTrackRendererType.RSS_MEMORY -> RssMemoryTrackRenderer()
    ProfilerTrackRendererType.ANDROID_FRAME_EVENT -> AndroidFrameEventTrackRenderer(vsyncEnabler)
    ProfilerTrackRendererType.CUSTOM_EVENTS -> CustomEventTrackRenderer()
  }
}