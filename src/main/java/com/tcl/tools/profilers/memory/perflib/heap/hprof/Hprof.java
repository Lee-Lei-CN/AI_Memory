package com.tcl.tools.profilers.memory.perflib.heap.hprof;


import com.google.common.base.Charsets;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class Hprof {
    public final String format;
    public final int idSize;
    public final Date date;
    public final List<HprofRecord> records;

    public Hprof(String format, int idSize, Date date, List<HprofRecord> records) {
        this.format = format;
        this.idSize = idSize;
        this.date = date;
        this.records = records;
    }

    public void write(OutputStream os) throws IOException {
        HprofOutputStream hprof = new HprofOutputStream(this.idSize, os);
        hprof.write(this.format.getBytes(Charsets.US_ASCII));
        hprof.write(0);
        hprof.writeU4(this.idSize);
        long time = this.date.getTime();
        hprof.writeU4((int)(time >> 32));
        hprof.writeU4((int)(time >> 0));
        Iterator var5 = this.records.iterator();

        while(var5.hasNext()) {
            HprofRecord record = (HprofRecord)var5.next();
            record.write(hprof);
        }

        hprof.flush();
        hprof.close();
    }
}
