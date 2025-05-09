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
package com.tcl.tools.adtui;

import com.intellij.util.ui.ButtonlessScrollBarUI;

/**
 * The default ButtonlessScrollBarUI contains logic to overlay the scrollbar
 * and fade in/out on top of a JScrollPane. Because we are using the scrollbar
 * without a scroll pane, it can fade in and out unexpectedly. This subclass
 * simply disables the overlay feature.
 */
public class RangeScrollBarUI extends ButtonlessScrollBarUI {
  @Override
  protected boolean isMacOverlayScrollbar() {
      return false;
    }
}
