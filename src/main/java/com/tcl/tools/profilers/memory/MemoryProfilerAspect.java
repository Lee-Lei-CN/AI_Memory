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

public enum MemoryProfilerAspect {
  TRACKING_ENABLED,
  LIVE_ALLOCATION_STATUS,  // the status of live allocation being ready or not
  LIVE_ALLOCATION_SAMPLING_MODE,
  // These aspects are fired at the start and end of taking heap dump, which are distinct events from
  // parsing and analyzing the heap dump that's already saved
  HEAP_DUMP_STARTED,
  HEAP_DUMP_FINISHED,
}
