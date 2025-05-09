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
package com.tcl.tools.profilers.cpu;

import com.tcl.tools.adtui.TabularLayout;
import com.tcl.tools.adtui.chart.hchart.HTreeChart;
import com.tcl.tools.adtui.chart.statechart.StateChart;
import com.tcl.tools.adtui.chart.statechart.StateChartColorProvider;
import com.tcl.tools.adtui.common.EnumColors;
import com.tcl.tools.profilers.AspectObserver;
import com.tcl.tools.adtui.model.MultiSelectionModel;
import com.tcl.tools.adtui.model.Range;
import com.tcl.tools.adtui.model.StateChartModel;
import com.tcl.tools.adtui.model.trackgroup.TrackModel;
import com.tcl.tools.adtui.trackgroup.TrackRenderer;
import com.tcl.tools.adtui.util.SwingUtil;
import com.tcl.tools.idea.codenavigation.CodeNavigator;
import com.tcl.tools.profilers.ProfilerColors;
import com.tcl.tools.profilers.StudioProfilersView;
import com.tcl.tools.profilers.cpu.CaptureNode;import com.tcl.tools.profilers.cpu.ThreadState;import com.tcl.tools.profilers.cpu.VsyncPanel;import com.tcl.tools.profilers.cpu.analysis.CaptureNodeAnalysisModel;
import com.tcl.tools.profilers.cpu.analysis.CpuAnalyzable;
import com.tcl.tools.profilers.cpu.capturedetails.CaptureDetails;
import com.tcl.tools.profilers.cpu.capturedetails.CaptureNodeHRenderer;
import com.tcl.tools.profilers.cpu.capturedetails.CodeNavigationHandler;
import com.tcl.tools.profilers.cpu.systemtrace.CpuSystemTraceData;
import com.intellij.util.ui.UIUtil;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Track renderer for CPU threads in CPU capture stage.
 */
public class CpuThreadTrackRenderer implements TrackRenderer<CpuThreadTrackModel> {
  @NotNull private final AspectObserver myObserver = new AspectObserver();
  @NotNull private final StudioProfilersView myProfilersView;
  private final BooleanSupplier myVsyncEnabler;

  public CpuThreadTrackRenderer(@NotNull StudioProfilersView profilersView, BooleanSupplier vsyncEnabler) {
    myProfilersView = profilersView;
    myVsyncEnabler = vsyncEnabler;
  }

