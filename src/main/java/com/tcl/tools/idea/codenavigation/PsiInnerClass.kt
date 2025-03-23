/*
 * Copyright (C) 2022 The Android Open Source Project
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
package com.tcl.tools.idea.codenavigation

import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiManager
import com.intellij.psi.util.ClassUtil

/**
 * Uses the class name to navigate to a Java/Kotlin method.
 */
class PsiInnerClass(project: Project) : NavSource {
  private val manager = PsiManager.getInstance(project)

  override fun lookUp(location: CodeLocation, arch: String?): Navigatable? {
    if (location.className.isNullOrEmpty()) {
      return null
    }

    return ClassUtil.findPsiClassByJVMName(manager, location.className!!)
  }
}