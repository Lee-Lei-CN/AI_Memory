/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.tcl.tools.profilers.cpu.capturedetails;


import static com.tcl.tools.profilers.cpu.capturedetails.CaptureNodeHRenderer.toUnmatchColor;

import com.tcl.tools.profilers.DataVisualizationColors;
import com.tcl.tools.profilers.ProfilerColors;
import com.tcl.tools.profilers.cpu.nodemodel.CaptureNodeModel;
import com.tcl.tools.profilers.cpu.nodemodel.CppFunctionModel;
import com.tcl.tools.profilers.cpu.nodemodel.NativeNodeModel;
import java.awt.Color;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the fill color of the rectangles used to represent {@link NativeNodeModel} nodes in a
 * {@link com.tcl.tools.adtui.chart.hchart.HTreeChart}.
 */
public class NativeModelHChartColors {

  private static void validateModel(@NotNull CaptureNodeModel model) {
    if (!(model instanceof NativeNodeModel)) {
      throw new IllegalStateException("Model must be a subclass of NativeNodeModel.");
    }
  }

  private static boolean isUserFunction(CaptureNodeModel model) {
    if (!(model instanceof CppFunctionModel)) {
      return false; // Not even a function.
    }
    return ((CppFunctionModel)model).isUserCode();
  }

  private static boolean isPlatformFunction(CaptureNodeModel method) {
    // TODO: include all the art-related methods (e.g. artQuickToInterpreterBridge and artMterpAsmInstructionStart)
    return method.getFullName().startsWith("art::") ||
           method.getFullName().startsWith("android::") ||
           method.getFullName().startsWith("art_") ||
           method.getFullName().startsWith("dalvik-jit-code-cache");
  }

  static Color getFillColor(@NotNull CaptureNodeModel model,
                            CaptureDetails.Type chartType,
                            boolean isUnmatched,
                            boolean isFocused,
                            boolean isDeselected) {
    validateModel(model);

    Color color;
    if (chartType == CaptureDetails.Type.CALL_CHART) {
      if (isDeselected) {
        color = DataVisualizationColors.getPaletteManager().getBackgroundColor(
          DataVisualizationColors.BACKGROUND_DATA_COLOR_NAME, isFocused);
      }
      else if (isUserFunction(model)) {
        color = isFocused ? ProfilerColors.CPU_CALLCHART_APP_HOVER : ProfilerColors.CPU_CALLCHART_APP;
      }
      else if (isPlatformFunction(model)) {
        color = isFocused ? ProfilerColors.CPU_CALLCHART_PLATFORM_HOVER : ProfilerColors.CPU_CALLCHART_PLATFORM;
      }
      else {
        color = isFocused ? ProfilerColors.CPU_CALLCHART_VENDOR_HOVER : ProfilerColors.CPU_CALLCHART_VENDOR;
      }
    }
    else {
      if (isUserFunction(model)) {
        color = isFocused ? ProfilerColors.CPU_FLAMECHART_APP_HOVER : ProfilerColors.CPU_FLAMECHART_APP;
      }
      else if (isPlatformFunction(model)) {
        color = isFocused ? ProfilerColors.CPU_FLAMECHART_PLATFORM_HOVER : ProfilerColors.CPU_FLAMECHART_PLATFORM;
      }
      else {
        color = isFocused ? ProfilerColors.CPU_FLAMECHART_VENDOR_HOVER : ProfilerColors.CPU_FLAMECHART_VENDOR;
      }
    }
    return isUnmatched ? toUnmatchColor(color) : color;
  }
}