  @NotNull
  @Override
  public JComponent render(@NotNull TrackModel<CpuThreadTrackModel, ?> trackModel) {
    HTreeChart<CaptureNode> traceEventChart = createHChart(trackModel.getDataModel().getCallChartModel(),
                                                           trackModel.getDataModel().getCapture().getRange(),
                                                           trackModel.isCollapsed());
    traceEventChart.setBackground(UIUtil.TRANSPARENT_COLOR);
    traceEventChart.setDrawDebugInfo(
      myProfilersView.getStudioProfilers().getIdeServices().getFeatureConfig().isPerformanceMonitoringEnabled());
    MultiSelectionModel<CpuAnalyzable> multiSelectionModel = trackModel.getDataModel().getMultiSelectionModel();
    multiSelectionModel.addDependency(myObserver).onChange(MultiSelectionModel.Aspect.CHANGE_SELECTION, () -> {
      List<CpuAnalyzable> selection = multiSelectionModel.getSelection();
      if (!selection.isEmpty() && selection.get(0) instanceof CaptureNodeAnalysisModel) {
        // A trace event is selected, possibly in another thread track.
        // Update all tracks so that they render the deselection state (i.e. gray-out) for all of their nodes.
        traceEventChart.setSelectedNode(((CaptureNodeAnalysisModel)selection.get(0)).getNode());
      }
      else {
        // No trace event is selected. Reset all tracks' selection so they render the trace events in their default state.
        traceEventChart.setSelectedNode(null);
      }
    });

    StateChart<ThreadState> threadStateChart = createStateChart(trackModel.getDataModel().getThreadStateChartModel());
    JPanel panel = new JPanel();
    panel.setOpaque(false);
    if (trackModel.isCollapsed() || threadStateChart == null) {
      // Don't show thread states if we don't have the chart for it or if the track is collapsed.
      panel.setLayout(new TabularLayout("*", "*"));
      panel.add(traceEventChart, new TabularLayout.Constraint(0, 0));
    }
    else {
      panel.setLayout(new TabularLayout("*", "8px,*"));
      panel.add(threadStateChart, new TabularLayout.Constraint(0, 0));
      panel.add(traceEventChart, new TabularLayout.Constraint(1, 0));
    }
    if (!trackModel.isCollapsed()) {
      panel.addMouseMotionListener(new MouseAdapter() {
        @Override
        public void mouseMoved(MouseEvent e) {
          if (threadStateChart != null && threadStateChart.contains(e.getPoint())) {
            trackModel.setActiveTooltipModel(trackModel.getDataModel().getThreadStateTooltip());
            threadStateChart.dispatchEvent(e);
          }
          else if (traceEventChart.contains(e.getPoint())) {
            // Translate mouse point to be relative of the tree chart component.
            Point p = e.getPoint();
            p.translate(-traceEventChart.getX(), -traceEventChart.getY());
            CaptureNode node = traceEventChart.getNodeAt(p);
            if (node == null) {
              trackModel.setActiveTooltipModel(null);
            }
            else {
              trackModel.setActiveTooltipModel(trackModel.getDataModel().getTraceEventTooltipBuilder().apply(node));
            }
            traceEventChart.dispatchEvent(SwingUtil.convertMouseEventPoint(e, p));
          }
          else {
            trackModel.setActiveTooltipModel(null);
          }
        }
      });
      panel.addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
          if (traceEventChart.contains(e.getPoint())) {
            // Translate mouse point to be relative of the tree chart component.
            Point p = e.getPoint();
            p.translate(-traceEventChart.getX(), -traceEventChart.getY());
            CaptureNode node = traceEventChart.getNodeAt(p);
            // Trace events only support single-selection.
            if (node != null) {
              multiSelectionModel.setSelection(
                Collections.singleton(new CaptureNodeAnalysisModel(node, trackModel.getDataModel().getCapture())));
            }
            else {
              multiSelectionModel.clearSelection();
            }
            traceEventChart.dispatchEvent(SwingUtil.convertMouseEventPoint(e, p));
          }
        }
      });
    }

    CpuSystemTraceData data = trackModel.getDataModel().getCapture().getSystemTraceData();
    return data == null ? panel :
           VsyncPanel.of(panel,
                         trackModel.getDataModel().getTimeline().getViewRange(),
                         data.getVsyncCounterValues(),
                         myVsyncEnabler);
  }

  @Nullable
  private static StateChart<ThreadState> createStateChart(@NotNull StateChartModel<ThreadState> model) {
    if (model.getSeries().isEmpty()) {
      // No thread state data, don't create chart.
      return null;
    }
    StateChart<ThreadState> threadStateChart = new StateChart<>(model, new CpuThreadColorProvider());
    threadStateChart.setHeightGap(0.0f);
    return threadStateChart;
  }

  private HTreeChart<CaptureNode> createHChart(@NotNull CaptureDetails.CallChart callChartModel,
                                               @NotNull Range captureRange,
                                               boolean isCollapsed) {
    CaptureNode node = callChartModel.getNode();
    Range selectionRange = callChartModel.getRange();

    HTreeChart.Builder<CaptureNode> builder =
      new HTreeChart.Builder<>(node, selectionRange, new CaptureNodeHRenderer(CaptureDetails.Type.CALL_CHART))
        .setGlobalXRange(captureRange)
        .setOrientation(HTreeChart.Orientation.TOP_DOWN)
        .setRootVisible(false)
        .setNodeSelectionEnabled(true);
    if (isCollapsed) {
      return builder.setCustomNodeHeightPx(1).setNodeYPaddingPx(0).build();
    }
    HTreeChart<CaptureNode> chart = builder.build();
    // Add context menu for source navigation.
    if (callChartModel.getCapture().getSystemTraceData() == null) {
      CodeNavigator navigator = myProfilersView.getStudioProfilers().getStage().getStudioProfilers().getIdeServices().getCodeNavigator();
      CodeNavigationHandler handler = new CodeNavigationHandler(chart, navigator);
      chart.addMouseListener(handler);
      myProfilersView.getIdeProfilerComponents().createContextMenuInstaller()
        .installNavigationContextMenu(chart, navigator, handler::getCodeLocation);
    }
    if (node != null) {
      // Force the call chart to update when a filter is applied to the root node. By setting the root to the same node we're not changing
      // the tree model but just triggering a model-changed event.
//      node.getAspectModel().addDependency(myObserver).onChange(CaptureNode.Aspect.FILTER_APPLIED, () -> chart.setHTree(node));
    }
    return chart;
  }

  private static class CpuThreadColorProvider extends StateChartColorProvider<ThreadState> {
    private final EnumColors<ThreadState> myEnumColors = ProfilerColors.THREAD_STATES.build();

    @NotNull
    @Override
    public Color getColor(boolean isMouseOver, @NotNull ThreadState value) {
      myEnumColors.setColorIndex(isMouseOver ? 1 : 0);
      return myEnumColors.getColor(value);
    }
  }
}
