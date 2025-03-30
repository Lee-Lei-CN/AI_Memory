package com.tcl.tools.profilers.memory.perflib.heap.hprof;

import com.google.common.base.Charsets;
import java.io.IOException;

public class HprofString implements HprofRecord {
    public static final byte TAG = 1;
    public final int time;
    public final long id;
    public final String string;

    public HprofString(int time, long id, String string) {
        this.time = time;
        this.id = id;
        this.string = string;
    }

    public void write(HprofOutputStream hprof) throws IOException {
        byte[] bytes = this.string.getBytes(Charsets.UTF_8);
        hprof.writeRecordHeader((byte)1, this.time, hprof.getIdSize() + bytes.length);
        hprof.writeId(this.id);
        hprof.write(bytes);
    }
}
