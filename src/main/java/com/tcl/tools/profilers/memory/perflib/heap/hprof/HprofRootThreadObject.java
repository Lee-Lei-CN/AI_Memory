package com.tcl.tools.profilers.memory.perflib.heap.hprof;

import java.io.IOException;

public class HprofRootThreadObject implements HprofDumpRecord {
    public static final byte SUBTAG = 8;
    public final long objectId;
    public final int threadSerialNumber;
    public final int stackTraceSerialNumber;

    public HprofRootThreadObject(long objectId, int threadSerialNumber, int stackTraceSerialNumber) {
        this.objectId = objectId;
        this.threadSerialNumber = threadSerialNumber;
        this.stackTraceSerialNumber = stackTraceSerialNumber;
    }

    public void write(HprofOutputStream hprof) throws IOException {
        hprof.writeU1((byte)8);
        hprof.writeId(this.objectId);
        hprof.writeU4(this.threadSerialNumber);
        hprof.writeU4(this.stackTraceSerialNumber);
    }

    public int getLength(int idSize) {
        return 1 + idSize + 4 + 4;
    }
}