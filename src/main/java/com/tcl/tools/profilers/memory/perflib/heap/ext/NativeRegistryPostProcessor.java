package com.tcl.tools.profilers.memory.perflib.heap.ext;



import com.tcl.tools.profilers.memory.perflib.heap.ClassInstance;
import com.tcl.tools.profilers.memory.perflib.heap.ClassObj;
import com.tcl.tools.profilers.memory.perflib.heap.Instance;
import com.tcl.tools.profilers.memory.perflib.heap.Snapshot;

import java.util.Iterator;

public class NativeRegistryPostProcessor implements SnapshotPostProcessor {
    static final String CLEANER_CLASS = "sun.misc.Cleaner";
    static final String CLEANER_THUNK_CLASS = "libcore.util.NativeAllocationRegistry$CleanerThunk";
    static final String NATIVE_REGISTRY_CLASS = "libcore.util.NativeAllocationRegistry";
    private boolean myHasNativeAllocations;

    public NativeRegistryPostProcessor() {
    }

    public boolean getHasNativeAllocations() {
        return this.myHasNativeAllocations;
    }

    public void postProcess(Snapshot snapshot) {
        ClassObj cleanerClass = snapshot.findClass("sun.misc.Cleaner");
        if (cleanerClass != null) {
            Iterator var3 = cleanerClass.getInstancesList().iterator();

            while(true) {
                while(true) {
                    Instance inst;
                    ClassInstance thunk;
                    ClassObj thunkClass;
                    do {
                        do {
                            Object thunkValue;
                            do {
                                ClassInstance cleaner;
                                Object referent;
                                do {
                                    if (!var3.hasNext()) {
                                        return;
                                    }

                                    Instance cleanerInst = (Instance)var3.next();
                                    cleaner = (ClassInstance)cleanerInst;
                                    referent = getField(cleaner, "referent");
                                } while(!(referent instanceof Instance));

                                inst = (Instance)referent;
                                thunkValue = getField(cleaner, "thunk");
                            } while(!(thunkValue instanceof ClassInstance));

                            thunk = (ClassInstance)thunkValue;
                            thunkClass = thunk.getClassObj();
                        } while(thunkClass == null);
                    } while(!"libcore.util.NativeAllocationRegistry$CleanerThunk".equals(thunkClass.getClassName()));

                    Iterator var11 = thunk.getValues().iterator();

                    while(var11.hasNext()) {
                        ClassInstance.FieldValue thunkField = (ClassInstance.FieldValue)var11.next();
                        if (thunkField.getValue() instanceof ClassInstance) {
                            ClassInstance registry = (ClassInstance)thunkField.getValue();
                            ClassObj registryClass = registry.getClassObj();
                            if (registryClass != null && "libcore.util.NativeAllocationRegistry".equals(registryClass.getClassName())) {
                                Object sizeValue = getField(registry, "size");
                                if (sizeValue instanceof Long) {
                                    inst.setNativeSize((Long)sizeValue);
                                    this.myHasNativeAllocations = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static Object getField(ClassInstance cls, String name) {
        Iterator var2 = cls.getValues().iterator();

        ClassInstance.FieldValue field;
        do {
            if (!var2.hasNext()) {
                return null;
            }

            field = (ClassInstance.FieldValue)var2.next();
        } while(!name.equals(field.getField().getName()));

        return field.getValue();
    }
}
