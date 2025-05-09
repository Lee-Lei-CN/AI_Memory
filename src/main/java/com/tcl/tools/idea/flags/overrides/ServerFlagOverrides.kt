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
package com.tcl.tools.idea.flags.overrides

import com.android.flags.Flag
import com.android.flags.ImmutableFlagOverrides
import com.android.tools.idea.serverflags.ServerFlagService

/*
ServerFlagOverrides is used to override StudioFlags from
the server. The server flag name is equal to the studio
flag name prefaced by "studio_flags/".
 */
class ServerFlagOverrides : ImmutableFlagOverrides {

  override fun get(flag: Flag<*>): String? {
    val service = ServerFlagService.instance
    if (!service.initialized) {
      return null
    }

    val id = flag.id
    return service.getBoolean("studio_flags/$id")?.toString()
  }
}