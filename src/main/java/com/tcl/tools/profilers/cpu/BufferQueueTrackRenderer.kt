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
package com.tcl.tools.profilers.cpu

import com.tcl.tools.adtui.chart.linechart.LineChart
import com.tcl.tools.adtui.chart.linechart.LineConfig
import com.tcl.tools.adtui.model.trackgroup.TrackModel
import com.tcl.tools.adtui.trackgroup.TrackRenderer
import com.tcl.tools.profilers.DataVisualizationColors
import com.tcl.tools.profilers.cpu.systemtrace.BufferQueueTrackModel
import java.util.function.BooleanSupplier
import javax.swing.JComponent

/**
 * Track renderer for System Trace BufferQueue counter.
 */
class BufferQueueTrackRenderer(private val vsyncEnabler: BooleanSupplier) : TrackRenderer<BufferQueueTrackModel> {
  override fun render(trackModel: TrackModel<BufferQueueTrackModel, *>): JComponent = trackModel.dataModel.let { lineChartModel ->
    val lineChart = LineChart(lineChartModel)
    lineChart.configure(
      lineChartModel.bufferQueueSeries,
      LineConfig(DataVisualizationColors.paletteManager.getBackgroundColor(DataVisualizationColors.BACKGROUND_DATA_COLOR_NAME, 0))
        .setStepped(true))
    lineChart.setFillEndGap(true)
    VsyncPanel.of(lineChart, lineChartModel.viewRange, lineChartModel.systemTraceData.getVsyncCounterValues(), vsyncEnabler)
  }
}