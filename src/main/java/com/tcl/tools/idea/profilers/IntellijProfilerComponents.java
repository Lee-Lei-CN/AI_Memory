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
package com.tcl.tools.idea.profilers;

import com.tcl.tools.adtui.stdui.ContentType;
import com.tcl.tools.adtui.stdui.ResizableImage;
import com.tcl.tools.idea.profilers.profilingconfig.CpuProfilingConfigurationsDialog;
import com.tcl.tools.inspectors.commom.api.ide.IntellijContextMenuInstaller;
import com.tcl.tools.inspectors.commom.api.ide.stacktrace.IntelliJStackTraceGroup;
import com.tcl.tools.inspectors.commom.ui.ContextMenuInstaller;
import com.tcl.tools.inspectors.commom.ui.dataviewer.DataViewer;
import com.tcl.tools.inspectors.commom.ui.dataviewer.IntellijDataViewer;
import com.tcl.tools.inspectors.commom.ui.dataviewer.IntellijImageDataViewer;
import com.tcl.tools.inspectors.commom.ui.stacktrace.StackTraceGroup;
import com.tcl.tools.profilers.ExportDialog;
import com.tcl.tools.profilers.IdeProfilerComponents;
import com.tcl.tools.profilers.ImportDialog;
import com.tcl.tools.profilers.UiMessageHandler;
import com.tcl.tools.profilers.analytics.FeatureTracker;
import com.tcl.tools.profilers.cpu.config.CpuProfilerConfigModel;
import com.tcl.tools.profilers.cpu.config.ProfilingConfiguration;
import com.tcl.tools.profilers.stacktrace.LoadingPanel;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBLoadingPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntellijProfilerComponents implements IdeProfilerComponents {

  @NotNull private final Project myProject;

  @NotNull private final FeatureTracker myFeatureTracker;

  public IntellijProfilerComponents(@NotNull Project project, @NotNull FeatureTracker featureTracker) {
    myProject = project;
    myFeatureTracker = featureTracker;
  }

  @NotNull
  @Override
  public LoadingPanel createLoadingPanel(int delayMs) {
    return new LoadingPanel() {
      private final JBLoadingPanel myLoadingPanel = new JBLoadingPanel(new BorderLayout(), myProject, delayMs);

      @NotNull
      @Override
      public JComponent getComponent() {
        return myLoadingPanel;
      }

      @Override
      public void setChildComponent(@Nullable Component comp) {
        myLoadingPanel.getContentPanel().removeAll();
        if (comp != null) {
          myLoadingPanel.add(comp);
        }
      }

      @Override
      public void setLoadingText(@NotNull String loadingText) {
        myLoadingPanel.setLoadingText(loadingText);
      }

      @Override
      public void startLoading() {
        myLoadingPanel.startLoading();
      }

      @Override
      public void stopLoading() {
        myLoadingPanel.stopLoading();
      }
    };
  }

  @NotNull
  @Override
  public StackTraceGroup createStackGroup() {
    return new IntelliJStackTraceGroup(myProject);
  }

  @NotNull
  @Override
  public ContextMenuInstaller createContextMenuInstaller() {
    return new IntellijContextMenuInstaller();
  }

  @NotNull
  @Override
  public ExportDialog createExportDialog() {
    return new IntellijExportDialog(myProject);
  }

  @NotNull
  @Override
  public ImportDialog createImportDialog() {
    return new IntellijImportDialog(myProject);
  }

  @NotNull
  @Override
  public DataViewer createDataViewer(@NotNull byte[] content, @NotNull ContentType contentType, @NotNull DataViewer.Style styleHint) {
    // User isn't expected to specify INVALID; it's only meant as an internal fallback
    assert(styleHint != DataViewer.Style.INVALID);

    if (contentType.isSupportedImageType()) {
      DataViewer viewer = (DataViewer) IntellijImageDataViewer.createImageViewer(content);
      if (viewer == null) {
        viewer = (DataViewer) IntellijDataViewer.createInvalidViewer();
      }
      return viewer;
    }
    if (styleHint == DataViewer.Style.RAW) {
      return (DataViewer) (contentType.isSupportedTextType()
                   ? IntellijDataViewer.createRawTextViewer(content)
                   : IntellijDataViewer.createInvalidViewer());
    }
    else {
      assert (styleHint == DataViewer.Style.PRETTY);
      return (DataViewer) IntellijDataViewer.createPrettyViewerIfPossible(myProject, content, contentType.getFileType());
    }
  }

  @NotNull
  @Override
  public JComponent createResizableImageComponent(@NotNull BufferedImage image) {
    return new ResizableImage(image);
  }

  @NotNull
  @Override
  public UiMessageHandler createUiMessageHandler() {
    return new IntellijUiMessageHandler();
  }

  @Override
  public void openCpuProfilingConfigurationsDialog(@NotNull CpuProfilerConfigModel model, int deviceLevel,
                                                   @NotNull Consumer<ProfilingConfiguration> dialogCallback) {
    CpuProfilingConfigurationsDialog dialog = new CpuProfilingConfigurationsDialog(myProject,
                                                                                   deviceLevel,
                                                                                   model,
                                                                                   dialogCallback,
                                                                                   myFeatureTracker);
    dialog.show();
  }
}
