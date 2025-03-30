package com.tcl.tools.profilers.memory.perflib.heap;


import com.google.common.primitives.UnsignedBytes;
import com.google.common.primitives.UnsignedInts;
import com.tcl.tools.profilers.memory.perflib.captures.DataBuffer;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TLongObjectHashMap;
import java.io.EOFException;
import java.io.IOException;

class HprofParser {
    private static final int STRING_IN_UTF8 = 1;
    private static final int LOAD_CLASS = 2;
    private static final int UNLOAD_CLASS = 3;
    private static final int STACK_FRAME = 4;
    private static final int STACK_TRACE = 5;
    private static final int ALLOC_SITES = 6;
    private static final int HEAP_SUMMARY = 7;
    private static final int START_THREAD = 10;
    private static final int END_THREAD = 11;
    private static final int HEAP_DUMP = 12;
    private static final int HEAP_DUMP_SEGMENT = 28;
    private static final int HEAP_DUMP_END = 44;
    private static final int CPU_SAMPLES = 13;
    private static final int CONTROL_SETTINGS = 14;
    private static final int ROOT_UNKNOWN = 255;
    private static final int ROOT_JNI_GLOBAL = 1;
    private static final int ROOT_JNI_LOCAL = 2;
    private static final int ROOT_JAVA_FRAME = 3;
    private static final int ROOT_NATIVE_STACK = 4;
    private static final int ROOT_STICKY_CLASS = 5;
    private static final int ROOT_THREAD_BLOCK = 6;
    private static final int ROOT_MONITOR_USED = 7;
    private static final int ROOT_THREAD_OBJECT = 8;
    private static final int CLASS_DUMP = 32;
    private static final int INSTANCE_DUMP = 33;
    private static final int OBJECT_ARRAY_DUMP = 34;
    private static final int PRIMITIVE_ARRAY_DUMP = 35;
    private static final int HEAP_DUMP_INFO = 254;
    private static final int ROOT_INTERNED_STRING = 137;
    private static final int ROOT_FINALIZING = 138;
    private static final int ROOT_DEBUGGER = 139;
    private static final int ROOT_REFERENCE_CLEANUP = 140;
    private static final int ROOT_VM_INTERNAL = 141;
    private static final int ROOT_JNI_MONITOR = 142;
    private static final int ROOT_UNREACHABLE = 144;
    private static final int PRIMITIVE_ARRAY_NODATA = 195;
    private final DataBuffer mInput;
    int mIdSize;
    Snapshot mSnapshot;
    private final ProguardMap mProguardMap;
    TLongObjectHashMap<String> mStrings = new TLongObjectHashMap();
    TLongObjectHashMap<String> mClassNamesById = new TLongObjectHashMap();
    TIntObjectHashMap<String> mClassNamesBySerial = new TIntObjectHashMap();

    static void parseBuffer(Snapshot snapshot, DataBuffer buffer, ProguardMap map) {
        (new HprofParser(snapshot, buffer, map)).parse();
    }

    private HprofParser(Snapshot snapshot, DataBuffer buffer, ProguardMap map) {
        this.mInput = buffer;
        this.mSnapshot = snapshot;
        this.mProguardMap = map;
    }

