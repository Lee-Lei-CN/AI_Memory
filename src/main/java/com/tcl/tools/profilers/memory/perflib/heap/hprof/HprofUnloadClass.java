package com.tcl.tools.profilers.memory.perflib.heap.hprof;

import java.io.IOException;

public class HprofUnloadClass implements HprofRecord {
    public static final byte TAG = 3;
    public final int time;
    public final int classSerialNumber;

    public HprofUnloadClass(int time, int classSerialNumber) {
        this.time = time;
        this.classSerialNumber = classSerialNumber;
    }

    public void write(HprofOutputStream hprof) throws IOException {
        hprof.writeRecordHeader((byte)3, this.time, 4);
        hprof.writeU4(this.classSerialNumber);
    }
}
