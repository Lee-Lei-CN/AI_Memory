/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.tcl.tools.adtui.eventrenderer;

import com.tcl.tools.adtui.model.event.EventAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class EventIconRenderer<E> implements EventRenderer<E> {
  private static int BORDER_MARGIN = 2;

  @NotNull private Icon myIcon;
  private int myIconWidth;

  public EventIconRenderer(@NotNull Icon icon) {
    myIcon = icon;
    myIconWidth = myIcon.getIconWidth();
  }

  @Override
  public void draw(@NotNull Component parent,
                   @NotNull Graphics2D g2d,
                   @NotNull AffineTransform transform,
                   double length,
                   boolean isMouseOver,
                   EventAction<E> notUsedData) {
    Icon icon = EventRenderer.createImageIconWithBackgroundBorder(myIcon, BORDER_MARGIN, parent.getBackground(), g2d);
    AffineTransform originalTransform = g2d.getTransform();
    g2d.transform(transform);
    icon.paintIcon(parent, g2d, -myIconWidth / 2, 0);
    g2d.setTransform(originalTransform);
  }
}
