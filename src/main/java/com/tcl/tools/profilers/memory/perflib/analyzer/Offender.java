package com.tcl.tools.profilers.memory.perflib.analyzer;

import java.util.Collections;
import java.util.List;

public class Offender<T> {
    protected List<T> mOffenders;
    protected String mOffendingDescription;

    public Offender(String offendingDescription, List<T> offendingInstances) {
        this.mOffendingDescription = offendingDescription;
        this.mOffenders = offendingInstances;
    }

    public List<T> getOffenders() {
        return Collections.unmodifiableList(this.mOffenders);
    }

    public String getOffendingDescription() {
        return this.mOffendingDescription;
    }
}
