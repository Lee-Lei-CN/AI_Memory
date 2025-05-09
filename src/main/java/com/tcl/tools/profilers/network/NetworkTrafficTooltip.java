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
package com.tcl.tools.profilers.network;

import com.tcl.tools.adtui.model.TooltipModel;
import org.jetbrains.annotations.NotNull;

/**
 * Tooltip shown when user hovers mouse over the traffic area in Network Profiler stage. Shows network traffic, number of connects, etc.
 */
public final class NetworkTrafficTooltip implements TooltipModel {
  @NotNull private final NetworkProfilerStage myStage;

  NetworkTrafficTooltip(@NotNull NetworkProfilerStage stage) {
    myStage = stage;
  }

  @NotNull
  public NetworkProfilerStage.NetworkStageLegends getLegends() {
    return myStage.getTooltipLegends();
  }
}
