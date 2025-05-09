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

import com.tcl.tools.adtui.AxisComponent
import com.tcl.tools.adtui.TabularLayout
import com.tcl.tools.adtui.chart.linechart.LineChart
import com.tcl.tools.adtui.chart.linechart.LineConfig
import com.tcl.tools.adtui.model.trackgroup.TrackModel
import com.tcl.tools.adtui.trackgroup.TrackRenderer
import com.tcl.tools.profilers.DataVisualizationColors
import com.tcl.tools.profilers.cpu.systemtrace.RssMemoryTrackModel
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Track renderer for System Trace RSS memory counters.
 */
class RssMemoryTrackRenderer : TrackRenderer<RssMemoryTrackModel> {
  override fun render(trackModel: TrackModel<RssMemoryTrackModel, *>): JComponent {
    val lineChart = LineChart(trackModel.dataModel).apply {
      configure(trackModel.dataModel.memoryCounterSeries,
                LineConfig(DataVisualizationColors.paletteManager.getBackgroundColor(trackModel.title.hashCode())).setFilled(true))
      setFillEndGap(true)
    }
    val leftAxis = AxisComponent(trackModel.dataModel.axisComponentModel, AxisComponent.AxisOrientation.RIGHT).apply {
      setShowAxisLine(false)
      setHideTickAtMin(true)
    }
    return JPanel(TabularLayout("*", "*")).apply {
      add(leftAxis, TabularLayout.Constraint(0, 0))
      add(lineChart, TabularLayout.Constraint(0, 0))
    }
  }
}