package com.tcl.tools.profilers.memory.perflib.heap.memoryanaluzer;


import com.tcl.tools.profilers.memory.perflib.analyzer.AnalysisResultEntry;
import com.tcl.tools.profilers.memory.perflib.heap.Instance;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public final class DuplicatedStringsReport implements Report {
    private List<AnalysisResultEntry<?>> mResults;
    private static final int MAX_VALUE_STRING_LENGTH = 100;

    public DuplicatedStringsReport() {
    }

    public void generate(List<AnalysisResultEntry<?>> results) {
        Collections.sort(results, Collections.reverseOrder(new Comparator<AnalysisResultEntry<?>>() {
            public int compare(AnalysisResultEntry<?> o1, AnalysisResultEntry<?> o2) {
                return DuplicatedStringsReport.this.getConsumedBytes(o1) - DuplicatedStringsReport.this.getConsumedBytes(o2);
            }
        }));
        this.mResults = results;
    }

    public void print(Printer printer) {
        DuplicatedStringsAnalyzerTask task = new DuplicatedStringsAnalyzerTask();
        printer.addHeading(2, task.getTaskName() + " Report");
        printer.addParagraph(task.getTaskDescription());
        if (this.mResults != null && !this.mResults.isEmpty()) {
            printer.startTable(new String[]{"Value", "Bytes", "Duplicates", "First Duplicate"});
            Iterator var3 = this.mResults.iterator();

            while(var3.hasNext()) {
                AnalysisResultEntry<?> entry = (AnalysisResultEntry)var3.next();
                String value = entry.getOffender().getOffendingDescription();
                if (value.length() > 100) {
                    value = value.substring(0, 100) + "...";
                }

                String consumedBytes = Integer.toString(this.getConsumedBytes(entry));
                String duplicates = Integer.toString(entry.getOffender().getOffenders().size());
                String instance = printer.formatInstance((Instance)entry.getOffender().getOffenders().get(0));
                printer.addRow(new String[]{value, consumedBytes, duplicates, instance});
            }

            printer.endTable();
        } else {
            printer.addParagraph("No issues found.");
        }
    }

    private int getConsumedBytes(AnalysisResultEntry<?> entry) {
        return entry.getOffender().getOffendingDescription().length() * entry.getOffender().getOffenders().size();
    }
}
