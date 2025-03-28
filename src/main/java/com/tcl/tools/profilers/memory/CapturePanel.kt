/*
 * Copyright (C) 2020 The Android Open Source Project
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
package com.tcl.tools.profilers.memory

import com.tcl.tools.profilers.AspectObserver
import com.intellij.ui.components.JBTabbedPane
import com.intellij.util.ui.JBEmptyBorder
import com.intellij.util.ui.JBUI
import com.tcl.tools.adtui.FilterComponent
import com.tcl.tools.adtui.StatLabel
import com.tcl.tools.adtui.common.AdtUiUtils
import com.tcl.tools.adtui.flat.FlatSeparator
import com.tcl.tools.adtui.model.FpsTimer
import com.tcl.tools.adtui.model.Range
import com.tcl.tools.adtui.model.StreamingTimeline
import com.tcl.tools.adtui.model.updater.Updater
import com.tcl.tools.inspectors.commom.api.ide.IntellijContextMenuInstaller
import com.tcl.tools.inspectors.commom.ui.ContextMenuInstaller
import com.tcl.tools.profilers.IdeProfilerComponents
import com.tcl.tools.profilers.ProfilerFonts
import com.tcl.tools.profilers.ProfilerLayout.*
import com.tcl.tools.profilers.memory.adapters.HeapDumpCaptureObject
import com.tcl.tools.profilers.memory.adapters.NativeAllocationSampleCaptureObject
import com.tcl.tools.profilers.memory.adapters.classifiers.HeapSet
import com.tcl.tools.profilers.memory.chart.MemoryVisualizationView
import icons.StudioIcons
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class CapturePanel(selection: MemoryCaptureSelection,
                   selectionTimeLabel: JLabel?,
                   selectionRange: Range,
                   isFullScreenHeapDumpUi: Boolean): AspectObserver() {
  val contextMenuInstaller = IntellijContextMenuInstaller()
  val timeline = StreamingTimeline(Updater(FpsTimer()))
  val heapView = MemoryHeapView(selection)
  val captureView = MemoryCaptureView(selection) // TODO: remove after full migration. Only needed for legacy tests
  val classGrouping = MemoryClassGrouping(selection)
  val classifierView = MemoryClassifierView(selection, contextMenuInstaller)
  val classSetView = MemoryClassSetView(selection, contextMenuInstaller, selectionRange, timeline)
  val instanceDetailsView = MemoryInstanceDetailsView(selection, contextMenuInstaller, timeline)

  val captureInfoMessage = JLabel().apply {
    border = TOOLBAR_ICON_BORDER
    // preset the minimize size of the info to only show the icon, so the text can be truncated when the user resizes the vertical splitter.
    minimumSize = preferredSize
    isVisible = false
    selection.aspect.addDependency(this@CapturePanel)
      .onChange(CaptureSelectionAspect.CURRENT_HEAP_CONTENTS) {
        when (val infoMessage = selection.selectedCapture?.infoMessage) {
          null -> isVisible = false
          else -> {
            isVisible = true
            text = infoMessage
            toolTipText = infoMessage
          }
        }
      }
  }

  private val filterComponent =
    FilterComponent(FILTER_TEXT_FIELD_WIDTH, FILTER_TEXT_HISTORY_SIZE, FILTER_TEXT_FIELD_TRIGGER_DELAY_MS).apply {
      model.setFilterHandler(selection.filterHandler)
      border = JBEmptyBorder(0, 4, 0, 0)
    }

  val component: JPanel =
    CapturePanelUi(selection, heapView, classGrouping, classifierView, filterComponent, captureInfoMessage)
}

/**
 * Helper class to maintain toolbar components between tabs.
 * One copy of components is maintained to preserve state and manage the selected heap.
 * This provides for a seamless user experience when doing things like filtering.
 * The caveat is components can only be added to one panel at a time. To work around
 * this a list of toolbar components is collected for each tab. When that tab is activated
 * the list of components is added to the selected tab.
 */
private data class ToolbarComponents(val toolbarPanel: JPanel,
                                     val components: List<Component>)

