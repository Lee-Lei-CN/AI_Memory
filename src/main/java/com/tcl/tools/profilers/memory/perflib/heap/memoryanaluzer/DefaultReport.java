package com.tcl.tools.profilers.memory.perflib.heap.memoryanaluzer;


import com.google.common.annotations.VisibleForTesting;
import com.tcl.tools.profilers.memory.perflib.analyzer.AnalysisResultEntry;

import java.util.Iterator;
import java.util.List;

public final class DefaultReport implements Report {
    private final MemoryAnalyzerTask mTask;
    protected List<AnalysisResultEntry<?>> mResults;
    @VisibleForTesting
    protected static final String NO_ISSUES_FOUND_STRING = "No issues found.";

    public DefaultReport(MemoryAnalyzerTask task) {
        this.mTask = task;
    }

    public void generate(List<AnalysisResultEntry<?>> data) {
        this.mResults = data;
    }

    public void print(Printer printer) {
        printer.addHeading(2, this.mTask.getTaskName() + " Report");
        printer.addParagraph(this.mTask.getTaskDescription());
        if (this.mResults != null && this.mResults.isEmpty()) {
            printer.addParagraph("No issues found.");
        } else {
            printer.startTable(new String[0]);
            Iterator var2 = this.mResults.iterator();

            while(var2.hasNext()) {
                AnalysisResultEntry<?> result = (AnalysisResultEntry)var2.next();
                printer.addRow(new String[]{result.getWarningMessage()});
            }

            printer.endTable();
        }
    }
}
