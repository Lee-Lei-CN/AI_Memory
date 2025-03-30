package com.tcl.tools.profilers.memory.perflib.heap.hprof;

import java.io.IOException;

public class HprofStackFrame implements HprofRecord {
    public static final byte TAG = 4;
    public static final int NO_LINE_INFO = 0;
    public static final int UNKNOWN_LOCATION = -1;
    public static final int COMPILED_METHOD = -2;
    public static final int NATIVE_METHOD = -3;
    public final int time;
    public final long stackFrameId;
    public final long methodNameStringId;
    public final long methodSignatureStringId;
    public final long sourceFileNameStringId;
    public final int classSerialNumber;
    public final int lineNumber;

    public HprofStackFrame(int time, long stackFrameId, long methodNameStringId, long methodSignatureStringId, long sourceFileNameStringId, int classSerialNumber, int lineNumber) {
        this.time = time;
        this.stackFrameId = stackFrameId;
        this.methodNameStringId = methodNameStringId;
        this.methodSignatureStringId = methodSignatureStringId;
        this.sourceFileNameStringId = sourceFileNameStringId;
        this.classSerialNumber = classSerialNumber;
        this.lineNumber = lineNumber;
    }

    public void write(HprofOutputStream hprof) throws IOException {
        int id = hprof.getIdSize();
        int u4 = 4;
        hprof.writeRecordHeader((byte)4, this.time, id + id + id + id + u4 + u4);
        hprof.writeId(this.stackFrameId);
        hprof.writeId(this.methodNameStringId);
        hprof.writeId(this.methodSignatureStringId);
        hprof.writeId(this.sourceFileNameStringId);
        hprof.writeU4(this.classSerialNumber);
        hprof.writeU4(this.lineNumber);
    }
}
