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
package com.tcl.tools.idea;

import com.intellij.openapi.util.Key;

public final class FileEditorUtil {
  private FileEditorUtil() {
  }

  /**
   * Boolean {@link Key} to set on a {@code FileEditor} instance to prevent the
   * {@code GeneratedFileNotificationProvider} from displaying a notification
   * that files under the "build" directory should not be edited.
   */
  public static final Key<Boolean> DISABLE_GENERATED_FILE_NOTIFICATION_KEY =
    Key.create(FileEditorUtil.class.getName() + ".generated.file.notification.disable");
}
