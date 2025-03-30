package com.tcl.tools.profilers.memory.perflib.heap.memoryanaluzer;


import com.android.tools.perflib.analyzer.AnalysisResultEntry;
import com.android.tools.perflib.heap.Instance;
import com.android.tools.perflib.heap.memoryanalyzer.Printer;
import com.android.tools.perflib.heap.memoryanalyzer.Report;
import com.android.tools.perflib.heap.memoryanalyzer.TopMemoryOffendersAnalyzerTask;

import java.util.Iterator;
import java.util.List;

public final class TopMemoryOffendersReport implements Report {
    private List<AnalysisResultEntry<?>> mResults;

    public TopMemoryOffendersReport() {
    }

    public void generate(List<AnalysisResultEntry<?>> data) {
        this.mResults = data;
    }

    public void print(Printer printer) {
        com.android.tools.perflib.heap.memoryanalyzer.TopMemoryOffendersAnalyzerTask task = new TopMemoryOffendersAnalyzerTask();
        printer.addHeading(2, task.getTaskName() + " Report");
        printer.addParagraph(task.getTaskDescription());
        if (this.mResults != null && !this.mResults.isEmpty()) {
            printer.startTable(new String[]{"Total Retained Size", "Instance"});
            Iterator var3 = this.mResults.iterator();

            while(var3.hasNext()) {
                AnalysisResultEntry<?> entry = (AnalysisResultEntry)var3.next();
                if (!entry.getOffender().getOffenders().isEmpty()) {
                    Instance instance = (Instance)entry.getOffender().getOffenders().get(0);
                    String totalRetainedSize = Long.toString(instance.getTotalRetainedSize());
                    String instanceString = printer.formatInstance(instance);
                    printer.addRow(new String[]{totalRetainedSize, instanceString});
                }
            }

            printer.endTable();
        } else {
            printer.addParagraph("Top offenders not found.");
        }
    }
}
