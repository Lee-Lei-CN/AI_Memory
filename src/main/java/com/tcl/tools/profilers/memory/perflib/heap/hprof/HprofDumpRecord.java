package com.tcl.tools.profilers.memory.perflib.heap.hprof;


import java.io.IOException;

public interface HprofDumpRecord {
    void write(HprofOutputStream hprof) throws IOException;

    int getLength(int idSize);
}
