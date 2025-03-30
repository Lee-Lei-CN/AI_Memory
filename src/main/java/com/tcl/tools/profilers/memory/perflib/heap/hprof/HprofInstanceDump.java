package com.tcl.tools.profilers.memory.perflib.heap.hprof;


import java.io.IOException;

public class HprofInstanceDump implements HprofDumpRecord {
    public static final byte SUBTAG = 33;
    public final long objectId;
    public final int stackTraceSerialNumber;
    public final long classObjectId;
    public final byte[] values;

    public HprofInstanceDump(long objectId, int stackTraceSerialNumber, long classObjectId, byte[] values) {
        this.objectId = objectId;
        this.stackTraceSerialNumber = stackTraceSerialNumber;
        this.classObjectId = classObjectId;
        this.values = values;
    }

    public void write(HprofOutputStream hprof) throws IOException {
        hprof.writeU1((byte)33);
        hprof.writeId(this.objectId);
        hprof.writeU4(this.stackTraceSerialNumber);
        hprof.writeId(this.classObjectId);
        hprof.writeU4(this.values.length);
        hprof.write(this.values);
    }

    public int getLength(int idSize) {
        return 1 + idSize + 4 + idSize + 4 + this.values.length;
    }
}
