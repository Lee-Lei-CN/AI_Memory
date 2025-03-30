package com.tcl.tools.profilers.memory.perflib.heap;

import com.tcl.tools.profilers.memory.perflib.captures.DataBuffer;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Iterator;

public class ArrayInstance extends Instance {
    private final Type mType;
    private final int mLength;
    private final long mValuesOffset;

    public ArrayInstance(long id, StackTrace stack, Type type, int length, long valuesOffset) {
        super(id, stack);
        this.mType = type;
        this.mLength = length;
        this.mValuesOffset = valuesOffset;
    }

    public Object[] getValues() {
        Object[] values = new Object[this.mLength];
        this.getBuffer().setPosition(this.mValuesOffset);

        for(int i = 0; i < this.mLength; ++i) {
            values[i] = this.readValue(this.mType);
        }

        return values;
    }

    public byte[] asRawByteArray(int start, int elementCount) {
        this.getBuffer().setPosition(this.mValuesOffset);

        assert this.mType != Type.OBJECT;

        assert start + elementCount <= this.mLength;

        byte[] bytes = new byte[elementCount * this.mType.getSize()];
        this.getBuffer().readSubSequence(bytes, start * this.mType.getSize(), elementCount * this.mType.getSize());
        return bytes;
    }

    public char[] asCharArray(int offset, int length) {
        assert this.mType == Type.CHAR;

        CharBuffer charBuffer = ByteBuffer.wrap(this.asRawByteArray(offset, length)).order(DataBuffer.HPROF_BYTE_ORDER).asCharBuffer();
        char[] result = new char[length];
        charBuffer.get(result);
        return result;
    }

    public final int getSize() {
        return this.mLength * this.mHeap.mSnapshot.getTypeSize(this.mType);
    }

    public final void resolveReferences() {
        if (this.mType == Type.OBJECT) {
            Object[] var1 = this.getValues();
            int var2 = var1.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                Object value = var1[var3];
                if (value instanceof Instance) {
                    ((Instance)value).addReverseReference((Field)null, this);
                    this.mHardForwardReferences.add((Instance)value);
                }
            }
        }

    }

    public final void accept(Visitor visitor) {
        visitor.visitArrayInstance(this);
        Iterator var2 = this.mHardForwardReferences.iterator();

        while(var2.hasNext()) {
            Instance instance = (Instance)var2.next();
            visitor.visitLater(this, instance);
        }

    }

    public ClassObj getClassObj() {
        if (this.mType == Type.OBJECT) {
            return super.getClassObj();
        } else {
            ClassObj primitiveArrayClassObj = this.mHeap.mSnapshot.findClass(this.mType.getClassNameOfPrimitiveArray(false));
            if (primitiveArrayClassObj == null) {
                primitiveArrayClassObj = this.mHeap.mSnapshot.findClass(this.mType.getClassNameOfPrimitiveArray(true));
            }

            return primitiveArrayClassObj;
        }
    }

    public int getLength() {
        return this.mLength;
    }

    public Type getArrayType() {
        return this.mType;
    }

    public final String toString() {
        String className = this.getClassObj().getClassName();
        if (className.endsWith("[]")) {
            className = className.substring(0, className.length() - 2);
        }

        return String.format("%s[%d]@%d (0x%x)", className, this.mLength, this.getUniqueId(), this.getUniqueId());
    }
}
