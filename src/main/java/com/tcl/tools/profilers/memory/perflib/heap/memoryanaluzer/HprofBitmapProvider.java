package com.tcl.tools.profilers.memory.perflib.heap.memoryanaluzer;


import com.tcl.tools.profilers.memory.perflib.heap.*;

import java.awt.*;
import java.util.Iterator;

public class HprofBitmapProvider implements BitmapDecoder.BitmapDataProvider {
    private ArrayInstance mBuffer = null;
    private boolean mMutable = false;
    private int mWidth = -1;
    private int mHeight = -1;

    public HprofBitmapProvider(Instance instance) {
        ClassInstance resolvedInstance = getBitmapClassInstance(instance);
        if (resolvedInstance == null) {
            throw new RuntimeException("Can not resolve Bitmap instance");
        } else {
            Integer width = null;
            Integer height = null;
            Boolean mutable = null;
            Iterator var6 = resolvedInstance.getValues().iterator();

            while(true) {
                while(var6.hasNext()) {
                    ClassInstance.FieldValue field = (ClassInstance.FieldValue)var6.next();
                    Object bitmapValue = field.getValue();
                    String bitmapFieldName = field.getField().getName();
                    if ("mBuffer".equals(bitmapFieldName) && bitmapValue instanceof ArrayInstance) {
                        ArrayInstance arrayInstance = (ArrayInstance)bitmapValue;
                        if (arrayInstance.getArrayType() == Type.BYTE) {
                            this.mBuffer = arrayInstance;
                        }
                    } else if ("mIsMutable".equals(bitmapFieldName) && bitmapValue instanceof Boolean) {
                        mutable = (Boolean)bitmapValue;
                    } else if ("mWidth".equals(bitmapFieldName) && bitmapValue instanceof Integer) {
                        width = (Integer)bitmapValue;
                    } else if ("mHeight".equals(bitmapFieldName) && bitmapValue instanceof Integer) {
                        height = (Integer)bitmapValue;
                    }
                }

                if (this.mBuffer != null && this.mBuffer.getArrayType() == Type.BYTE && mutable != null && width != null && height != null) {
                    this.mMutable = mutable;
                    this.mWidth = width;
                    this.mHeight = height;
                    return;
                }

                throw new RuntimeException("Unable to resolve bitmap instance member variables");
            }
        }
    }

    public static boolean canGetBitmapFromInstance(Instance value) {
        if (!(value instanceof ClassInstance)) {
            return false;
        } else {
            String className = value.getClassObj().getClassName();
            return "android.graphics.Bitmap".equals(className) || "android.graphics.drawable.BitmapDrawable".equals(className);
        }
    }

    public String getBitmapConfigName() throws Exception {
        int area = this.mWidth * this.mHeight;
        int pixelSize = this.mBuffer.getLength() / area;
        if ((this.mMutable || this.mBuffer.getLength() % area == 0) && (!this.mMutable || area <= this.mBuffer.getLength())) {
            switch(pixelSize) {
                case 2:
                    return "\"RGB_565\"";
                case 4:
                    return "\"ARGB_8888\"";
                default:
                    return "\"ALPHA_8\"";
            }
        } else {
            return null;
        }
    }

    public Dimension getDimension() throws Exception {
        return this.mWidth >= 0 && this.mHeight >= 0 ? new Dimension(this.mWidth, this.mHeight) : null;
    }

    public boolean downsizeBitmap(Dimension newSize) throws Exception {
        return true;
    }

    public byte[] getPixelBytes(Dimension size) throws Exception {
        return this.mBuffer.asRawByteArray(0, this.mBuffer.getLength());
    }

    private static ClassInstance getBitmapClassInstance(Instance instance) {
        if (!(instance instanceof ClassInstance)) {
            return null;
        } else {
            ClassInstance selectedObject = (ClassInstance)instance;
            String className = instance.getClassObj().getClassName();
            if ("android.graphics.Bitmap".equals(className)) {
                return selectedObject;
            } else {
                return "android.graphics.drawable.BitmapDrawable".equals(className) ? getBitmapFromDrawable(selectedObject) : null;
            }
        }
    }

    private static ClassInstance getBitmapFromDrawable(ClassInstance instance) {
        ClassInstance bitmapState = getBitmapStateFromBitmapDrawable(instance);
        if (bitmapState == null) {
            return null;
        } else {
            Iterator var2 = bitmapState.getValues().iterator();

            while(var2.hasNext()) {
                ClassInstance.FieldValue fieldValue = (ClassInstance.FieldValue)var2.next();
                Field field = fieldValue.getField();
                Object value = fieldValue.getValue();
                if ("mBitmap".equals(field.getName()) && value instanceof ClassInstance) {
                    ClassInstance result = (ClassInstance)value;
                    String className = result.getClassObj().getClassName();
                    if ("android.graphics.Bitmap".equals(className)) {
                        return (ClassInstance)value;
                    }
                }
            }

            return null;
        }
    }

    private static ClassInstance getBitmapStateFromBitmapDrawable(ClassInstance bitmapDrawable) {
        Iterator var1 = bitmapDrawable.getValues().iterator();

        while(var1.hasNext()) {
            ClassInstance.FieldValue field = (ClassInstance.FieldValue)var1.next();
            String fieldName = field.getField().getName();
            Object fieldValue = field.getValue();
            if ("mBitmapState".equals(fieldName) && fieldValue instanceof ClassInstance) {
                ClassInstance result = (ClassInstance)fieldValue;
                String className = result.getClassObj().getClassName();
                if ("android.graphics.drawable.BitmapDrawable$BitmapState".equals(className)) {
                    return result;
                }
            }
        }

        return null;
    }
}
