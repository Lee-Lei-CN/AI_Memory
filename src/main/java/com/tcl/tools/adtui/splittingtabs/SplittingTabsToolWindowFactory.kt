/*
 * Copyright (C) 2021 The Android Open Source Project
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
package com.tcl.tools.adtui.splittingtabs

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ex.ToolWindowEx
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentManager
import com.tcl.tools.adtui.splittingtabs.SplittingTabsBundle
import com.tcl.tools.adtui.splittingtabs.actions.NewTabAction
import com.tcl.tools.adtui.splittingtabs.state.SplittingTabsStateManager
import com.tcl.tools.adtui.splittingtabs.state.TabState
import com.tcl.tools.adtui.splittingtabs.state.ToolWindowState
import javax.swing.JComponent

abstract class SplittingTabsToolWindowFactory : ToolWindowFactory {
  override fun init(toolWindow: ToolWindow) {
    toolWindow.setToHideOnEmptyContent(true)
  }

  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    val stateManager = SplittingTabsStateManager.getInstance(project)
    stateManager.registerToolWindow(toolWindow)

    val contentManager = toolWindow.contentManager
    (toolWindow as ToolWindowEx).setTabActions(
      NewTabAction(SplittingTabsBundle.lazyMessage("SplittingTabsToolWindow.newTab")) { createNewTab(project, contentManager) })

    val toolWindowState = stateManager.getToolWindowState(toolWindow.id)
    if (toolWindowState.tabStates.isEmpty()) {
      createNewTab(project, contentManager)
    }
    else {
      restoreTabs(project, contentManager, toolWindowState)
    }
  }

  abstract fun generateTabName(tabNames: Set<String>): String

  abstract fun createChildComponent(project: Project, popupActionGroup: ActionGroup, clientState: String?): JComponent

  private fun restoreTabs(project: Project, contentManager: ContentManager, toolwindowState: ToolWindowState) {
    toolwindowState.run {
      tabStates.forEachIndexed { index, state -> createNewTab(project, contentManager, state, index == selectedTabIndex) }
    }
  }

  private fun createNewTab(project: Project, contentManager: ContentManager, tabState: TabState? = null, requestFocus: Boolean = false) {
    val content = createContent(project, contentManager, tabState)
    contentManager.addContent(content)
    contentManager.setSelectedContent(content, requestFocus)
  }

  private fun createContent(project: Project, contentManager: ContentManager, tabState: TabState?): Content {
    val tabName = tabState?.tabName ?: generateTabName(contentManager.contents.mapTo(hashSetOf()) { it.displayName })
    return contentManager.factory.createContent(null, tabName, false).also { content ->
      content.isCloseable = true
      content.component = SplittingPanel.buildComponentFromState(content, tabState?.panelState, object : ChildComponentFactory {
        override fun createChildComponent(state: String?, popupActionGroup: ActionGroup): JComponent =
          createChildComponent(project, popupActionGroup, state)
      })
    }
  }
}