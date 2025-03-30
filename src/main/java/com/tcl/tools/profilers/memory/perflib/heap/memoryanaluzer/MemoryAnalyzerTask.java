package com.tcl.tools.profilers.memory.perflib.heap.memoryanaluzer;

import com.tcl.tools.profilers.memory.perflib.analyzer.AnalysisResultEntry;
import com.tcl.tools.profilers.memory.perflib.analyzer.AnalyzerTask;
import com.tcl.tools.profilers.memory.perflib.heap.Heap;
import com.tcl.tools.profilers.memory.perflib.heap.Snapshot;

import java.util.Collection;
import java.util.List;

public abstract class MemoryAnalyzerTask implements AnalyzerTask {
    public MemoryAnalyzerTask() {
    }

    protected abstract List<AnalysisResultEntry<?>> analyze(MemoryAnalyzerTask.Configuration configuration, Snapshot snapshot);

    public static class Configuration {
        public Collection<Heap> mHeaps;

        public Configuration(Collection<Heap> heaps) {
            this.mHeaps = heaps;
        }
    }
}
