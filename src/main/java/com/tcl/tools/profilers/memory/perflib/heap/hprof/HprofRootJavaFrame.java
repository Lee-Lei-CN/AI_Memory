package com.tcl.tools.profilers.memory.perflib.heap.hprof;

import java.io.IOException;

public class HprofRootJavaFrame implements HprofDumpRecord {
    public static final byte SUBTAG = 3;
    public final long objectId;
    public final int threadSerialNumber;
    public final int frameNumberInStackTrace;

    public HprofRootJavaFrame(long objectId, int threadSerialNumber, int frameNumberInStackTrace) {
        this.objectId = objectId;
        this.threadSerialNumber = threadSerialNumber;
        this.frameNumberInStackTrace = frameNumberInStackTrace;
    }

    public void write(HprofOutputStream hprof) throws IOException {
        hprof.writeU1((byte)3);
        hprof.writeId(this.objectId);
        hprof.writeU4(this.threadSerialNumber);
        hprof.writeU4(this.frameNumberInStackTrace);
    }

    public int getLength(int idSize) {
        return 1 + idSize + 4 + 4;
    }
}
