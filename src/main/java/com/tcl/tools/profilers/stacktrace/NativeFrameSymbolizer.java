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
package com.tcl.tools.profilers.stacktrace;

import com.android.tools.profiler.proto.Memory;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for resolving native frames for the profilers.
 */
public interface NativeFrameSymbolizer {
  @NotNull
  Memory.NativeCallStack.NativeFrame symbolize(String abi, Memory.NativeCallStack.NativeFrame unsymbolizedFrame);

  /**
   * Stop / Cleanup any processes or state created by the symbolizer. This is a call made outside the symbolize function as an optimization.
   * It is the callers responsibility to call stop when done processing symbols allowing the symbolizer to reset state.
   */
  void stop();
}
