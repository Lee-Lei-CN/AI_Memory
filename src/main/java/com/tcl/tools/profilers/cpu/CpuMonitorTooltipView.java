/*
 * Copyright (C) 2017 The Android Open Source Project
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

import com.tcl.tools.adtui.LegendComponent;
import com.tcl.tools.adtui.LegendConfig;
import com.tcl.tools.profilers.ProfilerColors;
import com.tcl.tools.profilers.ProfilerMonitorTooltipView;
import com.tcl.tools.profilers.StageView;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;

public class CpuMonitorTooltipView extends ProfilerMonitorTooltipView<CpuMonitor> {
  public CpuMonitorTooltipView(StageView parent, @NotNull CpuMonitorTooltip tooltip) {
    super(tooltip.getMonitor());
  }

  @NotNull
  @Override
  public JComponent createTooltip() {
    CpuMonitor.Legends legends = getMonitor().getTooltipLegends();

    LegendComponent legend =
      new LegendComponent.Builder(legends).setVerticalPadding(0).setOrientation(LegendComponent.Orientation.VERTICAL).build();
    legend.configure(legends.getCpuLegend(), new LegendConfig(LegendConfig.IconType.BOX, ProfilerColors.CPU_USAGE_CAPTURED));

    return legend;
  }
}
