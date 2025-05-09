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
package com.tcl.tools.profilers.cpu;

import com.tcl.tools.adtui.chart.linechart.LineChart;
import com.tcl.tools.adtui.chart.linechart.LineConfig;
import com.tcl.tools.adtui.model.trackgroup.TrackModel;
import com.tcl.tools.adtui.trackgroup.TrackRenderer;
import com.tcl.tools.profilers.DataVisualizationColors;
import com.tcl.tools.profilers.cpu.VsyncPanel;import com.tcl.tools.profilers.cpu.systemtrace.VsyncTrackModel;
import java.util.function.BooleanSupplier;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;

/**
 * Track renderer for Atrace VSYNC signals.
 */
public class VsyncTrackRenderer implements TrackRenderer<VsyncTrackModel> {
  private final BooleanSupplier myVsyncEnabler;
  public VsyncTrackRenderer(BooleanSupplier vsyncEnabler) {
    myVsyncEnabler = vsyncEnabler;
  }

  @NotNull
  @Override
  public JComponent render(@NotNull TrackModel<VsyncTrackModel, ?> trackModel) {
    VsyncTrackModel lineChartModel = trackModel.getDataModel();
    LineChart lineChart = new LineChart(lineChartModel);
    lineChart.configure(
      lineChartModel.getVsyncCounterSeries(),
      new LineConfig(
        DataVisualizationColors.getPaletteManager().getBackgroundColor(DataVisualizationColors.BACKGROUND_DATA_COLOR_NAME, 0))
        .setStepped(true));
    lineChart.setFillEndGap(true);
    return VsyncPanel.of(lineChart, trackModel.getDataModel().getVsyncCounterSeries(), myVsyncEnabler);
  }
}
