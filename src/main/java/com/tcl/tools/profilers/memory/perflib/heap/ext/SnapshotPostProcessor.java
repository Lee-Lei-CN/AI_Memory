package com.tcl.tools.profilers.memory.perflib.heap.ext;


import com.tcl.tools.profilers.memory.perflib.heap.Snapshot;

public interface SnapshotPostProcessor {
    void postProcess(Snapshot snapshot);
}
