package com.tcl.tools.profilers.memory.perflib;



import com.android.ddmlib.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

public class ByteBufferUtil {
    public ByteBufferUtil() {
    }

    public static ByteBuffer mapFile(File f, long offset, ByteOrder byteOrder) throws IOException {
        FileInputStream dataFile = new FileInputStream(f);

        MappedByteBuffer var7;
        try {
            FileChannel fc = dataFile.getChannel();
            MappedByteBuffer buffer = fc.map(MapMode.READ_ONLY, offset, f.length() - offset);
            buffer.order(byteOrder);
            var7 = buffer;
        } finally {
            dataFile.close();
        }

        return var7;
    }

    public static String getString(ByteBuffer buf, int len) {
        char[] data = new char[len];

        for(int i = 0; i < len; ++i) {
            data[i] = buf.getChar();
        }

        return new String(data);
    }

    public static void putString(ByteBuffer buf, String str) {
        int len = str.length();

        for(int i = 0; i < len; ++i) {
            buf.putChar(str.charAt(i));
        }

    }

    public static boolean cleanBuffer(ByteBuffer buffer) {
        if (!buffer.isDirect()) {
            return true;
        } else {
            try {
                Class<?> unsafeClass = com.android.ddmlib.ByteBufferUtil.class.getClassLoader().loadClass("sun.misc.Unsafe");
                Field f = unsafeClass.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                Object unsafe = f.get((Object)null);
                MethodType type = MethodType.methodType(Void.TYPE, ByteBuffer.class);
                MethodHandle handle = MethodHandles.lookup().findVirtual(unsafeClass, "invokeCleaner", type);
                handle.invoke(unsafeClass.cast(unsafe), buffer);
                return true;
            } catch (Throwable var6) {
                Log.w("ddmlib", "ByteBufferUtil.cleanBuffer() failed " + var6);
                return false;
            }
        }
    }
}
