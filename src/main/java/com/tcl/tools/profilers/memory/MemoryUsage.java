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

import com.tcl.tools.adtui.model.LineChartModel;
import com.tcl.tools.adtui.model.Range;
import com.tcl.tools.adtui.model.RangedContinuousSeries;
import com.tcl.tools.adtui.model.SeriesData;
import com.android.tools.profiler.proto.Common;
import com.android.tools.profiler.proto.MemoryProfiler.MemoryData.MemorySample;
import com.android.tools.profiler.proto.MemoryServiceGrpc;
import com.android.tools.profiler.proto.TransportServiceGrpc;
import com.tcl.tools.profilers.StudioProfilers;
import com.tcl.tools.profilers.UnifiedEventDataSeries;
import java.util.List;
import java.util.function.Function;
import com.tcl.tools.profilers.memory.MemoryDataSeries;import org.jetbrains.annotations.NotNull;

public class MemoryUsage extends LineChartModel {

  @NotNull private final Range myMemoryRange;
  @NotNull private final RangedContinuousSeries myTotalMemorySeries;
  static final int KB_TO_B = 1024;

  public MemoryUsage(@NotNull StudioProfilers profilers) {
    myMemoryRange = new Range(0, 0);
    myTotalMemorySeries = createRangedSeries(profilers, getTotalSeriesLabel(), myMemoryRange,
                                             UnifiedEventDataSeries.DEFAULT_GROUP_ID,
                                             UnifiedEventDataSeries
                                               .fromFieldToDataExtractor(e -> (long)e.getMemoryUsage().getTotalMem()*KB_TO_B));
    add(myTotalMemorySeries);
  }

  protected RangedContinuousSeries createLegacyRangedSeries(@NotNull StudioProfilers profilers,
                                                            @NotNull String name,
                                                            @NotNull Range range,
                                                            @NotNull Function<MemorySample, Long> getter) {
    MemoryServiceGrpc.MemoryServiceBlockingStub client = profilers.getClient().getMemoryClient();
    MemoryDataSeries series = new MemoryDataSeries(client, profilers.getSession(), getter);
    return new RangedContinuousSeries(name, profilers.getTimeline().getViewRange(), range, series, profilers.getTimeline().getDataRange());
  }

  protected RangedContinuousSeries createRangedSeries(@NotNull StudioProfilers profilers,
                                                      @NotNull String name,
                                                      @NotNull Range range,
                                                      int groupId,
                                                      Function<List<Common.Event>, List<SeriesData<Long>>> dataExtractor) {
    TransportServiceGrpc.TransportServiceBlockingStub client = profilers.getClient().getTransportClient();
    UnifiedEventDataSeries<Long> series = new UnifiedEventDataSeries<>(client,
                                                                       profilers.getSession().getStreamId(),
                                                                       profilers.getSession().getPid(),
                                                                       Common.Event.Kind.MEMORY_USAGE,
                                                                       groupId,
                                                                       dataExtractor);
    return new RangedContinuousSeries(name, profilers.getTimeline().getViewRange(), range, series, profilers.getTimeline().getDataRange());
  }

  @NotNull
  public Range getMemoryRange() {
    return myMemoryRange;
  }

  @NotNull
  public RangedContinuousSeries getTotalMemorySeries() {
    return myTotalMemorySeries;
  }

  protected String getTotalSeriesLabel() {
    return "";
  }
}
