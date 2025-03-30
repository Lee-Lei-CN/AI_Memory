package com.tcl.tools.profilers.memory.perflib.heap;


import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TLongObjectHashMap;
import gnu.trove.TObjectProcedure;
import java.util.Collection;
import java.util.Iterator;

public class Heap {
    private final int mId;
    private final String mName;
    TIntObjectHashMap<ThreadObj> mThreads = new TIntObjectHashMap();
    TLongObjectHashMap<ClassObj> mClassesById = new TLongObjectHashMap();
    Multimap<String, ClassObj> mClassesByName = ArrayListMultimap.create();
    private final TLongObjectHashMap<Instance> mInstances = new TLongObjectHashMap();
    Snapshot mSnapshot;

    public Heap(int id, String name) {
        this.mId = id;
        this.mName = name;
    }

    public int getId() {
        return this.mId;
    }

    public String getName() {
        return this.mName;
    }

    public final void addThread(ThreadObj thread, int serialNumber) {
        this.mThreads.put(serialNumber, thread);
    }

    public final ThreadObj getThread(int serialNumber) {
        return (ThreadObj)this.mThreads.get(serialNumber);
    }

    public final void addInstance(long id, Instance instance) {
        this.mInstances.put(id, instance);
    }

    public final Instance getInstance(long id) {
        return (Instance)this.mInstances.get(id);
    }

    public final void addClass(long id, ClassObj theClass) {
        this.mClassesById.put(id, theClass);
        this.mClassesByName.put(theClass.mClassName, theClass);
    }

    public final ClassObj getClass(long id) {
        return (ClassObj)this.mClassesById.get(id);
    }

    public final ClassObj getClass(String name) {
        Collection<ClassObj> classes = this.mClassesByName.get(name);
        return classes.size() == 1 ? (ClassObj)classes.iterator().next() : null;
    }

    public final Collection<ClassObj> getClasses(String name) {
        return this.mClassesByName.get(name);
    }

    public final void dumpInstanceCounts() {
        Object[] var1 = this.mClassesById.getValues();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            Object value = var1[var3];
            ClassObj theClass = (ClassObj)value;
            int count = theClass.getInstanceCount();
            if (count > 0) {
                System.out.println(theClass + ": " + count);
            }
        }

    }

    public final void dumpSubclasses() {
        Object[] var1 = this.mClassesById.getValues();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            Object value = var1[var3];
            ClassObj theClass = (ClassObj)value;
            int count = theClass.mSubclasses.size();
            if (count > 0) {
                System.out.println(theClass);
                theClass.dumpSubclasses();
            }
        }

    }

    public final void dumpSizes() {
        Object[] var1 = this.mClassesById.getValues();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            Object value = var1[var3];
            ClassObj theClass = (ClassObj)value;
            int size = 0;

            Instance instance;
            for(Iterator var7 = theClass.getHeapInstances(this.getId()).iterator(); var7.hasNext(); size += instance.getCompositeSize()) {
                instance = (Instance)var7.next();
            }

            if (size > 0) {
                System.out.println(theClass + ": base " + theClass.getSize() + ", composite " + size);
            }
        }

    }

    public Collection<ClassObj> getClasses() {
        return this.mClassesByName.values();
    }

    public void forEachInstance(TObjectProcedure<Instance> procedure) {
        this.mInstances.forEachValue(procedure);
    }

    public int getInstancesCount() {
        return this.mInstances.size();
    }
}
