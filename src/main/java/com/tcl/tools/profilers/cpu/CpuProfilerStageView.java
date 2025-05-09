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
package com.tcl.tools.profilers.cpu;

import static com.tcl.tools.adtui.common.AdtUiUtils.DEFAULT_VERTICAL_BORDERS;
import static com.tcl.tools.profilers.ProfilerLayout.PROFILING_INSTRUCTIONS_BACKGROUND_ARC_DIAMETER;

import com.android.tools.profilers.RecordingOption;
import com.tcl.tools.adtui.RangeTooltipComponent;
import com.tcl.tools.adtui.TabularLayout;
import com.tcl.tools.adtui.instructions.InstructionsPanel;
import com.tcl.tools.adtui.instructions.TextInstruction;
import com.tcl.tools.adtui.stdui.StreamingScrollbar;
import com.android.tools.profiler.proto.Common;
import com.tcl.tools.profilers.DismissibleMessage;
import com.tcl.tools.profilers.ProfilerColors;
import com.tcl.tools.profilers.ProfilerFonts;
import com.tcl.tools.profilers.ProfilerMode;
import com.tcl.tools.profilers.ProfilerTooltipMouseAdapter;
import com.tcl.tools.profilers.RecordingOptionsView;
import com.tcl.tools.profilers.StageView;
import com.tcl.tools.profilers.StudioProfilersView;
import com.tcl.tools.profilers.SupportLevel;
import com.tcl.tools.profilers.cpu.CpuProfilerContextMenuInstaller;import com.tcl.tools.profilers.cpu.CpuProfilerStageCpuUsageTooltipView;import com.tcl.tools.profilers.cpu.CpuThreadsTooltipView;import com.tcl.tools.profilers.cpu.config.ProfilingConfiguration;
import com.tcl.tools.profilers.cpu.systemtrace.CpuFrameTooltip;
import com.tcl.tools.profilers.cpu.systemtrace.CpuKernelTooltip;
import com.tcl.tools.profilers.event.EventMonitorView;
import com.tcl.tools.profilers.event.LifecycleTooltip;
import com.tcl.tools.profilers.event.LifecycleTooltipView;
import com.tcl.tools.profilers.event.UserEventTooltip;
import com.tcl.tools.profilers.event.UserEventTooltipView;
import com.tcl.tools.profilers.sessions.SessionAspect;
import com.tcl.tools.profilers.sessions.SessionsManager;
import com.google.common.annotations.VisibleForTesting;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.UIUtilities;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseListener;
import java.util.function.Consumer;
import javax.swing.JPanel;
import javax.swing.MutableComboBoxModel;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;

public class CpuProfilerStageView extends StageView<CpuProfilerStage> {
  private enum PanelSizing {
    /**
     * Sizing string for the CPU graph.
     */
    MONITOR("140px", 0),

    /**
     * Sizing string for the threads / kernel view.
     */
    DETAILS("*", 1),

    /**
     * Sizing string for the frames portion of the details view.
     */
    FRAME("Fit", 0),

    /**
     * Sizing string for the kernel portion of the details view.
     */
    KERNEL("Fit", 1),

    /**
     * Sizing string for the threads portion of the details view.
     */
    THREADS("*", 2);

    @NotNull private final String myRowRule;
    private final int myRow;

    PanelSizing(@NotNull String rowRule, int row) {
      myRowRule = rowRule;
      myRow = row;
    }

    @NotNull
    public String getRowRule() {
      return myRowRule;
    }

    public int getRow() {
      return myRow;
    }
  }

  /**
   * Default ratio of splitter. The splitter ratio adjust the first elements size relative to the bottom elements size.
   * A ratio of 1 means only the first element is shown, while a ratio of 0 means only the bottom element is shown.
   */
  private static final float SPLITTER_DEFAULT_RATIO = 0.2f;

  /**
   * When we are showing the kernel data we want to increase the size of the kernel and threads view. This in turn reduces
   * the size of the view used for the CallChart, FlameChart, ect..
   */
  private static final float KERNEL_VIEW_SPLITTER_RATIO = 0.75f;

  private static final String SHOW_PROFILEABLE_MESSAGE = "profileable.cpu.message";

  private final CpuProfilerStage myStage;

  @NotNull private final CpuThreadsView myThreads;
  @NotNull private final CpuKernelsView myCpus;
  @NotNull private final CpuFramesView myFrames;

  /**
   * The action listener of the capture button changes depending on the state of the profiler.
   * It can be either "start capturing" or "stop capturing".
   * This will be null if {@link FeatureConfig::isCpuCaptureStageEnabled}
   */
  @NotNull private final JBSplitter mySplitter;

  @NotNull private final RecordingOptionsView myRecordingOptionsView;

  @NotNull private final RangeTooltipComponent myTooltipComponent;

  @NotNull private final CpuUsageView myUsageView;

