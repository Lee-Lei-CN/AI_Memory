package com.tcl.tools.profilers.memory.perflib.heap.hprof;


import java.io.IOException;

public class HprofEndThread implements HprofRecord {
    public static final byte TAG = 11;
    public final int time;
    public final int threadSerialNumber;

    public HprofEndThread(int time, int threadSerialNumber) {
        this.time = time;
        this.threadSerialNumber = threadSerialNumber;
    }

    public void write(HprofOutputStream hprof) throws IOException {
        hprof.writeRecordHeader((byte)11, this.time, 4);
        hprof.writeU4(this.threadSerialNumber);
    }
}