private class CapturePanelUi(private val selection: MemoryCaptureSelection,
                             private val heapView: MemoryHeapView,
                             private val classGrouping: MemoryClassGrouping,
                             private val classifierView: MemoryClassifierView,
                             private val filterComponent: FilterComponent,
                             private val captureInfoMessage: JLabel)
  : JPanel(BorderLayout()) {
  private val observer = AspectObserver()
  private val instanceFilterMenu = MemoryInstanceFilterMenu(selection)
  private val toolbarTabPanels = mutableMapOf<String, ToolbarComponents>()
  private val tabListeners = mutableListOf<CapturePanelTabContainer>()
  private val visualizationView = MemoryVisualizationView(selection)
  private var activeTabIndex = 0

  init {
    val headingPanel = JPanel().apply {
      layout = BoxLayout(this, BoxLayout.Y_AXIS)
      add(buildSummaryPanel())
    }
    add(headingPanel, BorderLayout.PAGE_START)
    add(buildDetailsPanel(headingPanel), BorderLayout.CENTER)
  }

  private fun buildDetailsPanel(headingPanel: JPanel) = JPanel(BorderLayout()).apply {
    fun refreshPanel() {
      removeAll()
      if (selection.selectedCapture is HeapDumpCaptureObject) {
        val toolbarPanel = JPanel(createToolbarLayout())
        toolbarDefaults().forEach { toolbarPanel.add(it) }
        headingPanel.add(buildToolbarPanel(toolbarPanel), 0)
        add(classifierView.component)
      }
      else {
        add(buildTabPanel(), BorderLayout.CENTER)
      }
    }
    selection.aspect.addDependency(observer).onChange(CaptureSelectionAspect.CURRENT_LOADED_CAPTURE, ::refreshPanel)
    refreshPanel()
  }

  private fun buildNonTabPanel(toolbar: JPanel, component: JComponent) = JPanel(BorderLayout()).apply {
    add(buildToolbarPanel(toolbar), BorderLayout.PAGE_START)
    add(component, BorderLayout.CENTER)
  }

  // Add the right side toolbar so that it is on top of the truncated |myCaptureInfoMessage|.
  private fun buildTabPanel() = JBTabbedPane().apply {
    addTab(this, "Table", classifierView, toolbarDefaults())
    addTab(this, "Visualization", visualizationView, mutableListOf(visualizationView.toolbarComponents, toolbarCore()).flatten())
    fun updateTabs() {
      // do move which panel the tabs bar appears on.
      tabListeners[activeTabIndex].onSelectionChanged(false)
      val title = getTitleAt(selectedIndex)
      val panel = toolbarTabPanels[title]!!.toolbarPanel
      panel.removeAll()
      toolbarTabPanels[title]!!.components.forEach { panel.add(it) }
      tabListeners[selectedIndex].onSelectionChanged(true)
      activeTabIndex = selectedIndex
    }
    addChangeListener { updateTabs() }
    updateTabs()
  }

  private fun addTab(tabPane: JBTabbedPane, name: String, tabContainer: CapturePanelTabContainer, toolbarComponents: List<Component>) {
    toolbarTabPanels[name] = ToolbarComponents(JPanel(createToolbarLayout()), toolbarComponents)
    tabListeners.add(tabContainer)
    val body = buildNonTabPanel(toolbarTabPanels[name]!!.toolbarPanel, tabContainer.component)
    tabPane.add(name, body)
    body.border = JBUI.Borders.empty() // undo insets added by `JBTabbedPane.addTab`
  }

  private fun toolbarDefaults() = mutableListOf<Component>().apply {
    if (!(selection.selectedCapture is NativeAllocationSampleCaptureObject)) {
      add(heapView.component)
    }
    add(classGrouping.component)
    addAll(toolbarCore())
  }

  private fun toolbarCore() = mutableListOf<Component>().apply {
    add(instanceFilterMenu.component)
    add(filterComponent)
    add(captureInfoMessage)
  }

  private fun buildToolbarPanel(toolbar: JPanel) = JPanel(BorderLayout()).apply {
    add(toolbar, BorderLayout.LINE_START)
    alignmentX = Component.LEFT_ALIGNMENT
    minimumSize = Dimension(0, minimumSize.height)
  }

  private fun buildSummaryPanel() = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
    fun mkLabel(desc: String, action: Runnable? = null) =
      StatLabel(0L, desc, numFont = ProfilerFonts.H2_FONT, descFont = AdtUiUtils.DEFAULT_FONT.biggerOn(1f), action = action)
    val totalClassLabel = mkLabel("Classes")
    val totalLeakLabel = mkLabel("Leaks", action = Runnable(::showLeaks))
    val totalCountLabel = mkLabel("Count")
    val totalNativeSizeLabel = mkLabel("Native Size")
    val totalShallowSizeLabel = mkLabel("Shallow Size")
    val totalRetainedSizeLabel = mkLabel("Retained Size")

    // Compute total classes asynchronously because it can take multiple seconds
    fun refreshTotalClassesAsync(heap: HeapSet)  {
      // Handle "no filter" case specially, because it recomputes from the current instance stream,
      // and `ClassifierSet` only considers instances as "matched" if the filter is not empty.
      // This is analogous to how `MemoryClassifierView` is checking if filter is empty to treat it specially
      val filterMatches = if (selection.filterHandler.filter.isEmpty) heap.instancesStream else heap.filterMatches
      // Totals other than class count don't need this, because they are direct fields initialized correctly
      val count = filterMatches.mapToLong { it.classEntry.classId }.distinct().count()
      totalClassLabel.numValue = count
    }

    fun refreshSummaries() {
      selection.selectedHeapSet?.let { heap ->
        refreshTotalClassesAsync(heap)
        totalCountLabel.numValue = heap.totalObjectCount.toLong()
        totalNativeSizeLabel.numValue = heap.totalNativeSize
        totalShallowSizeLabel.numValue = heap.totalShallowSize
        totalRetainedSizeLabel.numValue = heap.totalRetainedSize

        selection.selectedCapture?.let { capture ->
          isVisible = capture is HeapDumpCaptureObject
          when (val filter = capture.activityFragmentLeakFilter) {
            null -> totalLeakLabel.isVisible = false
            else -> totalLeakLabel.apply {
              val leakCount = heap.getInstanceFilterMatchCount(filter).toLong()
              isVisible = true
              numValue = leakCount
              icon = if (leakCount > 0) StudioIcons.Common.WARNING else null
            }
          }
        }
      }
    }

    selection.aspect.addDependency(observer)
      .onChange(CaptureSelectionAspect.CURRENT_HEAP_CONTENTS, ::refreshSummaries)
      .onChange(CaptureSelectionAspect.CURRENT_FILTER, ::refreshSummaries)

    add(totalClassLabel)
    add(totalLeakLabel)
    add(FlatSeparator(6, 36))
    add(totalCountLabel)
    add(totalNativeSizeLabel)
    add(totalShallowSizeLabel)
    add(totalRetainedSizeLabel)
    alignmentX = Component.LEFT_ALIGNMENT
  }

  private fun showLeaks() {
    selection.selectedCapture?.activityFragmentLeakFilter?.let {
      instanceFilterMenu.component.selectedItem = it
    }
  }
}