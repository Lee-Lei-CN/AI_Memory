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
package com.tcl.tools.profilers.event;

import com.android.tools.profiler.proto.Common;
import com.tcl.tools.profilers.ProfilerMonitor;
import com.tcl.tools.profilers.StudioProfiler;
import com.tcl.tools.profilers.StudioProfilers;
import org.jetbrains.annotations.NotNull;

public class EventProfiler extends StudioProfiler {
  public EventProfiler(@NotNull StudioProfilers profilers) {
    super(profilers);
  }

  @Override
  public ProfilerMonitor newMonitor() {
    return new EventMonitor(myProfilers);
  }

  @Override
  public void startProfiling(Common.Session session) {
    // TODO(b/150503095)
    com.android.tools.profiler.proto.EventProfiler.EventStartResponse response =
        myProfilers.getClient().getEventClient().startMonitoringApp(com.android.tools.profiler.proto.EventProfiler.EventStartRequest.newBuilder().setSession(session).build());
  }

  @Override
  public void stopProfiling(Common.Session session) {
    // TODO(b/150503095)
    com.android.tools.profiler.proto.EventProfiler.EventStopResponse response =
        myProfilers.getClient().getEventClient().stopMonitoringApp(com.android.tools.profiler.proto.EventProfiler.EventStopRequest.newBuilder().setSession(session).build());
  }
}
