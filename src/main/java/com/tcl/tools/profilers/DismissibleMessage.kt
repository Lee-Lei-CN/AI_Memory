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
package com.tcl.tools.profilers

import com.tcl.tools.adtui.common.linkForeground
import com.tcl.tools.adtui.common.secondaryPanelBackground
import com.intellij.ide.BrowserUtil
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBEmptyBorder
import java.awt.BorderLayout
import java.awt.Color
import java.awt.FlowLayout
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.font.TextAttribute
import javax.swing.JPanel
import javax.swing.SwingConstants


object DismissibleMessage {
  /**
   * @param key Key for use in persistent profiler preferences
   */
  @JvmStatic
  @JvmOverloads
  fun of(profilers: StudioProfilers, key: String, message: String, learnMoreLink: String,
         color: Color = secondaryPanelBackground): JPanel =
    of(profilers, key, message, { BrowserUtil.browse(learnMoreLink) }, color)

  @JvmStatic
  @JvmOverloads
  fun of(profilers: StudioProfilers, key: String, message: String, learnMore: () -> Unit,
         color: Color = secondaryPanelBackground): JPanel =
    profilers.ideServices.persistentProfilerPreferences.let { pref ->
      when {
        pref.getBoolean(key, true) -> JBPanel<Nothing>(BorderLayout()).apply {
          fun dismiss() {
            isVisible = false
            pref.setBoolean(key, false)
          }

          val label = JBLabel(message).apply {
            isOpaque = false
            verticalAlignment = SwingConstants.CENTER
            toolTipText = message
          }
          label.addComponentListener(object : ComponentAdapter() {
            val textWidth = getFontMetrics(label.font).stringWidth(message)
            override fun componentResized(e: ComponentEvent) {
              label.toolTipText = message.takeIf { e.component.width <= textWidth }
            }
          })

          val linkPanel = JBPanel<Nothing>(FlowLayout()).apply {
            isOpaque = false
            add(actionLink("Dismiss", ::dismiss))
            add(actionLink("Learn more", learnMore))
          }

          border = JBEmptyBorder(4)
          background = color
          add(label, BorderLayout.CENTER)
          add(linkPanel, BorderLayout.EAST)
        }
        else -> JBPanel<Nothing>()
      }
    }

  private fun actionLink(text: String, performAction: () -> Unit) = JBLabel(text).apply {
    isOpaque = false
    foreground = linkForeground
    verticalAlignment = SwingConstants.CENTER
    font = font.deriveFont(font.attributes + (TextAttribute.UNDERLINE to TextAttribute.UNDERLINE_ON))
    addMouseListener(object : MouseAdapter() {
      override fun mouseClicked(e: MouseEvent) = performAction()
    })
  }
}
