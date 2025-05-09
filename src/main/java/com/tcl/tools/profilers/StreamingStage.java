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
package com.tcl.tools.profilers;

import com.tcl.tools.adtui.model.StreamingTimeline;
import com.tcl.tools.profilers.StudioProfilers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A stage that uses a {@link StreamingTimeline}.
 */
public abstract class StreamingStage extends Stage<StreamingTimeline> {
  public StreamingStage(@Nullable StudioProfilers profilers) {
    super(profilers);
  }

  @Nullable
  @Override
  public StreamingTimeline getTimeline() {
    return getStudioProfilers().getTimeline();
  }
}
