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

import com.tcl.tools.adtui.chart.statechart.StateChart;
import com.tcl.tools.adtui.chart.statechart.StateChartColorProvider;
import com.tcl.tools.adtui.chart.statechart.StateChartTextConverter;
import com.tcl.tools.adtui.model.formatter.TimeFormatter;
import com.tcl.tools.adtui.model.trackgroup.TrackModel;
import com.tcl.tools.adtui.trackgroup.TrackRenderer;
import com.tcl.tools.profilers.DataVisualizationColors;
import com.tcl.tools.profilers.ProfilerColors;
import com.tcl.tools.profilers.cpu.VsyncPanel;import com.tcl.tools.profilers.cpu.systemtrace.CpuFramesModel;
import com.tcl.tools.profilers.cpu.systemtrace.SystemTraceFrame;
import java.awt.Color;
import java.util.function.BooleanSupplier;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;

/**
 * Track renderer for Atrace frame rendering data.
 */
public class FramesTrackRenderer implements TrackRenderer<CpuFramesModel.FrameState> {
  private final BooleanSupplier myVsyncEnabler;

  public FramesTrackRenderer(BooleanSupplier vsyncEnabler) {
    myVsyncEnabler = vsyncEnabler;
  }

  @NotNull
  @Override
  public JComponent render(@NotNull TrackModel<CpuFramesModel.FrameState, ?> trackModel) {
    return VsyncPanel.of(new StateChart<>(
                           trackModel.getDataModel().getModel(), new FrameColorProvider(), new FrameTextConverter()),
                         trackModel.getDataModel().getVsyncSeries(),
                         myVsyncEnabler);
  }

  private static class FrameColorProvider extends StateChartColorProvider<SystemTraceFrame> {
    @NotNull
    @Override
    public Color getColor(boolean isMouseOver, @NotNull SystemTraceFrame value) {
      switch (value.getTotalPerfClass()) {
        case BAD:
          return isMouseOver ? ProfilerColors.SLOW_FRAME_COLOR_HIGHLIGHTED : ProfilerColors.SLOW_FRAME_COLOR;
        case GOOD:
          return DataVisualizationColors.getPaletteManager().getBackgroundColor(
            DataVisualizationColors.BACKGROUND_DATA_COLOR_NAME, isMouseOver);
        default:
          return ProfilerColors.CPU_STATECHART_DEFAULT_STATE;
      }
    }
  }

  private static class FrameTextConverter implements StateChartTextConverter<SystemTraceFrame> {
    @NotNull
    @Override
    public String convertToString(@NotNull SystemTraceFrame value) {
      // Show timing on bad frames.
      if (value.getTotalPerfClass() == SystemTraceFrame.PerfClass.BAD) {
        return TimeFormatter.getSingleUnitDurationString(value.getDurationUs());
      }
      return "";
    }
  }
}
