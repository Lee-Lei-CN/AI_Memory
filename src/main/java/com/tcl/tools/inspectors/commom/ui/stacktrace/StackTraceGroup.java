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
package com.tcl.tools.inspectors.commom.ui.stacktrace;

import com.tcl.tools.inspectors.commom.api.stacktrace.StackTraceModel;
import org.jetbrains.annotations.NotNull;

/**
 * A class for creating a list of associated {@link com.android.tools.inspectors.common.ui.stacktrace.StackTraceView}s, so that when any single one
 * of them are interacted with, the other views clear their own state.
 * <p>
 * This is useful to avoid confusing behavior, for example, if you have many related callstack
 * views in a vertical list (e.g. one callstack per event in a list of events). Clicking on one
 * of the views should clear any selections from any others, so only one feels active at a time.
 */
public interface StackTraceGroup {
  @NotNull
  StackTraceView createStackView(@NotNull StackTraceModel model);
}
