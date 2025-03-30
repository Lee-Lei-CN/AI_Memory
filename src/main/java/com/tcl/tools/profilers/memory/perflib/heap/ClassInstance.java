package com.tcl.tools.profilers.memory.perflib.heap;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ClassInstance extends Instance {
    private final long mValuesOffset;

    public ClassInstance(long id, StackTrace stack, long valuesOffset) {
        super(id, stack);
        this.mValuesOffset = valuesOffset;
    }

    List<FieldValue> getFields(String name) {
        ArrayList<FieldValue> result = new ArrayList();
        Iterator var3 = this.getValues().iterator();

        while(var3.hasNext()) {
            FieldValue value = (FieldValue)var3.next();
            if (value.getField().getName().equals(name)) {
                result.add(value);
            }
        }

        return result;
    }

    public List<FieldValue> getValues() {
        ArrayList<FieldValue> result = new ArrayList();
        ClassObj clazz = this.getClassObj();
        this.getBuffer().setPosition(this.mValuesOffset);

        while(clazz != null) {
            Field[] var3 = clazz.getFields();
            int var4 = var3.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                Field field = var3[var5];
                result.add(new FieldValue(field, this.readValue(field.getType())));
            }

            clazz = clazz.getSuperClassObj();
        }

        return result;
    }

    public final void resolveReferences() {
        Iterator var1 = this.getValues().iterator();

        while(true) {
            while(true) {
                FieldValue fieldValue;
                do {
                    if (!var1.hasNext()) {
                        this.mHardForwardReferences.trimToSize();
                        return;
                    }

                    fieldValue = (FieldValue)var1.next();
                } while(!(fieldValue.getValue() instanceof Instance));

                Instance referencedInstance = (Instance)fieldValue.getValue();
                referencedInstance.addReverseReference(fieldValue.getField(), this);
                if (this.getIsSoftReference() && fieldValue.getField().getName().equals("referent")) {
                    this.mSoftForwardReference = referencedInstance;
                } else {
                    this.mHardForwardReferences.add(referencedInstance);
                }
            }
        }
    }

    public final void accept(Visitor visitor) {
        visitor.visitClassInstance(this);
        Iterator var2 = this.mHardForwardReferences.iterator();

        while(var2.hasNext()) {
            Instance instance = (Instance)var2.next();
            visitor.visitLater(this, instance);
        }

    }

    public boolean getIsSoftReference() {
        return this.getClassObj().getIsSoftReference();
    }

    public final String toString() {
        return String.format("%s@%d (0x%x)", this.getClassObj().getClassName(), this.getUniqueId(), this.getUniqueId());
    }

    public boolean isStringInstance() {
        return this.getClassObj() != null && "java.lang.String".equals(this.getClassObj().getClassName());
    }

    public final String getAsString() {
        return this.getAsString(2147483647);
    }

    public final String getAsString(int maxDecodeStringLength) {
        int count = -1;
        int offset = 0;
        ArrayInstance charBufferArray = null;
        ArrayInstance byteBufferArray = null;
        Iterator var6 = this.getValues().iterator();

        while(true) {
            while(var6.hasNext()) {
                FieldValue entry = (FieldValue)var6.next();
                if (charBufferArray == null && "value".equals(entry.getField().getName())) {
                    if (entry.getValue() instanceof ArrayInstance) {
                        if (((ArrayInstance)entry.getValue()).getArrayType() == Type.CHAR) {
                            charBufferArray = (ArrayInstance)entry.getValue();
                        } else if (((ArrayInstance)entry.getValue()).getArrayType() == Type.BYTE) {
                            byteBufferArray = (ArrayInstance)entry.getValue();
                        }
                    }
                } else if ("count".equals(entry.getField().getName())) {
                    if (entry.getValue() instanceof Integer) {
                        count = (Integer)entry.getValue();
                    }
                } else if ("offset".equals(entry.getField().getName()) && entry.getValue() instanceof Integer) {
                    offset = (Integer)entry.getValue();
                }
            }

            if (byteBufferArray != null) {
                try {
                    return new String(byteBufferArray.asRawByteArray(offset >= 0 ? offset : 0, Math.max(Math.min(count, maxDecodeStringLength), 0)), "UTF-8");
                } catch (UnsupportedEncodingException var8) {
                    return null;
                }
            }

            return charBufferArray == null ? null : new String(charBufferArray.asCharArray(offset >= 0 ? offset : 0, Math.max(Math.min(count, maxDecodeStringLength), 0)));
        }
    }

    public static class FieldValue {
        private Field mField;
        private Object mValue;

        public FieldValue(Field field, Object value) {
            this.mField = field;
            this.mValue = value;
        }

        public Field getField() {
            return this.mField;
        }

        public Object getValue() {
            return this.mValue;
        }
    }
}
