package com.tcl.tools.profilers.memory.perflib.heap.hprof;


import java.io.IOException;

public class HprofRootNativeStack implements HprofDumpRecord {
    public static final byte SUBTAG = 4;
    public final long objectId;
    public final int threadSerialNumber;

    public HprofRootNativeStack(long objectId, int threadSerialNumber) {
        this.objectId = objectId;
        this.threadSerialNumber = threadSerialNumber;
    }

    public void write(HprofOutputStream hprof) throws IOException {
        hprof.writeU1((byte)4);
        hprof.writeId(this.objectId);
        hprof.writeU4(this.threadSerialNumber);
    }

    public int getLength(int idSize) {
        return 1 + idSize + 4;
    }
}
