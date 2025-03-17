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
import com.tcl.tools.adtui.common.AdtUiUtils;
import com.tcl.tools.adtui.model.trackgroup.TrackModel;
import com.tcl.tools.adtui.trackgroup.TrackRenderer;
import com.tcl.tools.profilers.DataVisualizationColors;
import com.tcl.tools.profilers.ProfilerColors;
import com.tcl.tools.profilers.cpu.systemtrace.CpuCoreTrackModel;
import com.tcl.tools.profilers.cpu.systemtrace.CpuThreadSliceInfo;
import com.google.common.annotations.VisibleForTesting;
import java.awt.Color;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;

/**
 * Track renderer for CPU cores in CPU capture stage.
 */
public class CpuCoreTrackRenderer implements TrackRenderer<CpuCoreTrackModel> {
  @NotNull
  @Override
  public JComponent render(@NotNull TrackModel<CpuCoreTrackModel, ?> trackModel) {
    CpuCoreTrackModel dataModel = trackModel.getDataModel();
    StateChart<CpuThreadSliceInfo> stateChart =
      new StateChart<>(dataModel.getStateChartModel(), new CpuCoreColorProvider(), CpuThreadInfo::getName);
    stateChart.setOpaque(true);
    return stateChart;
  }

  @VisibleForTesting
  protected static class CpuCoreColorProvider extends StateChartColorProvider<CpuThreadSliceInfo> {
    @NotNull
    @Override
    public Color getColor(boolean isMouseOver, @NotNull CpuThreadSliceInfo value) {
      // On the null thread return the background color.
      if (value == CpuThreadSliceInfo.NULL_THREAD) {
        return ProfilerColors.DEFAULT_BACKGROUND;
      }
      return DataVisualizationColors.getPaletteManager().getBackgroundColor(value.getId(), isMouseOver);
    }

    @NotNull
    @Override
    public Color getFontColor(boolean isMouseOver, @NotNull CpuThreadSliceInfo value) {
      // On the null thread return the default font color.
      if (value == CpuThreadSliceInfo.NULL_THREAD) {
        return AdtUiUtils.DEFAULT_FONT_COLOR;
      }
      return DataVisualizationColors.getPaletteManager().getForegroundColor(value.getId());
    }
  }
}
