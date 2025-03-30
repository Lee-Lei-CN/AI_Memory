package com.tcl.tools.profilers.memory.perflib.heap;


public class Value {
    private Object mValue;
    private final Instance instance;

    public Value(Instance instance) {
        this.instance = instance;
    }

    public Object getValue() {
        return this.mValue;
    }

    public void setValue(Object value) {
        this.mValue = value;
        if (value instanceof Instance) {
            ((Instance)value).addReverseReference((Field)null, this.instance);
        }

    }
}
