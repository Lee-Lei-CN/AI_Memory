package com.tcl.tools.profilers.memory.perflib.analyzer;

public abstract class Capture {
    public Capture() {
    }

    public abstract <T> T getRepresentation(Class<T> asClass);

    public abstract String getTypeName();
}
