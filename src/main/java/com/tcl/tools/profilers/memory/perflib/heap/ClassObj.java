package com.tcl.tools.profilers.memory.perflib.heap;


import gnu.trove.TIntObjectHashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Map.Entry;

public class ClassObj extends Instance implements Comparable<ClassObj> {
    final String mClassName;
    private final long mStaticFieldsOffset;
    long mSuperClassId;
    long mClassLoaderId;
    Field[] mFields;
    Field[] mStaticFields;
    private int mInstanceSize;
    private boolean mIsSoftReference = false;
    TIntObjectHashMap<HeapData> mHeapData = new TIntObjectHashMap();
    Set<ClassObj> mSubclasses = new HashSet();

    public ClassObj(long id, StackTrace stack, String className, long staticFieldsOffset) {
        super(id, stack);
        this.mClassName = className;
        this.mStaticFieldsOffset = staticFieldsOffset;
    }

    public final void addSubclass(ClassObj subclass) {
        this.mSubclasses.add(subclass);
    }

    public final Set<ClassObj> getSubclasses() {
        return this.mSubclasses;
    }

    public final void dumpSubclasses() {
        Iterator var1 = this.mSubclasses.iterator();

        while(var1.hasNext()) {
            ClassObj subclass = (ClassObj)var1.next();
            System.out.println("     " + subclass.mClassName);
        }

    }

    public final String toString() {
        return this.mClassName.replace('/', '.');
    }

    public final void addInstance(int heapId, Instance instance) {
        if (instance instanceof ClassInstance) {
            instance.setSize(this.mInstanceSize);
        }

        HeapData heapData = (HeapData)this.mHeapData.get(heapId);
        if (heapData == null) {
            heapData = new HeapData();
            this.mHeapData.put(heapId, heapData);
        }

        heapData.mInstances.add(instance);
        heapData.mShallowSize += instance.getSize();
    }

    public final void setSuperClassId(long superClass) {
        this.mSuperClassId = superClass;
    }

    public final void setClassLoaderId(long classLoader) {
        this.mClassLoaderId = classLoader;
    }

    public final long getClassLoaderId() {
        return this.mClassLoaderId;
    }

    public int getAllFieldsCount() {
        int result = 0;

        for(ClassObj clazz = this; clazz != null; clazz = clazz.getSuperClassObj()) {
            result += clazz.getFields().length;
        }

        return result;
    }

    public Field[] getFields() {
        return this.mFields;
    }

    public void setFields(Field[] fields) {
        this.mFields = fields;
    }

    public void setStaticFields(Field[] staticFields) {
        this.mStaticFields = staticFields;
    }

    public void setInstanceSize(int size) {
        this.mInstanceSize = size;
    }

    public int getInstanceSize() {
        return this.mInstanceSize;
    }

    public int getShallowSize(int heapId) {
        HeapData heapData = (HeapData)this.mHeapData.get(heapId);
        return heapData == null ? 0 : ((HeapData)this.mHeapData.get(heapId)).mShallowSize;
    }

    public void setIsSoftReference() {
        this.mIsSoftReference = true;
    }

    public boolean getIsSoftReference() {
        return this.mIsSoftReference;
    }

    public Map<Field, Object> getStaticFieldValues() {
        Map<Field, Object> result = new HashMap();
        this.getBuffer().setPosition(this.mStaticFieldsOffset);
        int numEntries = this.readUnsignedShort();

        for(int i = 0; i < numEntries; ++i) {
            Field f = this.mStaticFields[i];
            this.readId();
            this.readUnsignedByte();
            Object value = this.readValue(f.getType());
            result.put(f, value);
        }

        return result;
    }

    public final void dump() {
        System.out.println("+----------  ClassObj dump for: " + this.mClassName);
        System.out.println("+-----  Static fields");
        Map<Field, Object> staticFields = this.getStaticFieldValues();
        Iterator var2 = staticFields.keySet().iterator();

        while(var2.hasNext()) {
            Field field = (Field)var2.next();
            System.out.println(field.getName() + ": " + field.getType() + " = " + staticFields.get(field));
        }

        System.out.println("+-----  Instance fields");
        Field[] var6 = this.mFields;
        int var7 = var6.length;

        for(int var4 = 0; var4 < var7; ++var4) {
            Field field = var6[var4];
            System.out.println(field.getName() + ": " + field.getType());
        }

        if (this.getSuperClassObj() != null) {
            this.getSuperClassObj().dump();
        }

    }

