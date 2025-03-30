package com.tcl.tools.profilers.memory.perflib.heap.hprof;


import java.io.IOException;

public interface HprofRecord {
    void write(HprofOutputStream hprof) throws IOException;
}
