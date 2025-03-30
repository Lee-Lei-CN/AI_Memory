package com.tcl.tools.profilers.memory.perflib.heap.hprof;



import java.io.IOException;

public class HprofAllocSites implements HprofRecord {
    public static final byte TAG = 6;
    public static final short INCREMENTAL_VS_COMPLETE = 1;
    public static final short SORTED_BY_ALLOCATION_VS_LINE = 2;
    public static final short FORCE_GC = 4;
    public final int time;
    public final short bitMaskFlags;
    public final int cutoffRatio;
    public final int totalLiveBytes;
    public final int totalLiveInstances;
    public final long totalBytesAllocated;
    public final long totalInstancesAllocated;
    public final HprofAllocSite[] sites;

    public HprofAllocSites(int time, short bitMaskFlags, int cutoffRatio, int totalLiveBytes, int totalLiveInstances, long totalBytesAllocated, long totalInstancesAllocated, HprofAllocSite[] sites) {
        this.time = time;
        this.bitMaskFlags = bitMaskFlags;
        this.cutoffRatio = cutoffRatio;
        this.totalLiveBytes = totalLiveBytes;
        this.totalLiveInstances = totalLiveInstances;
        this.totalBytesAllocated = totalBytesAllocated;
        this.totalInstancesAllocated = totalInstancesAllocated;
        this.sites = sites;
    }

    public void write(HprofOutputStream hprof) throws IOException {
        hprof.writeRecordHeader((byte)6, this.time, 34 + this.sites.length * 25);
        hprof.writeU2(this.bitMaskFlags);
        hprof.writeU4(this.cutoffRatio);
        hprof.writeU4(this.totalLiveBytes);
        hprof.writeU4(this.totalLiveInstances);
        hprof.writeU8(this.totalBytesAllocated);
        hprof.writeU8(this.totalInstancesAllocated);
        hprof.writeU4(this.sites.length);
        HprofAllocSite[] var2 = this.sites;
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            HprofAllocSite site = var2[var4];
            site.write(hprof);
        }

    }
}
