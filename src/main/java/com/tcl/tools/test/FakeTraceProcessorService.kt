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
package com.android.tools.profilers

import com.android.tools.profiler.perfetto.proto.TraceProcessor
import com.android.tools.profiler.proto.Cpu
import com.tcl.tools.profilers.cpu.systemtrace.CpuCoreModel
import com.tcl.tools.profilers.cpu.systemtrace.ProcessModel
import com.tcl.tools.profilers.cpu.systemtrace.SystemTraceModelAdapter
import com.tcl.tools.profilers.cpu.systemtrace.ThreadModel
import com.tcl.tools.profilers.perfetto.traceprocessor.TraceProcessorService
import java.io.File

class FakeTraceProcessorService: TraceProcessorService {

  companion object {
    private val validTraces = setOf<File>()

    // Keep the loaded traces in a static JVM cache, so we can re-use across the same test suite.
    private val loadedModelProcessList: MutableMap<String, List<ProcessModel>> = mutableMapOf()
    private val loadedModelMapCache: MutableMap<String, Map<Int, SystemTraceModelAdapter>> = mutableMapOf()

    private fun loadProcessModelListFor(traceFile: File): List<ProcessModel> {
      val cacheKey = traceFile.name
//
//      if (!loadedModelMapCache.containsKey(cacheKey)) {
//        val serializedProcessModelList = CpuProfilerTestUtils.getTraceFile("${traceFile.name}_process_list")
//        val ois = ObjectInputStream(FileInputStream(serializedProcessModelList))
//        @Suppress("UNCHECKED_CAST")
//        loadedModelProcessList[cacheKey] = ois.readObject() as List<ProcessModel>
//        ois.close()
//      }

      return loadedModelProcessList[cacheKey] ?: error("$cacheKey should be present in the loadedModelProcessList")
    }

    // For each known trace we store a map for each possible process id to the generated model.
    private fun getModelMapFor(traceFile: File): Map<Int, SystemTraceModelAdapter> {
      val cacheKey = traceFile.name
//
//      if (!loadedModelMapCache.containsKey(cacheKey)) {
//        val serializedModelMap = CpuProfilerTestUtils.getTraceFile("${traceFile.name}_tpd_model")
//        val ois = ObjectInputStream(FileInputStream(serializedModelMap))
//        @Suppress("UNCHECKED_CAST")
//        loadedModelMapCache[cacheKey] = ois.readObject() as Map<Int, SystemTraceModelAdapter>
//        ois.close()
//      }

      return loadedModelMapCache[cacheKey] ?: error("$cacheKey should be present in the modelMapCache")
    }
  }

  private val loadedTraces = mutableMapOf<Long, File>()

  /**
   * If true, will always return false on loadTrace() calls, to simulate when the daemon return a failure when attempting to
   * load a trace file.
   */
  var forceFailLoadTrace = false

  /**
   * Setup test UiState data to be returned when parsing a trace. If the trace id is not found in this list, an empty UiState will be
   * returned.
   */
  val uiStateForTraceId = mutableMapOf<Long, String>()

  private inner class EmptyModelAdapter:  com.tcl.tools.profilers.cpu.systemtrace.SystemTraceModelAdapter {
    override fun getCaptureStartTimestampUs() = 0L
    override fun getCaptureEndTimestampUs() = 0L
    override fun getProcesses(): List<ProcessModel> = emptyList()
    override fun getProcessById(id: Int) = getProcesses().find { it.id == id }
    override fun getDanglingThread(tid: Int): ThreadModel? = null
    override fun getCpuCores(): List<CpuCoreModel> = emptyList()
    override fun getSystemTraceTechnology() = Cpu.CpuTraceType.PERFETTO
    override fun isCapturePossibleCorrupted() = false
    override fun getAndroidFrameLayers(): List<TraceProcessor.AndroidFrameEventsResult.Layer> = emptyList()
  }

  override fun loadTrace(
    traceId: Long,
    traceFile: File,
    ideProfilerServices: com.tcl.tools.profilers.IdeProfilerServices
  ): Boolean {
   return true
  }

  override fun getProcessMetadata(
    traceId: Long,
    ideProfilerServices: com.tcl.tools.profilers.IdeProfilerServices
  ): List<com.tcl.tools.profilers.cpu.systemtrace.ProcessModel> {
   return  emptyList()
  }

  override fun loadCpuData(
    traceId: Long,
    processIds: List<Int>,
    selectedProcessName: String,
    ideProfilerServices: com.tcl.tools.profilers.IdeProfilerServices
  ): com.tcl.tools.profilers.cpu.systemtrace.SystemTraceModelAdapter {
    return EmptyModelAdapter()
  }

  override fun loadMemoryData(
    traceId: Long,
    abi: String,
    memorySet: com.tcl.tools.profilers.memory.adapters.classifiers.NativeMemoryHeapSet,
    ideProfilerServices: com.tcl.tools.profilers.IdeProfilerServices
  ) {

  }

  override fun getTraceMetadata(
    traceId: Long,
    metadataName: String,
    ideProfilerServices: com.tcl.tools.profilers.IdeProfilerServices
  ): List<String> {
    val metadataList = mutableListOf<String>()
    if (metadataName.equals("ui_state") && uiStateForTraceId.containsKey(traceId)) {
      metadataList.add(uiStateForTraceId[traceId]!!)
    }
    return metadataList
  }
}