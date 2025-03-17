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
package com.tcl.tools.adtui.splittingtabs.actions

import com.intellij.ui.content.Content
import com.tcl.tools.adtui.splittingtabs.SplitOrientation
import com.tcl.tools.adtui.splittingtabs.findFirstSplitter

@Suppress("ComponentNotRegistered")
internal sealed class SplitAction(private val orientation: SplitOrientation)
  : SplittingTabsContextMenuAction(orientation.text) {

  init {
    templatePresentation.icon = orientation.icon
  }

  override fun actionPerformed(content: Content) {
    content.findFirstSplitter()?.split(orientation)
  }

  class Vertical : SplitAction(SplitOrientation.VERTICAL)

  class Horizontal : SplitAction(SplitOrientation.HORIZONTAL)
}