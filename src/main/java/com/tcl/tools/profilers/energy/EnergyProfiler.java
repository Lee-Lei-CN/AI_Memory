// Copyright (C) 2017 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.tcl.tools.profilers.energy;

import com.android.tools.profiler.proto.Commands;
import com.android.tools.profiler.proto.Common;
import com.tcl.tools.profilers.ProfilerMonitor;
import com.tcl.tools.profilers.StudioProfiler;
import com.tcl.tools.profilers.StudioProfilers;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

public class EnergyProfiler extends StudioProfiler {
  private static Logger getLogger() {
    return Logger.getInstance(EnergyProfiler.class);
  }

  public EnergyProfiler(@NotNull StudioProfilers profilers) {
    super(profilers);
  }

  @Override
  public ProfilerMonitor newMonitor() {
    return new EnergyMonitor(myProfilers);
  }

  @Override
  public void startProfiling(Common.Session session) {
    // TODO(b/150503095)
    com.android.tools.profiler.proto.EnergyProfiler.EnergyStartResponse startResponse =
        myProfilers.getClient().getEnergyClient().startMonitoringApp(com.android.tools.profiler.proto.EnergyProfiler.EnergyStartRequest.newBuilder().setSession(session).build());

    if (myProfilers.getIdeServices().getFeatureConfig().isUnifiedPipelineEnabled()) {
      // Issue GetCpuCoreConfig command once so we can calculate CPU energy usage.
      // We need the device ID to run the command, but there has been a report (b/146037091) that 'myProfilers.getDevice()' may
      // be null in release build. Therefore we use if to guard the use of the device to avoid NPE, instead of assert.
      if (myProfilers.getDevice() != null) {
        // CPU frequency files may not always be available (e.g. emulator), in which case we still have a fallback model to use from
        // DefaultPowerProfile.
        myProfilers.getClient().executeAsync(
          Commands.Command.newBuilder()
            .setStreamId(session.getStreamId())
            .setPid(session.getPid())
            .setType(Commands.Command.CommandType.GET_CPU_CORE_CONFIG)
            .setGetCpuCoreConfig(Commands.GetCpuCoreConfig.newBuilder().setDeviceId(myProfilers.getDevice().getDeviceId())).build(),
          myProfilers.getIdeServices().getPoolExecutor());
      } else {
        getLogger().warn("Unable to retrieve CPU frequency files; device ID unknown.");
      }
    }
  }

  @Override
  public void stopProfiling(Common.Session session) {
    // TODO(b/150503095)
    com.android.tools.profiler.proto.EnergyProfiler.EnergyStopResponse response =
        myProfilers.getClient().getEnergyClient().stopMonitoringApp(com.android.tools.profiler.proto.EnergyProfiler.EnergyStopRequest.newBuilder().setSession(session).build());
  }
}
