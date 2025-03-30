package com.tcl.tools.profilers.memory.perflib.heap.hprof;

import java.io.IOException;

public class HprofRootJniLocal implements HprofDumpRecord {
    public static final byte SUBTAG = 2;
    public final long objectId;
    public final int threadSerialNumber;
    public final int frameNumberInStackTrace;

    public HprofRootJniLocal(long objectId, int threadSerialNumber, int frameNumberInStackTrace) {
        this.objectId = objectId;
        this.threadSerialNumber = threadSerialNumber;
        this.frameNumberInStackTrace = frameNumberInStackTrace;
    }

    public void write(HprofOutputStream hprof) throws IOException {
        hprof.writeU1((byte)2);
        hprof.writeId(this.objectId);
        hprof.writeU4(this.threadSerialNumber);
        hprof.writeU4(this.frameNumberInStackTrace);
    }

    public int getLength(int idSize) {
        return 1 + idSize + 4 + 4;
    }
}
