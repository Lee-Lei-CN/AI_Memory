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
package com.tcl.tools.profilers.memory;

import static com.tcl.tools.profilers.ProfilerLayout.createToolbarLayout;

import com.tcl.tools.profilers.AspectObserver;
import com.tcl.tools.profilers.ProfilerCombobox;
import com.tcl.tools.profilers.ProfilerComboboxCellRenderer;
import com.tcl.tools.profilers.memory.CaptureSelectionAspect;import com.tcl.tools.profilers.memory.MemoryCaptureSelection;import com.tcl.tools.profilers.memory.adapters.CaptureObject;
import com.tcl.tools.profilers.memory.adapters.classifiers.AllHeapSet;
import com.tcl.tools.profilers.memory.adapters.classifiers.HeapSet;
import com.google.common.annotations.VisibleForTesting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.Objects;

public class MemoryHeapView extends AspectObserver {
  @NotNull private final MemoryCaptureSelection mySelection;

  @NotNull private JPanel myHeapToolbar = new JPanel(createToolbarLayout());
  @NotNull private JComboBox<HeapSet> myHeapComboBox = new ProfilerCombobox<>();

  @Nullable private CaptureObject myCaptureObject = null;

  MemoryHeapView(@NotNull MemoryCaptureSelection selection) {
    mySelection = selection;

    mySelection.getAspect().addDependency(this)
      .onChange(CaptureSelectionAspect.CURRENT_LOADING_CAPTURE, this::setNewCapture)
      .onChange(CaptureSelectionAspect.CURRENT_LOADED_CAPTURE, this::updateCaptureState)
      .onChange(CaptureSelectionAspect.CURRENT_HEAP, this::refreshHeap);

    myHeapComboBox.addActionListener(e -> {
      Object item = myHeapComboBox.getSelectedItem();
      if (item instanceof HeapSet) {
        mySelection.selectHeapSet((HeapSet)item);
      }
    });
    myHeapToolbar.add(myHeapComboBox);

    setNewCapture();
    refreshHeap();
  }

  @NotNull
  JComponent getComponent() {
    return myHeapToolbar;
  }

  @VisibleForTesting
  @NotNull
  JComboBox<HeapSet> getHeapComboBox() {
    return myHeapComboBox;
  }

  private void setNewCapture() {
    myCaptureObject = mySelection.getSelectedCapture();
    myHeapComboBox.setModel(new DefaultComboBoxModel<>());
    myHeapComboBox.setRenderer(new HeapListCellRenderer());
    mySelection.selectHeapSet(null); // Clear the heap such that views lower in the hierarchy has a chance to repopulate themselves.
  }

  private void updateCaptureState() {
    CaptureObject captureObject = mySelection.getSelectedCapture();
    if (myCaptureObject != captureObject) {
      return;
    }

    myCaptureObject = captureObject;
    if (myCaptureObject == null) {
      return; // Loading probably failed.
    }

    Collection<HeapSet> heaps = myCaptureObject.getHeapSets();
    HeapSet[] heapsArray = heaps.toArray(new HeapSet[heaps.size()]);
    ComboBoxModel<HeapSet> comboBoxModel = new DefaultComboBoxModel<>(heapsArray);
    myHeapComboBox.setModel(comboBoxModel);

  }

  void refreshHeap() {
    HeapSet heapSet = mySelection.getSelectedHeapSet();
    Object selectedObject = myHeapComboBox.getSelectedItem();
    if (!Objects.equals(heapSet, selectedObject)) {
      myHeapComboBox.setSelectedItem(heapSet);
    }
  }

  private static final class HeapListCellRenderer extends ProfilerComboboxCellRenderer<HeapSet> {

    @Override
    protected void customizeCellRenderer(@NotNull JList<? extends HeapSet> list,
                                         HeapSet value,
                                         int index,
                                         boolean selected,
                                         boolean hasFocus) {
      if (value != null) {
        append("View " + (value instanceof AllHeapSet ? "all heaps" : value.getName() + " heap"));
      }
    }
  }
}
