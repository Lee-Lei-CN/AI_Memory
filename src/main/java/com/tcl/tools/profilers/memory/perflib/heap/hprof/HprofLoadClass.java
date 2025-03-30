package com.tcl.tools.profilers.memory.perflib.heap.hprof;


import java.io.IOException;

public class HprofLoadClass implements HprofRecord {
    public static final byte TAG = 2;
    public final int time;
    public final int classSerialNumber;
    public final long classObjectId;
    public final int stackTraceSerialNumber;
    public final long classNameStringId;

    public HprofLoadClass(int time, int classSerialNumber, long classObjectId, int stackTraceSerialNumber, long classNameStringId) {
        this.time = time;
        this.classSerialNumber = classSerialNumber;
        this.classObjectId = classObjectId;
        this.stackTraceSerialNumber = stackTraceSerialNumber;
        this.classNameStringId = classNameStringId;
    }

    public void write(HprofOutputStream hprof) throws IOException {
        int id = hprof.getIdSize();
        hprof.writeRecordHeader((byte)2, this.time, 4 + id + 4 + id);
        hprof.writeU4(this.classSerialNumber);
        hprof.writeId(this.classObjectId);
        hprof.writeU4(this.stackTraceSerialNumber);
        hprof.writeId(this.classNameStringId);
    }
}