  public CpuProfilerStageView(@NotNull StudioProfilersView profilersView, @NotNull CpuProfilerStage stage) {
    super(profilersView, stage);
    myStage = stage;
    myThreads = new CpuThreadsView(myStage);
    myCpus = new CpuKernelsView(myStage);
    myFrames = new CpuFramesView(myStage);
    myUsageView = new CpuUsageView.NormalModeView(myStage);


    myTooltipComponent = new RangeTooltipComponent(getStage().getTimeline(),
                                                   getTooltipPanel(),
                                                   getProfilersView().getComponent(),
                                                   this::shouldShowTooltipSeekComponent);

    getTooltipBinder().bind(CpuProfilerStageCpuUsageTooltip.class, CpuProfilerStageCpuUsageTooltipView::new);
    getTooltipBinder().bind(CpuKernelTooltip.class, (stageView, tooltip) -> new CpuKernelTooltipView(stageView.getComponent(), tooltip));
    getTooltipBinder().bind(CpuThreadsTooltip.class, (stageView, tooltip) -> new CpuThreadsTooltipView(stageView.getComponent(), tooltip));
    getTooltipBinder().bind(CpuFrameTooltip.class, (stageView, tooltip) -> new CpuFrameTooltipView(stageView.getComponent(), tooltip));
    getTooltipBinder().bind(LifecycleTooltip.class, (stageView, tooltip) -> new LifecycleTooltipView(stageView.getComponent(), tooltip));
    getTooltipBinder().bind(UserEventTooltip.class, (stageView, tooltip) -> new UserEventTooltipView(stageView.getComponent(), tooltip));
    getTooltipPanel().setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

    myTooltipComponent.registerListenersOn(myUsageView);
    MouseListener listener = new ProfilerTooltipMouseAdapter(myStage, () -> new CpuProfilerStageCpuUsageTooltip(myStage));
    myUsageView.addMouseListener(listener);

    // "Fit" for the event profiler, "*" for everything else.
    final JPanel details = new JPanel(new TabularLayout("*", "Fit-,*"));
    details.setBackground(ProfilerColors.DEFAULT_STAGE_BACKGROUND);
    // Order matters as such our tooltip component should be first so it draws on top of all elements.
    details.add(myTooltipComponent, new TabularLayout.Constraint(0, 0, 3, 1));

    if (stage.getStudioProfilers().getSelectedSessionSupportLevel() == SupportLevel.DEBUGGABLE) {
      final EventMonitorView eventsView = new EventMonitorView(profilersView, stage.getEventMonitor());
      eventsView.registerTooltip(myTooltipComponent, getStage());
      details.add(eventsView.getComponent(), new TabularLayout.Constraint(0, 0));
    }

    TabularLayout mainLayout = new TabularLayout("*");
    mainLayout.setRowSizing(PanelSizing.MONITOR.getRow(), PanelSizing.MONITOR.getRowRule());
    mainLayout.setRowSizing(PanelSizing.DETAILS.getRow(), PanelSizing.DETAILS.getRowRule());
    final JPanel mainPanel = new JBPanel(mainLayout);
    mainPanel.setBackground(ProfilerColors.DEFAULT_STAGE_BACKGROUND);

    mainPanel.add(myUsageView, new TabularLayout.Constraint(PanelSizing.MONITOR.getRow(), 0));
    mainPanel.add(createCpuStatePanel(), new TabularLayout.Constraint(PanelSizing.DETAILS.getRow(), 0));

    // Panel that represents all of L2
    details.add(mainPanel, new TabularLayout.Constraint(1, 0));
    details.add(buildTimeAxis(myStage.getStudioProfilers()), new TabularLayout.Constraint(3, 0));
    details.add(new StreamingScrollbar(myStage.getTimeline(), details), new TabularLayout.Constraint(4, 0));

    // The first component in the splitter is the recording options, the 2nd component is the L2 components.
    myRecordingOptionsView = new RecordingOptionsView(getStage().getRecordingModel(), this::editConfigurations);
    mySplitter = new JBSplitter(false);
    mySplitter.setFirstComponent(myRecordingOptionsView);
    mySplitter.setSecondComponent(details);
    mySplitter.getDivider().setBorder(DEFAULT_VERTICAL_BORDERS);
    mySplitter.setProportion(SPLITTER_DEFAULT_RATIO);
    getComponent().add(mySplitter, BorderLayout.CENTER);

    CpuProfilerContextMenuInstaller.install(myStage, getIdeComponents(), myUsageView, getComponent());
    // Add the profilers common menu items
    getProfilersView().installCommonMenuItems(myUsageView);

    SessionsManager sessions = getStage().getStudioProfilers().getSessionsManager();
    sessions.addDependency(this).onChange(SessionAspect.SELECTED_SESSION, this::sessionChanged);
    sessions.addDependency(this).onChange(SessionAspect.PROFILING_SESSION, this::sessionChanged);

    if (!getStage().hasUserUsedCpuCapture()) {
      installProfilingInstructions(myUsageView);
    }
    sessionChanged();
  }

  @Override
  public JPanel getToolbar() {
    return getStage().getStudioProfilers().getSelectedSessionSupportLevel() == SupportLevel.PROFILEABLE
           ? DismissibleMessage.of(getStage().getStudioProfilers(),
                                   SHOW_PROFILEABLE_MESSAGE,
                                   "Some features are disabled for profileable processes.",
                                   SupportLevel.DOC_LINK)
           : new JPanel();
  }

