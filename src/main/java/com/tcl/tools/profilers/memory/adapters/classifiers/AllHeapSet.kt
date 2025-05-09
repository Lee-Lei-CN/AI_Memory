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
package com.tcl.tools.profilers.memory.adapters.classifiers

import com.tcl.tools.profilers.memory.ClassGrouping
import com.tcl.tools.profilers.memory.adapters.CaptureObject
import com.tcl.tools.profilers.memory.adapters.InstanceObject

/**
 * This class implements an all-"heap" that aggregates multiple separate heaps
 */
class AllHeapSet(obj: CaptureObject, private val subHeaps: Array<HeapSet>): HeapSet(obj, NAME, ID) {

  override fun setClassGrouping(classGrouping: ClassGrouping) {
    subHeaps?.forEach { it.setClassGrouping(classGrouping) }
    super.setClassGrouping(classGrouping)
  }

  override fun createSubClassifier() = object : Classifier() {
    override fun isTerminalClassifier() = false
    override fun getClassifierSet(instance: InstanceObject, createIfAbsent: Boolean) =
      subHeaps.find {it.id == instance.heapId}
    override fun getFilteredClassifierSets() = subHeaps.filterNot {it.isEmpty}
    override fun getAllClassifierSets() = subHeaps.toList()
  }

  companion object {
    const val NAME = "All"
    const val ID = -1
  }
}