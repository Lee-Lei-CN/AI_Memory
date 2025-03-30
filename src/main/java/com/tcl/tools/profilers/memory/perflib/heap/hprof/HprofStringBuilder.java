package com.tcl.tools.profilers.memory.perflib.heap.hprof;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HprofStringBuilder {
    private int mTime;
    private Map<String, Integer> mStrings;
    private List<HprofString> mStringRecords;

    public HprofStringBuilder(int time) {
        this.mTime = time;
        this.mStrings = new HashMap();
        this.mStringRecords = new ArrayList();
    }

    public int get(String string) {
        Integer id = (Integer)this.mStrings.get(string);
        if (id == null) {
            id = this.mStrings.size() + 1;
            this.mStrings.put(string, id);
            this.mStringRecords.add(new HprofString(this.mTime, (long)id, string));
        }

        return id;
    }

    public List<HprofString> getStringRecords() {
        return this.mStringRecords;
    }
}
