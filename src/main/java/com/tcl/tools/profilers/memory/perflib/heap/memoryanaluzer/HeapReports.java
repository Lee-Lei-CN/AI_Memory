package com.tcl.tools.profilers.memory.perflib.heap.memoryanaluzer;



import com.tcl.tools.profilers.memory.perflib.analyzer.AnalysisResultEntry;
import com.tcl.tools.profilers.memory.perflib.analyzer.Capture;
import com.tcl.tools.profilers.memory.perflib.heap.Snapshot;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class HeapReports {
    private HeapReports() {
    }

    public static DefaultReport generateReport(MemoryAnalyzerTask task, Snapshot snapshot) {
        DefaultReport report = new DefaultReport(task);
        generateReport(report, task, snapshot);
        return report;
    }

    public static void generateReport(Report report, MemoryAnalyzerTask task, Snapshot snapshot) {
        Set<MemoryAnalyzerTask> tasks = new HashSet();
        tasks.add(task);
        List<AnalysisResultEntry<?>> results = TaskRunner.runTasks(tasks, new Capture[]{snapshot});
        report.generate(results);
    }
}