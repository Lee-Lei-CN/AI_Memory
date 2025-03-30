/*
 * Copyright (C) 2019 The Android Open Source Project
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
package com.tcl.tools.profilers.memory.adapters.instancefilters

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.search.ProjectScope
import com.intellij.psi.search.searches.AllClassesSearch
import com.tcl.tools.profilers.memory.adapters.InstanceObject
import java.util.function.Consumer

/**
 * A filter to locate all instances of classes that directly belong to the currently opened project (excluding dependent libraries).
 */
class ProjectClassesInstanceFilter(project:Project)
  : CaptureObjectInstanceFilter(
  "project classes",
  "Show instances of classes from only the current project.",
  null,
  null,
  makeProjectClassTest(project))

private fun makeProjectClassTest(project: Project) : (InstanceObject) -> Boolean {
  fun getAllProjectClasses(project: Project): Set<String?> {
    val query = AllClassesSearch.search(ProjectScope.getProjectScope(project), project)
    val classNames: MutableSet<String?> = HashSet()
    query.forEach(Consumer { aClass: PsiClass -> classNames.add(aClass.qualifiedName) })
    return classNames
  }
  val projectClasses by lazy { getAllProjectClasses(project) }
  return { inst ->
    var className = inst.classEntry.className

    // Ignore inner classes since they can contain lambdas (e.g. topLevelClass$1). All inner classes should be included anyway if
    // the top-level class belongs to the project.
    val innerClassStartIndex = className.indexOf("$")
    if (innerClassStartIndex != -1) {
      className = className.substring(0, innerClassStartIndex)
    }
    projectClasses.contains(className)
  }
}