package com.tcl.tools.profilers.memory.perflib.heap;

import gnu.trove.TObjectProcedure;

import java.util.*;

public class Queries {
    private static final String DEFAULT_PACKAGE = "<default>";

    public Queries() {
    }

    public static Map<String, Set<ClassObj>> allClasses(Snapshot snapshot) {
        return classes(snapshot, (String[])null);
    }

    public static Map<String, Set<ClassObj>> classes(Snapshot snapshot, String[] excludedPrefixes) {
        TreeMap<String, Set<ClassObj>> result = new TreeMap();
        Set<ClassObj> classes = new TreeSet();
        Iterator var4 = snapshot.mHeaps.iterator();

        while(var4.hasNext()) {
            Heap heap = (Heap)var4.next();
            classes.addAll(heap.getClasses());
        }

        if (excludedPrefixes != null) {
            int N = excludedPrefixes.length;
            Iterator iter = classes.iterator();

            label44:
            while(true) {
                while(true) {
                    if (!iter.hasNext()) {
                        break label44;
                    }

                    ClassObj theClass = (ClassObj)iter.next();
                    String classPath = theClass.toString();

                    for(int i = 0; i < N; ++i) {
                        if (classPath.startsWith(excludedPrefixes[i])) {
                            iter.remove();
                            break;
                        }
                    }
                }
            }
        }

        ClassObj theClass;
        Set<ClassObj> classSet;
        for(var4 = classes.iterator(); var4.hasNext(); ((Set)classSet).add(theClass)) {
            theClass = (ClassObj)var4.next();
            String packageName = "<default>";
            int lastDot = theClass.mClassName.lastIndexOf(46);
            if (lastDot != -1) {
                packageName = theClass.mClassName.substring(0, lastDot);
            }

            classSet = (Set)result.get(packageName);
            if (classSet == null) {
                classSet = new TreeSet();
                result.put(packageName, classSet);
            }
        }

        return result;
    }

    public static Collection<ClassObj> commonClasses(Snapshot first, Snapshot second) {
        Collection<ClassObj> classes = new ArrayList();
        Iterator var3 = first.getHeaps().iterator();

        while(var3.hasNext()) {
            Heap heap = (Heap)var3.next();
            Iterator var5 = heap.getClasses().iterator();

            while(var5.hasNext()) {
                ClassObj clazz = (ClassObj)var5.next();
                if (second.findClass(clazz.getClassName()) != null) {
                    classes.add(clazz);
                }
            }
        }

        return classes;
    }

    public static ClassObj findClass(Snapshot snapshot, String name) {
        return snapshot.findClass(name);
    }

    public static Instance[] instancesOf(Snapshot snapshot, String baseClassName) {
        ClassObj theClass = snapshot.findClass(baseClassName);
        if (theClass == null) {
            throw new IllegalArgumentException("Class not found: " + baseClassName);
        } else {
            List<Instance> instances = theClass.getInstancesList();
            return (Instance[])instances.toArray(new Instance[instances.size()]);
        }
    }

    public static Instance[] allInstancesOf(Snapshot snapshot, String baseClassName) {
        ClassObj theClass = snapshot.findClass(baseClassName);
        if (theClass == null) {
            throw new IllegalArgumentException("Class not found: " + baseClassName);
        } else {
            ArrayList<ClassObj> classList = new ArrayList();
            classList.add(theClass);
            classList.addAll(traverseSubclasses(theClass));
            ArrayList<Instance> instanceList = new ArrayList();
            Iterator var5 = classList.iterator();

            while(var5.hasNext()) {
                ClassObj someClass = (ClassObj)var5.next();
                instanceList.addAll(someClass.getInstancesList());
            }

            Instance[] result = new Instance[instanceList.size()];
            instanceList.toArray(result);
            return result;
        }
    }

    private static ArrayList<ClassObj> traverseSubclasses(ClassObj base) {
        ArrayList<ClassObj> result = new ArrayList();
        Iterator var2 = base.mSubclasses.iterator();

        while(var2.hasNext()) {
            ClassObj subclass = (ClassObj)var2.next();
            result.add(subclass);
            result.addAll(traverseSubclasses(subclass));
        }

        return result;
    }

    public static Instance findObject(Snapshot snapshot, String id) {
        long id2 = Long.parseLong(id, 16);
        return snapshot.findInstance(id2);
    }

    /** @deprecated */
    @Deprecated
    public static Collection<RootObj> getRoots(Snapshot snapshot) {
        return snapshot.getGCRoots();
    }

    public static final Instance[] newInstances(Snapshot older, Snapshot newer) {
        final ArrayList<Instance> resultList = new ArrayList();
        Iterator var3 = newer.mHeaps.iterator();

        while(var3.hasNext()) {
            Heap newHeap = (Heap)var3.next();
            final Heap oldHeap = older.getHeap(newHeap.getName());
            if (oldHeap != null) {
                newHeap.forEachInstance(new TObjectProcedure<Instance>() {
                    public boolean execute(Instance instance) {
                        Instance oldInstance = oldHeap.getInstance(instance.mId);
                        if (oldInstance == null || instance.getClassObj() != oldInstance.getClassObj()) {
                            resultList.add(instance);
                        }

                        return true;
                    }
                });
            }
        }

        Instance[] resultArray = new Instance[resultList.size()];
        return (Instance[])resultList.toArray(resultArray);
    }
}
