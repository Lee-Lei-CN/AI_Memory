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
package com.tcl.tools.profilers.customevent;


import static com.tcl.tools.profilers.ProfilerLayout.createToolbarLayout;

import com.tcl.tools.adtui.TabularLayout;
import com.tcl.tools.adtui.model.Range;
import com.tcl.tools.adtui.model.StreamingTimeline;
import com.tcl.tools.adtui.stdui.StreamingScrollbar;
import com.tcl.tools.adtui.trackgroup.TrackGroupListPanel;
import com.tcl.tools.profilers.ProfilerColors;
import com.tcl.tools.profilers.ProfilerTrackRendererFactory;
import com.tcl.tools.profilers.StageView;
import com.tcl.tools.profilers.StudioProfilers;
import com.tcl.tools.profilers.StudioProfilersView;
import com.google.common.annotations.VisibleForTesting;
import com.intellij.ui.components.JBScrollPane;
import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;

/**
 * This class represents the view of all the custom events that users have chosen to track for Custom Event Visualization.
 */
public class CustomEventProfilerStageView extends StageView<CustomEventProfilerStage> {

  @NotNull
  private final TrackGroupListPanel myTrackGroupList;

  public CustomEventProfilerStageView(@NotNull StudioProfilersView profilersView, @NotNull CustomEventProfilerStage stage) {
    super(profilersView, stage);

    myTrackGroupList = new TrackGroupListPanel(new ProfilerTrackRendererFactory(getProfilersView(), () -> false));
    myTrackGroupList.loadTrackGroups(getStage().getTrackGroupModels());

    // Add a dependency for when the range changes so the track group list has to be repainted as the timeline moves.
    getStage().getTimeline().getViewRange().addDependency(this).onChange(Range.Aspect.RANGE, this::updateTrackGroupList);

    // Add a dependency for when an event has been added so the track group list can be updated
    stage.getUserCounterAspectModel().addDependency(this).onChange(UserCounterAspectModel.Aspect.USER_COUNTER, this::reloadTrackGroup);

    buildUI(stage.getStudioProfilers());
  }

  @Override
  public JComponent getToolbar() {
    // Currently an empty toolbar
    JPanel toolBar = new JPanel(createToolbarLayout());
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(toolBar, BorderLayout.WEST);
    return panel;
  }

  @VisibleForTesting
  @NotNull
  protected final TrackGroupListPanel getTrackGroupList() {
    return myTrackGroupList;
  }

  private void buildUI(StudioProfilers profilers) {
    StreamingTimeline timeline = getStage().getTimeline();

    // The scrollbar can modify the view range of timeline and the tracks.
    getComponent().add(new StreamingScrollbar(timeline, getComponent()), BorderLayout.SOUTH);

    // Two row panel:
    // 1. first row contains the EventMonitor and the tracks for each user event.
    // 2. second row contains the time axis. Time axis will stay pinned when the view window is resized.
    JPanel container = new JPanel(new TabularLayout("*", "*,Fit-"));

    // Main panel containing the interaction trackgroup and user counter trackgroup.
    JPanel mainPanel = new JPanel(new TabularLayout("*"));
    mainPanel.setBackground(ProfilerColors.DEFAULT_BACKGROUND);
    mainPanel.add(myTrackGroupList.getComponent(), new TabularLayout.Constraint(1, 0));
    container.add(new JBScrollPane(mainPanel), new TabularLayout.Constraint(0, 0));

    JComponent timeAxis = buildTimeAxis(profilers);
    container.add(timeAxis, new TabularLayout.Constraint(1, 0));

    getComponent().add(container, BorderLayout.CENTER);
  }

  private void updateTrackGroupList() {
    // Force track group list to validate its children.
    myTrackGroupList.getComponent().updateUI();
  }

  private void reloadTrackGroup() {
    myTrackGroupList.loadTrackGroups(getStage().getTrackGroupModels());
    updateTrackGroupList();
  }
}
