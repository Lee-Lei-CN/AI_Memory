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
package com.tcl.tools.profilers.cpu.config

import com.tcl.tools.adtui.model.options.OptionsProperty
import com.tcl.tools.adtui.model.options.Slider
import com.android.tools.profiler.proto.Cpu

/**
 * Configuration for sampled art traces.
 */
class ArtSampledConfiguration(name: String) : ProfilingConfiguration(name) {
  @OptionsProperty(name = "Sample interval: ", group = TRACE_CONFIG_GROUP, order = 100, unit = "Us (Microseconds)")
  var profilingSamplingIntervalUs = DEFAULT_SAMPLING_INTERVAL_US

  @Slider(min = 1, max = 32, step = 1)
  @OptionsProperty(group = TRACE_CONFIG_GROUP, order = 101, name = "File size limit:", unit = "Mb",
                   description = "Maximum recording output file size. On Android 8.0 (API level 26) and higher, this value is ignored.")
  var profilingBufferSizeInMb = DEFAULT_BUFFER_SIZE_MB

  override fun buildUserOptions(): Cpu.CpuTraceConfiguration.UserOptions.Builder {
    return Cpu.CpuTraceConfiguration.UserOptions.newBuilder()
      .setTraceMode(Cpu.CpuTraceMode.SAMPLED)
      .setBufferSizeInMb(profilingBufferSizeInMb)
      .setSamplingIntervalUs(profilingSamplingIntervalUs)
  }

  override fun getTraceType(): Cpu.CpuTraceType {
    return Cpu.CpuTraceType.ART
  }

  override fun getRequiredDeviceLevel(): Int {
    return 0
  }
}