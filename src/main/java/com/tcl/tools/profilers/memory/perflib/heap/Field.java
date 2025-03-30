package com.tcl.tools.profilers.memory.perflib.heap;
import com.google.common.base.Objects;

public final class Field {
    private final Type mType;
    private final String mName;

    public Field(Type type, String name) {
        this.mType = type;
        this.mName = name;
    }

    public Type getType() {
        return this.mType;
    }

    public String getName() {
        return this.mName;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Field)) {
            return false;
        } else {
            Field field = (Field)o;
            return this.mType == field.mType && this.mName.equals(field.mName);
        }
    }

    public int hashCode() {
        return Objects.hashCode(new Object[]{this.mType, this.mName});
    }
}
