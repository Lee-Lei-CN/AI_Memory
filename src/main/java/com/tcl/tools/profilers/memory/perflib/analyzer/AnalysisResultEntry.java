package com.tcl.tools.profilers.memory.perflib.analyzer;


public interface AnalysisResultEntry<T> {
    String getWarningMessage();

    String getCategory();

    Offender<T> getOffender();
}
