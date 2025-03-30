package com.tcl.tools.profilers.memory.perflib.heap.hprof;


import java.io.IOException;

public class HprofHeapDump implements HprofRecord {
    public static final byte TAG = 12;
    public final int time;
    public final HprofDumpRecord[] records;

    public HprofHeapDump(int time, HprofDumpRecord[] records) {
        this.time = time;
        this.records = records;
    }

    public void write(HprofOutputStream hprof) throws IOException {
        int idSize = hprof.getIdSize();
        int len = 0;
        HprofDumpRecord[] var4 = this.records;
        int var5 = var4.length;

        int var6;
        HprofDumpRecord record;
        for(var6 = 0; var6 < var5; ++var6) {
            record = var4[var6];
            len += record.getLength(idSize);
        }

        hprof.writeRecordHeader((byte)12, this.time, len);
        var4 = this.records;
        var5 = var4.length;

        for(var6 = 0; var6 < var5; ++var6) {
            record = var4[var6];
            record.write(hprof);
        }

    }
}
