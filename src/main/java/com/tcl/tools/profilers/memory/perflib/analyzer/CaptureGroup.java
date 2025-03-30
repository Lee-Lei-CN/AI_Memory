package com.tcl.tools.profilers.memory.perflib.analyzer;


import java.util.ArrayList;
import java.util.List;

public class CaptureGroup {
    private List<Capture> mCaptures = new ArrayList();

    public CaptureGroup() {
    }

    public List<Capture> getCaptures() {
        return this.mCaptures;
    }

    public void addCapture(Capture capture) {
        this.mCaptures.add(capture);
    }
}
