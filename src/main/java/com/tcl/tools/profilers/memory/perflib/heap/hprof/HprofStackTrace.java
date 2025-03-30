package com.tcl.tools.profilers.memory.perflib.heap.hprof;


import java.io.IOException;

public class HprofStackTrace implements HprofRecord {
    public static final byte TAG = 5;
    public final int time;
    public final int stackTraceSerialNumber;
    public final int threadSerialNumber;
    public final long[] stackFrameIds;

    public HprofStackTrace(int time, int stackTraceSerialNumber, int threadSerialNumber, long[] stackFrameIds) {
        this.time = time;
        this.stackTraceSerialNumber = stackTraceSerialNumber;
        this.threadSerialNumber = threadSerialNumber;
        this.stackFrameIds = stackFrameIds;
    }

    public void write(HprofOutputStream hprof) throws IOException {
        int id = hprof.getIdSize();
        int u4 = 4;
        hprof.writeRecordHeader((byte)5, this.time, u4 + u4 + u4 + this.stackFrameIds.length * id);
        hprof.writeU4(this.stackTraceSerialNumber);
        hprof.writeU4(this.threadSerialNumber);
        hprof.writeU4(this.stackFrameIds.length);
        long[] var4 = this.stackFrameIds;
        int var5 = var4.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            long frameId = var4[var6];
            hprof.writeId(frameId);
        }

    }
}
