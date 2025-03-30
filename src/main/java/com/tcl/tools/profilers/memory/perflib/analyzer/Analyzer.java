package com.tcl.tools.profilers.memory.perflib.analyzer;


import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

public abstract class Analyzer {
    public Analyzer() {
    }

    public abstract boolean accept(CaptureGroup captureGroup);

    public abstract AnalysisReport analyze(CaptureGroup captureGroup, Set<AnalysisReport.Listener> listeners, Set<? extends AnalyzerTask> tasks, Executor taskCompleteExecutor, ExecutorService taskExecutor);

    public abstract void cancel();
}
