package com.tcl.tools.profilers.memory.perflib.heap.hprof;


import java.io.IOException;

public class HprofStartThread implements HprofRecord {
    public static final byte TAG = 10;
    public final int time;
    public final int threadSerialNumber;
    public final long threadObjectId;
    public final int stackTraceSerialNumber;
    public final long threadNameStringId;
    public final long threadGroupNameId;
    public final long threadParentGroupNameId;

    public HprofStartThread(int time, int threadSerialNumber, long threadObjectId, int stackTraceSerialNumber, long threadNameStringId, long threadGroupNameId, long threadParentGroupNameId) {
        this.time = time;
        this.threadSerialNumber = threadSerialNumber;
        this.threadObjectId = threadObjectId;
        this.stackTraceSerialNumber = stackTraceSerialNumber;
        this.threadNameStringId = threadNameStringId;
        this.threadGroupNameId = threadGroupNameId;
        this.threadParentGroupNameId = threadParentGroupNameId;
    }

    public void write(HprofOutputStream hprof) throws IOException {
        int id = hprof.getIdSize();
        hprof.writeRecordHeader((byte)10, this.time, 4 + id + 4 + id + id + id);
        hprof.writeU4(this.threadSerialNumber);
        hprof.writeId(this.threadObjectId);
        hprof.writeU4(this.stackTraceSerialNumber);
        hprof.writeId(this.threadNameStringId);
        hprof.writeId(this.threadGroupNameId);
        hprof.writeId(this.threadParentGroupNameId);
    }
}