    public final String getClassName() {
        return this.mClassName;
    }

    public final void resolveReferences() {
        Iterator var1 = this.getStaticFieldValues().entrySet().iterator();

        while(var1.hasNext()) {
            Entry<Field, Object> entry = (Entry)var1.next();
            Object value = entry.getValue();
            if (value instanceof Instance) {
                ((Instance)value).addReverseReference((Field)entry.getKey(), this);
                this.mHardForwardReferences.add((Instance)value);
            }
        }

    }

    public final void accept(Visitor visitor) {
        visitor.visitClassObj(this);
        Iterator var2 = this.mHardForwardReferences.iterator();

        while(var2.hasNext()) {
            Instance instance = (Instance)var2.next();
            visitor.visitLater(this, instance);
        }

    }

    public final int compareTo(ClassObj o) {
        if (this.getId() == o.getId()) {
            return 0;
        } else {
            int nameCompareResult = this.mClassName.compareTo(o.mClassName);
            if (nameCompareResult != 0) {
                return nameCompareResult;
            } else {
                return this.getId() - o.getId() > 0L ? 1 : -1;
            }
        }
    }

    public final boolean equals(Object o) {
        if (!(o instanceof ClassObj)) {
            return false;
        } else {
            return 0 == this.compareTo((ClassObj)o);
        }
    }

    public int hashCode() {
        return this.mClassName.hashCode();
    }

    Object getStaticField(Type type, String name) {
        return this.getStaticFieldValues().get(new Field(type, name));
    }

    public ClassObj getSuperClassObj() {
        return this.mHeap.mSnapshot.findClass(this.mSuperClassId);
    }

    public Instance getClassLoader() {
        return this.mHeap.mSnapshot.findInstance(this.mClassLoaderId);
    }

    public List<Instance> getInstancesList() {
        int count = this.getInstanceCount();
        ArrayList<Instance> resultList = new ArrayList(count);
        int[] var3 = this.mHeapData.keys();
        int var4 = var3.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            int heapId = var3[var5];
            resultList.addAll(this.getHeapInstances(heapId));
        }

        return resultList;
    }

    public List<Instance> getHeapInstances(int heapId) {
        HeapData result = (HeapData)this.mHeapData.get(heapId);
        return (List)(result == null ? new ArrayList(0) : result.mInstances);
    }

    public int getHeapInstancesCount(int heapId) {
        HeapData result = (HeapData)this.mHeapData.get(heapId);
        return result == null ? 0 : result.mInstances.size();
    }

    public int getInstanceCount() {
        int count = 0;
        Object[] var2 = this.mHeapData.getValues();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            Object heapStat = var2[var4];
            count += ((HeapData)heapStat).mInstances.size();
        }

        return count;
    }

    public int getShallowSize() {
        int size = 0;
        Object[] var2 = this.mHeapData.getValues();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            Object heapStat = var2[var4];
            size += ((HeapData)heapStat).mShallowSize;
        }

        return size;
    }

    public static String getReferenceClassName() {
        return "java.lang.ref.Reference";
    }

    public List<ClassObj> getDescendantClasses() {
        List<ClassObj> descendants = new ArrayList();
        Stack<ClassObj> searchStack = new Stack();
        searchStack.push(this);

        while(!searchStack.isEmpty()) {
            ClassObj classObj = (ClassObj)searchStack.pop();
            descendants.add(classObj);
            Iterator var4 = classObj.getSubclasses().iterator();

            while(var4.hasNext()) {
                ClassObj subClass = (ClassObj)var4.next();
                searchStack.push(subClass);
            }
        }

        return descendants;
    }

    public static class HeapData {
        public int mShallowSize = 0;
        public List<Instance> mInstances = new ArrayList();

        public HeapData() {
        }
    }
}
