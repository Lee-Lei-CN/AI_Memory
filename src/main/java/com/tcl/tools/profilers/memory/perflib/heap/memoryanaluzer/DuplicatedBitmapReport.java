package com.tcl.tools.profilers.memory.perflib.heap.memoryanaluzer;



import com.tcl.tools.profilers.memory.perflib.analyzer.AnalysisResultEntry;
import com.tcl.tools.profilers.memory.perflib.heap.Instance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class DuplicatedBitmapReport implements Report {
    private List<AnalysisResultEntry<?>> results;

    public DuplicatedBitmapReport() {
    }

    public void generate(List<AnalysisResultEntry<?>> data) {
        List<DuplicatedBitmapAnalyzerTask.DuplicatedBitmapEntry> bitmapEntries = new ArrayList();
        Iterator var3 = data.iterator();

        while(var3.hasNext()) {
            AnalysisResultEntry<?> entry = (AnalysisResultEntry)var3.next();
            if (!(entry instanceof DuplicatedBitmapAnalyzerTask.DuplicatedBitmapEntry)) {
                this.results = null;
                return;
            }

            bitmapEntries.add((DuplicatedBitmapAnalyzerTask.DuplicatedBitmapEntry)entry);
        }

        Collections.sort(bitmapEntries, Collections.reverseOrder((o1, o2) -> {
            return getConsumedBytes(o1) - getConsumedBytes(o2);
        }));
        this.results = new ArrayList();
        this.results.addAll(bitmapEntries);
    }

    public void print(Printer printer) {
        DuplicatedBitmapAnalyzerTask task = new DuplicatedBitmapAnalyzerTask();
        printer.addHeading(2, task.getTaskName() + " Report");
        printer.addParagraph(task.getTaskDescription());
        if (this.results != null && !this.results.isEmpty()) {
            Iterator var3 = this.results.iterator();

            while(true) {
                AnalysisResultEntry entry;
                do {
                    do {
                        if (!var3.hasNext()) {
                            return;
                        }

                        entry = (AnalysisResultEntry)var3.next();
                    } while(entry.getOffender().getOffenders().size() < 1);
                } while(!(entry instanceof DuplicatedBitmapAnalyzerTask.DuplicatedBitmapEntry));

                Instance firstInstance = (Instance)entry.getOffender().getOffenders().get(0);
                printer.addHeading(3, printer.formatInstance(firstInstance));
                printer.addImage(firstInstance);
                int size = ((DuplicatedBitmapAnalyzerTask.DuplicatedBitmapEntry)entry).getByteArraySize();
                String bytes = Integer.toString(size);
                int duplicates = entry.getOffender().getOffenders().size();
                printer.startTable(new String[]{"Bytes", "Duplicates", "Total Bytes Consumed"});
                printer.addRow(new String[]{bytes, Integer.toString(duplicates), Integer.toString(size * duplicates)});
                printer.endTable();
                printer.startTable(new String[]{"All Duplicates"});
                List<Instance> instances = entry.getOffender().getOffenders();
                Iterator var10 = instances.iterator();

                while(var10.hasNext()) {
                    Instance instance = (Instance)var10.next();
                    printer.addRow(new String[]{printer.formatInstance(instance)});
                }

                printer.endTable();
            }
        } else {
            printer.addParagraph("No issues found.");
        }
    }

    private static int getConsumedBytes(DuplicatedBitmapAnalyzerTask.DuplicatedBitmapEntry entry) {
        return entry.getByteArraySize() * entry.getOffender().getOffenders().size();
    }
}
