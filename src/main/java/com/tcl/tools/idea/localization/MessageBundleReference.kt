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
package com.tcl.tools.idea.localization

import com.intellij.AbstractBundle
import com.intellij.reference.SoftReference
import org.jetbrains.annotations.Nls
import java.lang.ref.Reference
import java.util.ResourceBundle
import java.util.function.Supplier

/**
 * Simple helper class useful for creating a message bundle for your module.
 *
 * It creates a soft reference to an underlying text bundle, which means that it can
 * be garbage collected if needed (although it will be reallocated again if you request
 * a new message from it).
 *
 * You might use it like so:
 *
 * ```
 * # In module 'custom'...
 *
 * # resources/messages/CustomBundle.properties:
 * sample.text.key=This is a sample text value.
 *
 * # src/messages/CustomBundle.kt:
 * private const val com.tcl.tools.adtui.splittingtabs.BUNDLE_NAME = "messages.CustomBundle"
 * object CustomBundle {
 *   private val bundleRef = MessageBundleReference(com.tcl.tools.adtui.splittingtabs.BUNDLE_NAME)
 *   fun message(@PropertyKey(resourceBundle = com.tcl.tools.adtui.splittingtabs.BUNDLE_NAME) key: String, vararg params: String) = bundleRef.message(key, *params)
 * }
 * ```
 *
 * That's it! Now you can call `CustomBundle.message("sample.text.key")` to fetch the text value.
 */
class MessageBundleReference(private val name: String) {
  private var bundleRef: Reference<ResourceBundle>? = null

  fun getBundle(): ResourceBundle =
    SoftReference.dereference(bundleRef) ?: ResourceBundle.getBundle(name).also { bundleRef = SoftReference(it) }

  fun message(key: String, vararg params: String) = AbstractBundle.message(getBundle(), key, *params)

  fun lazyMessage(key: String, vararg params: String) = Supplier { message(key, *params) }
}