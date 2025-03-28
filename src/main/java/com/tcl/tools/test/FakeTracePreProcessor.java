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
package com.tcl.tools.test;

import com.android.tools.idea.protobuf.ByteString;
import com.android.tools.profilers.cpu.TracePreProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FakeTracePreProcessor implements TracePreProcessor {

  private boolean myTracePreProcessed = false;

  private boolean myFailedToPreProcess = false;

  private List<String> mySymbolDirs;

  @Override
  public ByteString preProcessTrace(@NotNull ByteString trace, @NotNull List<String> symbolDirs) {
    myTracePreProcessed = true;
    mySymbolDirs = symbolDirs;
    return myFailedToPreProcess ? TracePreProcessor.FAILURE : trace;
  }

  public List<String> getSymbolDirs() { return mySymbolDirs; }

  public boolean isTracePreProcessed() {
    return myTracePreProcessed;
  }

  public void setFailedToPreProcess(boolean failedToPreProcess) {
    myFailedToPreProcess = failedToPreProcess;
  }
}
