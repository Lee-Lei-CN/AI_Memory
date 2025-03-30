package com.tcl.tools.profilers.memory.perflib.heap.memoryanaluzer;


import com.tcl.tools.profilers.memory.perflib.analyzer.AnalysisResultEntry;
import com.tcl.tools.profilers.memory.perflib.heap.Instance;
import com.tcl.tools.profilers.memory.perflib.heap.Snapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class TopMemoryOffendersAnalyzerTask extends MemoryAnalyzerTask {
    private static final int DEFAULT_NUM_ENTRIES = 5;
    private final int mNumEntries;

    public TopMemoryOffendersAnalyzerTask() {
        this(5);
    }

    public TopMemoryOffendersAnalyzerTask(int numEntries) {
        this.mNumEntries = numEntries;
    }

    protected List<AnalysisResultEntry<?>> analyze(Configuration configuration, Snapshot snapshot) {
        List<Instance> reachableInstances = snapshot.getReachableInstances();
        Collections.sort(reachableInstances, (a, b) -> {
            return Long.compare(a.getTotalRetainedSize(), b.getTotalRetainedSize());
        });
        Collections.reverse(reachableInstances);
        List<AnalysisResultEntry<?>> entries = new ArrayList();
        Iterator var5 = reachableInstances.subList(0, Math.min(this.mNumEntries, reachableInstances.size())).iterator();

        while(var5.hasNext()) {
            Instance instance = (Instance)var5.next();
            entries.add(new TopMemoryOffendersAnalyzerTask.TopMemoryOffendersEntry("Offender #" + (entries.size() + 1) + " (" + instance + ") has total retained size " + instance.getTotalRetainedSize() + ".", Arrays.asList(instance)));
        }

        return entries;
    }

    public String getTaskName() {
        return "Top Memory Offenders";
    }

    public String getTaskDescription() {
        return "Finds the top objects in memory.";
    }

    public static class TopMemoryOffendersEntry extends MemoryAnalysisResultEntry {
        protected TopMemoryOffendersEntry(String offenseDescription, List<Instance> offendingInstances) {
            super(offenseDescription, offendingInstances);
        }

        public String getWarningMessage() {
            return this.mOffender.getOffendingDescription();
        }

        public String getCategory() {
            return "Top Memory Offenders";
        }
    }
}
