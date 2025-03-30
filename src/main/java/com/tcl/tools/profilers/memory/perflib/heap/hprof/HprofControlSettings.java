package com.tcl.tools.profilers.memory.perflib.heap.hprof;


import java.io.IOException;

public class HprofControlSettings implements HprofRecord {
    public static final byte TAG = 14;
    public static final int ALLOC_TRACES_ON = 1;
    public static final int CPU_SAMPLING_ON = 2;
    public final int time;
    public final int bitMaskFlags;
    public final short stackTraceDepth;

    public HprofControlSettings(int time, int bitMaskFlags, short stackTraceDepth) {
        this.time = time;
        this.bitMaskFlags = bitMaskFlags;
        this.stackTraceDepth = stackTraceDepth;
    }

    public void write(HprofOutputStream hprof) throws IOException {
        hprof.writeRecordHeader((byte)14, this.time, 6);
        hprof.writeU4(this.bitMaskFlags);
        hprof.writeU2(this.stackTraceDepth);
    }
}
