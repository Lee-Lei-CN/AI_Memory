package com.tcl.tools.profilers.memory.perflib.heap.hprof;


import java.io.IOException;

public class HprofHeapDumpEnd implements HprofRecord {
    public static final byte TAG = 44;
    public final int time;

    public HprofHeapDumpEnd(int time) {
        this.time = time;
    }

    public void write(HprofOutputStream hprof) throws IOException {
        hprof.writeRecordHeader((byte)44, this.time, 0);
    }
}
