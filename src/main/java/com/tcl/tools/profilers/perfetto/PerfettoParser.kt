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
package com.tcl.tools.profilers.perfetto

import com.tcl.tools.adtui.model.Range
import com.android.tools.profiler.proto.Cpu
import com.tcl.tools.profilers.IdeProfilerServices
import com.tcl.tools.profilers.cpu.CpuCapture
import com.tcl.tools.profilers.cpu.MainProcessSelector
import com.tcl.tools.profilers.cpu.TraceParser
import com.tcl.tools.profilers.cpu.systemtrace.AtraceParser
import com.tcl.tools.profilers.cpu.systemtrace.ProcessListSorter
import com.tcl.tools.profilers.cpu.systemtrace.SystemTraceCpuCaptureBuilder
import com.tcl.tools.profilers.cpu.systemtrace.SystemTraceSurfaceflingerManager
import com.intellij.openapi.diagnostic.Logger
import perfetto.protos.PerfettoTrace
import java.io.File
import java.util.Base64
import java.util.concurrent.TimeUnit

class PerfettoParser(private val mainProcessSelector: MainProcessSelector,
                     private val ideProfilerServices: IdeProfilerServices) : TraceParser {

  companion object {
    private val LOGGER = Logger.getInstance(PerfettoParser::class.java)
    // This lock controls access to TPD, see comment below in the synchronized block.
    // We use this instead of @Synchronized, because CpuCaptureParser build and return new instances of PerfettoParser,
    // so we need a static object to serve as the lock monitor.
    val TPD_LOCK = Any()
  }

  override fun parse(file: File, traceId: Long): CpuCapture {
    if (ideProfilerServices.featureConfig.isUseTraceProcessor) {
      return parseUsingTraceProcessor(file, traceId)
    } else {
      return parseUsingTrebuchet(file, traceId)
    }
  }

  private fun parseUsingTrebuchet(file: File, traceId: Long): CpuCapture {
    val atraceParser = AtraceParser(Cpu.CpuTraceType.PERFETTO, mainProcessSelector)
    return atraceParser.parse(file, traceId)
  }

  private fun parseUsingTraceProcessor(file: File, traceId: Long): CpuCapture {
    // We only allow one instance running here, because TPD currently doesn't handle the multiple loaded traces case (since we can only see
    // one in the UI), so any attempt of doing so (like double clicking very fast in the UI to trigger two parses) might lead to a race
    // condition and end up with a failure.
    synchronized(TPD_LOCK) {
      val traceProcessor = ideProfilerServices.traceProcessorService

      val traceLoaded = traceProcessor.loadTrace(traceId, file, ideProfilerServices)
      if (!traceLoaded) {
        error("Unable to load trace with TPD.")
      }

      val processList = traceProcessor.getProcessMetadata(traceId, ideProfilerServices)
      check(processList.isNotEmpty()) { "Invalid trace without any process information." }
      val traceUIMetadata = traceProcessor.getTraceMetadata(traceId, "ui_state", ideProfilerServices)
      var processHint = mainProcessSelector.nameHint
      val initialViewRange = Range()

      if (traceUIMetadata.isNotEmpty()) {
        try {
          val uiState = PerfettoTrace.UiState.parseFrom(Base64.getDecoder().decode(traceUIMetadata.last()))
          if (uiState.highlightProcess.hasPid()) {
            val wantedProcessId = uiState.getHighlightProcess().getPid()
            processHint = processList.find { it.id == wantedProcessId }?.getSafeProcessName() ?: mainProcessSelector.nameHint
          } else if (uiState.highlightProcess.hasCmdline()) {
            processHint = uiState.highlightProcess.cmdline
          }
          if (uiState.timelineStartTs != 0L && uiState.timelineEndTs != 0L) {
            initialViewRange.set(TimeUnit.NANOSECONDS.toMicros(uiState.timelineStartTs).toDouble(),
                                 TimeUnit.NANOSECONDS.toMicros(uiState.timelineEndTs).toDouble());
          }
        }
        catch (throwable:Throwable) {
          // Failed to parse / decode ui metadata log and continue.
          LOGGER.warn("Trace contained ui-state, however it failed to parse correctly. Ui state will not be loaded", throwable)
        }
      }
      // If a valid process was not parsed from the UI metadata
      val processListSorter = ProcessListSorter(processHint)
      val userSelectedProcess = mainProcessSelector.apply(processListSorter.sort(processList))
      checkNotNull(userSelectedProcess) { "It was not possible to select a process for this trace." }
      val pidsToQuery = mutableListOf(userSelectedProcess)
      processList.find {
        it.getSafeProcessName().endsWith(SystemTraceSurfaceflingerManager.SURFACEFLINGER_PROCESS_NAME)
      }?.let { pidsToQuery.add(it.id) }
      val selectedProcessName = processList.first { processModel -> processModel.id == userSelectedProcess }.name
      val model = traceProcessor.loadCpuData(traceId, pidsToQuery, selectedProcessName, ideProfilerServices)

      val builder = SystemTraceCpuCaptureBuilder(model)

      if (initialViewRange.isEmpty()) {
        initialViewRange.set(model.getCaptureStartTimestampUs().toDouble(), model.getCaptureEndTimestampUs().toDouble())
      }
      return builder.build(traceId, userSelectedProcess, initialViewRange)
    }
  }
}