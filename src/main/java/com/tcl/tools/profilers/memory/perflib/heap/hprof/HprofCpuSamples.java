package com.tcl.tools.profilers.memory.perflib.heap.hprof;



import java.io.IOException;

public class HprofCpuSamples implements HprofRecord {
    public static final byte TAG = 13;
    public final int time;
    public final int totalNumberOfSamples;
    public final HprofCpuSample[] samples;

    public HprofCpuSamples(int time, int totalNumberOfSamples, HprofCpuSample[] samples) {
        this.time = time;
        this.totalNumberOfSamples = totalNumberOfSamples;
        this.samples = samples;
    }

    public void write(HprofOutputStream hprof) throws IOException {
        hprof.writeRecordHeader((byte)13, this.time, 8 + this.samples.length * 8);
        hprof.writeU4(this.totalNumberOfSamples);
        hprof.writeU4(this.samples.length);
        HprofCpuSample[] var2 = this.samples;
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            HprofCpuSample sample = var2[var4];
            sample.write(hprof);
        }

    }
}
