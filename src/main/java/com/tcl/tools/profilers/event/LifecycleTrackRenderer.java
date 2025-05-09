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
package com.tcl.tools.profilers.event;

import com.tcl.tools.adtui.ActivityComponent;
import com.tcl.tools.adtui.model.event.LifecycleEventModel;
import com.tcl.tools.adtui.model.trackgroup.TrackModel;
import com.tcl.tools.adtui.trackgroup.TrackRenderer;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;

/**
 * Track renderer for app lifecycle events, i.e. activities and fragments.
 */
public class LifecycleTrackRenderer
  implements TrackRenderer<LifecycleEventModel> {
  @NotNull
  @Override
  public JComponent render(@NotNull TrackModel<LifecycleEventModel, ?> trackModel) {
    return new ActivityComponent(trackModel.getDataModel());
  }
}
