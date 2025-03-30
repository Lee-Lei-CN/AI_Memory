package com.tcl.tools.profilers.memory.perflib.heap.hprof;



import java.io.IOException;

public class HprofClassDump implements HprofDumpRecord {
    public static final byte SUBTAG = 32;
    public final long classObjectId;
    public final int stackTraceSerialNumber;
    public final long superClassObjectId;
    public final long classLoaderObjectId;
    public final long signersObjectId;
    public final long protectionDomainObjectId;
    public final long reserved1;
    public final long reserved2;
    public final int instanceSize;
    public final HprofConstant[] constantPool;
    public final HprofStaticField[] staticFields;
    public final HprofInstanceField[] instanceFields;

    public HprofClassDump(long classObjectId, int stackTraceSerialNumber, long superClassObjectId, long classLoaderObjectId, long signersObjectId, long protectionDomainObjectId, long reserved1, long reserved2, int instanceSize, HprofConstant[] constantPool, HprofStaticField[] staticFields, HprofInstanceField[] instanceFields) {
        this.classObjectId = classObjectId;
        this.stackTraceSerialNumber = stackTraceSerialNumber;
        this.superClassObjectId = superClassObjectId;
        this.classLoaderObjectId = classLoaderObjectId;
        this.signersObjectId = signersObjectId;
        this.protectionDomainObjectId = protectionDomainObjectId;
        this.reserved1 = reserved1;
        this.reserved2 = reserved2;
        this.instanceSize = instanceSize;
        this.constantPool = constantPool;
        this.staticFields = staticFields;
        this.instanceFields = instanceFields;
    }

    public void write(HprofOutputStream hprof) throws IOException {
        hprof.writeU1((byte)32);
        hprof.writeId(this.classObjectId);
        hprof.writeU4(this.stackTraceSerialNumber);
        hprof.writeId(this.superClassObjectId);
        hprof.writeId(this.classLoaderObjectId);
        hprof.writeId(this.signersObjectId);
        hprof.writeId(this.protectionDomainObjectId);
        hprof.writeId(this.reserved1);
        hprof.writeId(this.reserved2);
        hprof.writeU4(this.instanceSize);
        hprof.writeU2((short)this.constantPool.length);
        HprofConstant[] var2 = this.constantPool;
        int var3 = var2.length;

        int var4;
        for(var4 = 0; var4 < var3; ++var4) {
            HprofConstant constant = var2[var4];
            constant.write(hprof);
        }

        hprof.writeU2((short)this.staticFields.length);
        HprofStaticField[] var6 = this.staticFields;
        var3 = var6.length;

        for(var4 = 0; var4 < var3; ++var4) {
            HprofStaticField field = var6[var4];
            field.write(hprof);
        }

        hprof.writeU2((short)this.instanceFields.length);
        HprofInstanceField[] var7 = this.instanceFields;
        var3 = var7.length;

        for(var4 = 0; var4 < var3; ++var4) {
            HprofInstanceField field = var7[var4];
            field.write(hprof);
        }

    }

    public int getLength(int idSize) {
        int length = 1 + 7 * idSize + 8 + 6;
        HprofConstant[] var3 = this.constantPool;
        int var4 = var3.length;

        int var5;
        for(var5 = 0; var5 < var4; ++var5) {
            HprofConstant constant = var3[var5];
            length += constant.getLength(idSize);
        }

        HprofStaticField[] var7 = this.staticFields;
        var4 = var7.length;

        for(var5 = 0; var5 < var4; ++var5) {
            HprofStaticField field = var7[var5];
            length += field.getLength(idSize);
        }

        HprofInstanceField[] var8 = this.instanceFields;
        var4 = var8.length;

        for(var5 = 0; var5 < var4; ++var5) {
            HprofInstanceField field = var8[var5];
            length += field.getLength(idSize);
        }

        return length;
    }
}
