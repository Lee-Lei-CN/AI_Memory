/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.tcl.tools.adtui.model;

import com.tcl.tools.profilers.AspectModel;
import com.tcl.tools.adtui.model.updater.Updatable;
import com.tcl.tools.adtui.model.updater.Updater;
import org.jetbrains.annotations.NotNull;

import static com.tcl.tools.adtui.model.updater.Updater.DEFAULT_LERP_FRACTION;

/**
 * Controls an ease-out value based on elasped time. The easing starts at a user-specified time after an instance is created, after which
 * the ease-out kicks in over a fixed duration relative to the {@link Updater}'s update rate.
 */
public class EaseOutModel extends AspectModel<EaseOutModel.Aspect> implements Updatable {

  public enum Aspect {
    EASING
  }

  @NotNull private final Updater myUpdater;
  private final long myEaseOutStartTimeNs;
  private long myTimeRemainingUntilEaseOut;
  private float myPercentage;

  /**
   * @param updater            The updater that animates the easing effect.
   * @param easeOutStartTimeNs Time after which the easing effect starts - has to be >= 0.
   */
  public EaseOutModel(@NotNull Updater updater, long easeOutStartTimeNs) {
    assert easeOutStartTimeNs >= 0;

    myUpdater = updater;
    myPercentage = 0f;
    myEaseOutStartTimeNs = easeOutStartTimeNs;
    myTimeRemainingUntilEaseOut = myEaseOutStartTimeNs;
    myUpdater.register(this);
  }

  public void setCurrentPercentage(float percentage) {
    assert percentage >= 0 && percentage <= 1;
    myPercentage = percentage;
    changed(Aspect.EASING);

    if (myPercentage >= 1) {
      myUpdater.unregister(this);
    }
  }

  @Override
  public void update(long elapsedNs) {
    myTimeRemainingUntilEaseOut -= elapsedNs;
    if (myTimeRemainingUntilEaseOut < 0) {
      myPercentage = Updater.lerp(myPercentage, 1, DEFAULT_LERP_FRACTION, elapsedNs, 0.01f);
    }
    changed(Aspect.EASING);

    if (myPercentage >= 1) {
      // Stops updating completely after we have reached full fade out.
      myUpdater.unregister(this);
    }
  }

  /**
   * @return a [0,1] value indicating the current easing progress. A value of 1 means the easing has completed.
   */
  public float getPercentageComplete() {
    return myPercentage;
  }
}
