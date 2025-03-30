package com.tcl.tools.profilers.memory.perflib.analyzer;


public abstract class CaptureAnalyzer {
    public CaptureAnalyzer() {
    }

    public abstract boolean accept(CaptureGroup captureGroup);

    public abstract AnalysisReport analyze(CaptureGroup captureGroup);
}
