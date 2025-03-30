package com.tcl.tools.profilers.memory.perflib.heap.hprof;

import java.io.IOException;

public class HprofRootThreadBlock implements HprofDumpRecord {
    public static final byte SUBTAG = 6;
    public final long objectId;
    public final int threadSerialNumber;

    public HprofRootThreadBlock(long objectId, int threadSerialNumber) {
        this.objectId = objectId;
        this.threadSerialNumber = threadSerialNumber;
    }

    public void write(HprofOutputStream hprof) throws IOException {
        hprof.writeU1((byte)6);
        hprof.writeId(this.objectId);
        hprof.writeU4(this.threadSerialNumber);
    }

    public int getLength(int idSize) {
        return 1 + idSize + 4;
    }
}