  private void sessionChanged() {
    boolean sessionAlive = SessionsManager.isSessionAlive(getStage().getStudioProfilers().getSessionsManager().getSelectedSession());
    myRecordingOptionsView.setEnabled(sessionAlive);
  }

  private Unit editConfigurations(MutableComboBoxModel<RecordingOption> model) {
    Consumer<ProfilingConfiguration> dialogCallback = (configuration) -> {
      // Update the config list to pick up any potential changes.
      myStage.getProfilerConfigModel().updateProfilingConfigurations();
      myStage.refreshRecordingConfigurations();
    };
    Common.Device selectedDevice = myStage.getStudioProfilers().getDevice();
    int deviceFeatureLevel = selectedDevice != null ? selectedDevice.getFeatureLevel() : 0;
    getIdeComponents().openCpuProfilingConfigurationsDialog(myStage.getProfilerConfigModel(), deviceFeatureLevel, dialogCallback);
    myStage.getStudioProfilers().getIdeServices().getFeatureTracker().trackOpenProfilingConfigDialog();
    return Unit.INSTANCE;
  }

  @NotNull
  private JPanel createCpuStatePanel() {
    TabularLayout cpuStateLayout = new TabularLayout("*");
    JPanel cpuStatePanel = new JBPanel(cpuStateLayout);

    cpuStatePanel.setBackground(ProfilerColors.DEFAULT_STAGE_BACKGROUND);
    cpuStateLayout.setRowSizing(PanelSizing.FRAME.getRow(), PanelSizing.FRAME.getRowRule());
    cpuStateLayout.setRowSizing(PanelSizing.KERNEL.getRow(), PanelSizing.KERNEL.getRowRule());
    cpuStateLayout.setRowSizing(PanelSizing.THREADS.getRow(), PanelSizing.THREADS.getRowRule());

    //region CpuThreadsView
    myTooltipComponent.registerListenersOn(myThreads.getComponent());
    cpuStatePanel.add(myThreads.getComponent(), new TabularLayout.Constraint(PanelSizing.THREADS.getRow(), 0));
    //endregion

    //region CpuKernelsView
    myCpus.getComponent().addComponentListener(new ComponentAdapter() {
      // When the CpuKernelModel is updated we adjust the splitter. The higher the number the more space
      // the first component occupies. For when we are showing Kernel elements we want to take up more space
      // than when we are not. As such each time we modify the CpuKernelModel (when a trace is selected) we
      // adjust the proportion of the splitter accordingly.

      @Override
      public void componentShown(ComponentEvent e) {
        mySplitter.setProportion(KERNEL_VIEW_SPLITTER_RATIO);
      }

      @Override
      public void componentHidden(ComponentEvent e) {
        mySplitter.setProportion(SPLITTER_DEFAULT_RATIO);
      }
    });
    myTooltipComponent.registerListenersOn(myCpus.getComponent());
    cpuStatePanel.add(myCpus.getComponent(), new TabularLayout.Constraint(PanelSizing.KERNEL.getRow(), 0));
    //endregion

    //region CpuFramesView
    myTooltipComponent.registerListenersOn(myFrames.getComponent());
    cpuStatePanel.add(myFrames.getComponent(), new TabularLayout.Constraint(PanelSizing.FRAME.getRow(), 0));
    //endregion

    return cpuStatePanel;
  }

  private void installProfilingInstructions(@NotNull JPanel parent) {
    assert parent.getLayout().getClass() == TabularLayout.class;
    FontMetrics metrics = UIUtilities.getFontMetrics(parent, ProfilerFonts.H2_FONT);
    InstructionsPanel panel =
      new InstructionsPanel.Builder(new TextInstruction(metrics, "Click Record to start capturing CPU activity"))
        .setEaseOut(myStage.getInstructionsEaseOutModel(), instructionsPanel -> parent.remove(instructionsPanel))
        .setBackgroundCornerRadius(PROFILING_INSTRUCTIONS_BACKGROUND_ARC_DIAMETER, PROFILING_INSTRUCTIONS_BACKGROUND_ARC_DIAMETER)
        .build();
    // Add the instructions panel as the first component of |parent|, so that |parent| renders the instructions on top of other components.
    parent.add(panel, new TabularLayout.Constraint(0, 0), 0);
  }

  private void updateCaptureViewVisibility() {
    if (myStage.getProfilerMode() == ProfilerMode.EXPANDED) {
      mySplitter.setFirstComponent(myRecordingOptionsView);
    }
  }

  /**
   * @return true if the blue seek component from {@link RangeTooltipComponent} should be visible.
   * @see {@link RangeTooltipComponent#myShowSeekComponent}
   */
  @VisibleForTesting
  boolean shouldShowTooltipSeekComponent() {
    return myStage.getTooltip() instanceof CpuProfilerStageCpuUsageTooltip && myUsageView.shouldShowTooltipSeekComponent();
  }
}