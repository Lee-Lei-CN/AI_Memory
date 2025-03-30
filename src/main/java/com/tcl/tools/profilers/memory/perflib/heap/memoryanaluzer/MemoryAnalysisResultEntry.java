package com.tcl.tools.profilers.memory.perflib.heap.memoryanaluzer;

import com.tcl.tools.profilers.memory.perflib.analyzer.AnalysisResultEntry;
import com.tcl.tools.profilers.memory.perflib.analyzer.Offender;
import com.tcl.tools.profilers.memory.perflib.heap.Instance;

import java.util.List;

public abstract class MemoryAnalysisResultEntry implements AnalysisResultEntry<Instance> {
    protected Offender<Instance> mOffender;

    protected MemoryAnalysisResultEntry(String offenseDescription, List<Instance> offendingInstance) {
        this.mOffender = new Offender(offenseDescription, offendingInstance);
    }

    public Offender<Instance> getOffender() {
        return this.mOffender;
    }
}
