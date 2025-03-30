package com.tcl.tools.profilers.memory.perflib.heap.hprof;


import java.io.IOException;

public class HprofRootMonitorUsed implements HprofDumpRecord {
    public static final byte SUBTAG = 7;
    public final long objectId;

    public HprofRootMonitorUsed(long objectId) {
        this.objectId = objectId;
    }

    public void write(HprofOutputStream hprof) throws IOException {
        hprof.writeU1((byte)7);
        hprof.writeId(this.objectId);
    }

    public int getLength(int idSize) {
        return 1 + idSize;
    }
}
