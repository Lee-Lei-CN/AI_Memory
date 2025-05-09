/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.tcl.tools.profilers.memory;

import com.tcl.tools.adtui.model.DefaultDurationData;

public final class GcDurationData extends DefaultDurationData {

  // TODO with JVMTI in the future this will contain proper start/end time + objects reclaimed information.
  public GcDurationData(long duration) {
    super(duration);
  }

  @Override
  public String toString() {
    return String.format("GC Event");
  }
}
