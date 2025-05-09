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

import com.android.tools.perflib.vmtrace.ClockType;
import com.tcl.tools.adtui.model.Range;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;

public class TopDownTreeModel extends CpuTreeModel<TopDownNode> {
  public TopDownTreeModel(@NotNull ClockType clockType, @NotNull Range range, @NotNull TopDownNode node) {
    super(clockType, range, node);
  }

  @Override
  void expand(@NotNull DefaultMutableTreeNode node) {
  }
}