    private void parse() {
        try {
            try {
                this.readNullTerminatedString();
                this.mIdSize = this.mInput.readInt();
                this.mSnapshot.setIdSize(this.mIdSize);
                this.mInput.readLong();

                while(this.mInput.hasRemaining()) {
                    int tag = this.readUnsignedByte();
                    this.mInput.readInt();
                    long length = this.readUnsignedInt();
                    switch(tag) {
                        case 1:
                            this.loadString((int)length - this.mIdSize);
                            break;
                        case 2:
                            this.loadClass();
                            break;
                        case 4:
                            this.loadStackFrame();
                            break;
                        case 5:
                            this.loadStackTrace();
                            break;
                        case 12:
                            this.loadHeapDump(length);
                            this.mSnapshot.setToDefaultHeap();
                            break;
                        case 28:
                            this.loadHeapDump(length);
                            this.mSnapshot.setToDefaultHeap();
                            break;
                        default:
                            this.skipFully(length);
                    }
                }
            } catch (EOFException var4) {
            }

            this.mSnapshot.resolveClasses();
            this.mSnapshot.identifySoftReferences();
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        this.mClassNamesById.clear();
        this.mClassNamesBySerial.clear();
        this.mStrings.clear();
    }

    private String readNullTerminatedString() throws IOException {
        StringBuilder s = new StringBuilder();

        for(byte c = this.mInput.readByte(); c != 0; c = this.mInput.readByte()) {
            s.append((char)c);
        }

        return s.toString();
    }

    private long readId() throws IOException {
        switch(this.mIdSize) {
            case 1:
                return (long)this.mInput.readByte();
            case 2:
                return (long)this.mInput.readShort();
            case 3:
            case 5:
            case 6:
            case 7:
            default:
                throw new IllegalArgumentException("ID Length must be 1, 2, 4, or 8");
            case 4:
                return (long)this.mInput.readInt();
            case 8:
                return this.mInput.readLong();
        }
    }

    private String readUTF8(int length) throws IOException {
        byte[] b = new byte[length];
        this.mInput.read(b);
        return new String(b, "utf-8");
    }

    private int readUnsignedByte() throws IOException {
        return UnsignedBytes.toInt(this.mInput.readByte());
    }

    private int readUnsignedShort() throws IOException {
        return this.mInput.readShort() & '\uffff';
    }

    private long readUnsignedInt() throws IOException {
        return UnsignedInts.toLong(this.mInput.readInt());
    }

    private void loadString(int length) throws IOException {
        long id = this.readId();
        String string = this.readUTF8(length);
        this.mStrings.put(id, string);
    }

    private void loadClass() throws IOException {
        int serial = this.mInput.readInt();
        long id = this.readId();
        this.mInput.readInt();
        String name = (String)this.mStrings.get(this.readId());
        String className = this.mProguardMap.getClassName(name);
        this.mClassNamesById.put(id, className);
        this.mClassNamesBySerial.put(serial, className);
    }

    private void loadStackFrame() throws IOException {
        long id = this.readId();
        String methodName = (String)this.mStrings.get(this.readId());
        String methodSignature = (String)this.mStrings.get(this.readId());
        String sourceFile = (String)this.mStrings.get(this.readId());
        int serial = this.mInput.readInt();
        int lineNumber = this.mInput.readInt();
        String className = (String)this.mClassNamesBySerial.get(serial);
        ProguardMap.Frame frame = this.mProguardMap.getFrame(className, methodName, methodSignature, sourceFile, lineNumber);
        StackFrame stackFrame = new StackFrame(id, frame.methodName, frame.signature, frame.filename, serial, frame.line);
        this.mSnapshot.addStackFrame(stackFrame);
    }

    private void loadStackTrace() throws IOException {
        int serialNumber = this.mInput.readInt();
        int threadSerialNumber = this.mInput.readInt();
        int numFrames = this.mInput.readInt();
        StackFrame[] frames = new StackFrame[numFrames];

        for(int i = 0; i < numFrames; ++i) {
            frames[i] = this.mSnapshot.getStackFrame(this.readId());
        }

        StackTrace trace = new StackTrace(serialNumber, threadSerialNumber, frames);
        this.mSnapshot.addStackTrace(trace);
    }

    private void loadHeapDump(long length) throws IOException {
        while(length > 0L) {
            int tag = this.readUnsignedByte();
            --length;
            switch(tag) {
                case 1:
                    length -= (long)this.loadBasicObj(RootType.NATIVE_STATIC);
                    this.readId();
                    length -= (long)this.mIdSize;
                    break;
                case 2:
                    length -= (long)this.loadJniLocal();
                    break;
                case 3:
                    length -= (long)this.loadJavaFrame();
                    break;
                case 4:
                    length -= (long)this.loadNativeStack();
                    break;
                case 5:
                    length -= (long)this.loadBasicObj(RootType.SYSTEM_CLASS);
                    break;
                case 6:
                    length -= (long)this.loadThreadBlock();
                    break;
                case 7:
                    length -= (long)this.loadBasicObj(RootType.BUSY_MONITOR);
                    break;
                case 8:
                    length -= (long)this.loadThreadObject();
                    break;
                case 32:
                    length -= (long)this.loadClassDump();
                    break;
                case 33:
                    length -= (long)this.loadInstanceDump();
                    break;
                case 34:
                    length -= (long)this.loadObjectArrayDump();
                    break;
                case 35:
                    length -= (long)this.loadPrimitiveArrayDump();
                    break;
                case 137:
                    length -= (long)this.loadBasicObj(RootType.INTERNED_STRING);
                    break;
                case 138:
                    length -= (long)this.loadBasicObj(RootType.FINALIZING);
                    break;
                case 139:
                    length -= (long)this.loadBasicObj(RootType.DEBUGGER);
                    break;
                case 140:
                    length -= (long)this.loadBasicObj(RootType.REFERENCE_CLEANUP);
                    break;
                case 141:
                    length -= (long)this.loadBasicObj(RootType.VM_INTERNAL);
                    break;
                case 142:
                    length -= (long)this.loadJniMonitor();
                    break;
                case 144:
                    length -= (long)this.loadBasicObj(RootType.UNREACHABLE);
                    break;
                case 195:
                    System.err.println("+--- PRIMITIVE ARRAY NODATA DUMP");
                    long var10000 = length - (long)this.loadPrimitiveArrayDump();
                    throw new IllegalArgumentException("Don't know how to load a nodata array");
                case 254:
                    int heapId = this.mInput.readInt();
                    long heapNameId = this.readId();
                    String heapName = (String)this.mStrings.get(heapNameId);
                    this.mSnapshot.setHeapTo(heapId, heapName);
                    length -= (long)(4 + this.mIdSize);
                    break;
                case 255:
                    length -= (long)this.loadBasicObj(RootType.UNKNOWN);
                    break;
                default:
                    throw new IllegalArgumentException("loadHeapDump loop with unknown tag " + tag + " with " + this.mInput.remaining() + " bytes possibly remaining");
            }
        }

    }

    private int loadJniLocal() throws IOException {
        long id = this.readId();
        int threadSerialNumber = this.mInput.readInt();
        int stackFrameNumber = this.mInput.readInt();
        ThreadObj thread = this.mSnapshot.getThread(threadSerialNumber);
        StackTrace trace = this.mSnapshot.getStackTraceAtDepth(thread.mStackTrace, stackFrameNumber);
        RootObj root = new RootObj(RootType.NATIVE_LOCAL, id, threadSerialNumber, trace);
        this.mSnapshot.addRoot(root);
        return this.mIdSize + 4 + 4;
    }

    private int loadJavaFrame() throws IOException {
        long id = this.readId();
        int threadSerialNumber = this.mInput.readInt();
        int stackFrameNumber = this.mInput.readInt();
        ThreadObj thread = this.mSnapshot.getThread(threadSerialNumber);
        StackTrace trace = this.mSnapshot.getStackTraceAtDepth(thread.mStackTrace, stackFrameNumber);
        RootObj root = new RootObj(RootType.JAVA_LOCAL, id, threadSerialNumber, trace);
        this.mSnapshot.addRoot(root);
        return this.mIdSize + 4 + 4;
    }

    private int loadNativeStack() throws IOException {
        long id = this.readId();
        int threadSerialNumber = this.mInput.readInt();
        ThreadObj thread = this.mSnapshot.getThread(threadSerialNumber);
        StackTrace trace = this.mSnapshot.getStackTrace(thread.mStackTrace);
        RootObj root = new RootObj(RootType.NATIVE_STACK, id, threadSerialNumber, trace);
        this.mSnapshot.addRoot(root);
        return this.mIdSize + 4;
    }

    private int loadBasicObj(RootType type) throws IOException {
        long id = this.readId();
        RootObj root = new RootObj(type, id);
        this.mSnapshot.addRoot(root);
        return this.mIdSize;
    }

    private int loadThreadBlock() throws IOException {
        long id = this.readId();
        int threadSerialNumber = this.mInput.readInt();
        ThreadObj thread = this.mSnapshot.getThread(threadSerialNumber);
        StackTrace stack = this.mSnapshot.getStackTrace(thread.mStackTrace);
        RootObj root = new RootObj(RootType.THREAD_BLOCK, id, threadSerialNumber, stack);
        this.mSnapshot.addRoot(root);
        return this.mIdSize + 4;
    }

    private int loadThreadObject() throws IOException {
        long id = this.readId();
        int threadSerialNumber = this.mInput.readInt();
        int stackSerialNumber = this.mInput.readInt();
        ThreadObj thread = new ThreadObj(id, stackSerialNumber);
        StackTrace stack = this.mSnapshot.getStackTrace(stackSerialNumber);
        RootObj root = new RootObj(RootType.THREAD_OBJECT, id, threadSerialNumber, stack);
        this.mSnapshot.addThread(thread, threadSerialNumber);
        this.mSnapshot.addRoot(root);
        return this.mIdSize + 4 + 4;
    }

    private int loadClassDump() throws IOException {
        long id = this.readId();
        int stackSerialNumber = this.mInput.readInt();
        StackTrace stack = this.mSnapshot.getStackTrace(stackSerialNumber);
        long superClassId = this.readId();
        long classLoaderId = this.readId();
        this.readId();
        this.readId();
        this.readId();
        this.readId();
        int instanceSize = this.mInput.readInt();
        int bytesRead = 7 * this.mIdSize + 4 + 4;
        int numEntries = this.readUnsignedShort();
        bytesRead += 2;

        for(int i = 0; i < numEntries; ++i) {
            this.readUnsignedShort();
            bytesRead += 2 + this.skipValue();
        }

        String className = (String)this.mClassNamesById.get(id);
        ClassObj theClass = new ClassObj(id, stack, className, this.mInput.position());
        theClass.setSuperClassId(superClassId);
        theClass.setClassLoaderId(classLoaderId);
        numEntries = this.readUnsignedShort();
        bytesRead += 2;
        Field[] staticFields = new Field[numEntries];

        for(int i = 0; i < numEntries; ++i) {
            String name = this.mProguardMap.getFieldName(className, (String)this.mStrings.get(this.readId()));
            Type type = Type.getType(this.mInput.readByte());
            staticFields[i] = new Field(type, name);
            this.skipFully((long)this.mSnapshot.getTypeSize(type));
            bytesRead += this.mIdSize + 1 + this.mSnapshot.getTypeSize(type);
        }

        theClass.setStaticFields(staticFields);
        numEntries = this.readUnsignedShort();
        bytesRead += 2;
        Field[] fields = new Field[numEntries];

        for(int i = 0; i < numEntries; ++i) {
            String name = this.mProguardMap.getFieldName(className, (String)this.mStrings.get(this.readId()));
            Type type = Type.getType(this.readUnsignedByte());
            fields[i] = new Field(type, name);
            bytesRead += this.mIdSize + 1;
        }

        theClass.setFields(fields);
        theClass.setInstanceSize(instanceSize);
        this.mSnapshot.addClass(id, theClass);
        return bytesRead;
    }

    private int loadInstanceDump() throws IOException {
        long id = this.readId();
        int stackId = this.mInput.readInt();
        StackTrace stack = this.mSnapshot.getStackTrace(stackId);
        long classId = this.readId();
        int remaining = this.mInput.readInt();
        long position = this.mInput.position();
        ClassInstance instance = new ClassInstance(id, stack, position);
        instance.setClassId(classId);
        this.mSnapshot.addInstance(id, instance);
        this.skipFully((long)remaining);
        return this.mIdSize + 4 + this.mIdSize + 4 + remaining;
    }

    private int loadObjectArrayDump() throws IOException {
        long id = this.readId();
        int stackId = this.mInput.readInt();
        StackTrace stack = this.mSnapshot.getStackTrace(stackId);
        int numElements = this.mInput.readInt();
        long classId = this.readId();
        ArrayInstance array = new ArrayInstance(id, stack, Type.OBJECT, numElements, this.mInput.position());
        array.setClassId(classId);
        this.mSnapshot.addInstance(id, array);
        int remaining = numElements * this.mIdSize;
        this.skipFully((long)remaining);
        return this.mIdSize + 4 + 4 + this.mIdSize + remaining;
    }

    private int loadPrimitiveArrayDump() throws IOException {
        long id = this.readId();
        int stackId = this.mInput.readInt();
        StackTrace stack = this.mSnapshot.getStackTrace(stackId);
        int numElements = this.mInput.readInt();
        Type type = Type.getType(this.readUnsignedByte());
        int size = this.mSnapshot.getTypeSize(type);
        ArrayInstance array = new ArrayInstance(id, stack, type, numElements, this.mInput.position());
        this.mSnapshot.addInstance(id, array);
        int remaining = numElements * size;
        this.skipFully((long)remaining);
        return this.mIdSize + 4 + 4 + 1 + remaining;
    }

    private int loadJniMonitor() throws IOException {
        long id = this.readId();
        int threadSerialNumber = this.mInput.readInt();
        int stackDepth = this.mInput.readInt();
        ThreadObj thread = this.mSnapshot.getThread(threadSerialNumber);
        StackTrace trace = this.mSnapshot.getStackTraceAtDepth(thread.mStackTrace, stackDepth);
        RootObj root = new RootObj(RootType.NATIVE_MONITOR, id, threadSerialNumber, trace);
        this.mSnapshot.addRoot(root);
        return this.mIdSize + 4 + 4;
    }

    private int skipValue() throws IOException {
        Type type = Type.getType(this.readUnsignedByte());
        int size = this.mSnapshot.getTypeSize(type);
        this.skipFully((long)size);
        return size + 1;
    }

    private void skipFully(long numBytes) throws IOException {
        this.mInput.setPosition(this.mInput.position() + numBytes);
    }
}

