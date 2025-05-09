/*
 * Copyright (C) 2021 The Android Open Source Project
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

import com.tcl.tools.adtui.chart.statechart.StateChart
import com.tcl.tools.adtui.chart.statechart.StateChartColorProvider
import com.tcl.tools.adtui.chart.statechart.StateChartTextConverter
import com.tcl.tools.adtui.model.trackgroup.TrackModel
import com.tcl.tools.adtui.trackgroup.TrackRenderer
import com.tcl.tools.profilers.DataVisualizationColors
import com.tcl.tools.profilers.cpu.systemtrace.AndroidFrameEvent
import com.tcl.tools.profilers.cpu.systemtrace.AndroidFrameEventTrackModel
import com.intellij.util.ui.UIUtil
import java.awt.Color
import java.util.function.BooleanSupplier

/**
 * Track renderer for the a frame lifecycle track representing Android frames in a specific rendering phase.
 */
class AndroidFrameEventTrackRenderer(private val vsyncEnabler: BooleanSupplier) : TrackRenderer<AndroidFrameEventTrackModel> {
  override fun render(trackModel: TrackModel<AndroidFrameEventTrackModel, *>) =
    StateChart(trackModel.dataModel, AndroidFrameEventColorProvider(), AndroidFrameEventTextProvider()).apply {
      addRowIndexChangeListener {
        trackModel.dataModel.activeSeriesIndex = it
      }
    }.let { VsyncPanel.of(it, trackModel.dataModel.vsyncSeries, vsyncEnabler)}
}

private class AndroidFrameEventColorProvider : StateChartColorProvider<AndroidFrameEvent>() {
  override fun getColor(isMouseOver: Boolean, value: AndroidFrameEvent): Color = when (value) {
    is AndroidFrameEvent.Data -> DataVisualizationColors.paletteManager.getBackgroundColor(value.frameNumber, isMouseOver)
    is AndroidFrameEvent.Padding -> UIUtil.TRANSPARENT_COLOR
  }

  override fun getFontColor(isMouseOver: Boolean, value: AndroidFrameEvent): Color = when (value) {
    is AndroidFrameEvent.Data -> DataVisualizationColors.paletteManager.getForegroundColor(value.frameNumber)
    is AndroidFrameEvent.Padding -> UIUtil.TRANSPARENT_COLOR
  }
}

private class AndroidFrameEventTextProvider : StateChartTextConverter<AndroidFrameEvent> {
  override fun convertToString(value: AndroidFrameEvent): String = when (value) {
    is AndroidFrameEvent.Data -> value.frameNumber.toString()
    is AndroidFrameEvent.Padding -> ""
  }
}