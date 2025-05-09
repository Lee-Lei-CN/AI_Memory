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
package com.tcl.tools.profilers.cpu.analysis;

import com.tcl.tools.adtui.model.Range;
import com.tcl.tools.profilers.cpu.CpuCapture;
import org.jetbrains.annotations.NotNull;

/**
 * Analysis tab model for full capture summary.
 */
public class FullTraceAnalysisSummaryTabModel extends CpuAnalysisSummaryTabModel<CpuCapture> {
  @NotNull private final Range mySelectionRange;

  public FullTraceAnalysisSummaryTabModel(@NotNull Range captureRange, @NotNull Range selectionRange) {
    super(captureRange);
    mySelectionRange = selectionRange;
  }

  @NotNull
  @Override
  public String getLabel() {
    // Full trace contains various data types and thus the view does not display this value.
    return "";
  }

  @NotNull
  @Override
  public Range getSelectionRange() {
    return mySelectionRange;
  }
}
