package com.tcl.tools.profilers.memory.perflib.heap.memoryanaluzer;


import com.tcl.tools.profilers.memory.perflib.analyzer.AnalysisResultEntry;
import com.tcl.tools.profilers.memory.perflib.heap.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public final class DuplicatedBitmapAnalyzerTask extends MemoryAnalyzerTask {
    public DuplicatedBitmapAnalyzerTask() {
    }

    protected List<AnalysisResultEntry<?>> analyze(Configuration configuration, Snapshot snapshot) {
        ClassObj bitmapClass = snapshot.findClass("android.graphics.Bitmap");
        if (bitmapClass == null) {
            return Collections.emptyList();
        } else {
            Map<ArrayInstance, Instance> byteArrayToBitmapMap = new HashMap();
            Set<ArrayInstance> byteArrays = new HashSet();
            List<Instance> reachableInstances = new ArrayList();
            configuration.mHeaps.stream().forEach((heap) -> {
                // 获取堆实例并过滤
                Stream<Instance> filteredInstances = bitmapClass.getHeapInstances(heap.getId())
                        .stream()
                        .filter((instance) -> instance.getDistanceToGcRoot() != Integer.MAX_VALUE);

                // 确保 reachableInstances 非空并添加筛选后的实例
                Objects.requireNonNull(reachableInstances);
                filteredInstances.forEach(instance -> reachableInstances.add(instance));
            });
            reachableInstances.forEach((instancex) -> {
                byteArrayToBitmapMap.put((ArrayInstance)((ClassInstance.FieldValue)((ClassInstance)instancex).getValues().stream().filter((fieldValue) -> {
                    return fieldValue.getField().getName().equals("mBuffer");
                }).findFirst().get()).getValue(), instancex);
            });
            byteArrays.addAll(byteArrayToBitmapMap.keySet());
            if (byteArrays.size() <= 1) {
                return Collections.emptyList();
            } else {
                List<AnalysisResultEntry<?>> results = new ArrayList();
                List<Set<ArrayInstance>> commonPrefixSets = new ArrayList();
                List<Set<ArrayInstance>> reducedPrefixSets = new ArrayList();
                commonPrefixSets.add(byteArrays);
                Map<ArrayInstance, Object[]> cachedValues = new HashMap();
                cachedValues.clear();
                Iterator var11 = byteArrays.iterator();

                while(var11.hasNext()) {
                    ArrayInstance instance = (ArrayInstance)var11.next();
                    cachedValues.put(instance, instance.getValues());
                }

                for(int columnIndex = 0; !commonPrefixSets.isEmpty(); ++columnIndex) {
                    Iterator var23 = commonPrefixSets.iterator();

                    label84:
                    while(var23.hasNext()) {
                        Set<ArrayInstance> commonPrefixArrays = (Set)var23.next();
                        Map<Object, Set<ArrayInstance>> entryClassifier = new HashMap(commonPrefixArrays.size());
                        Iterator var15 = commonPrefixArrays.iterator();

                        while(var15.hasNext()) {
                            ArrayInstance arrayInstance = (ArrayInstance)var15.next();
                            Object element = ((Object[])cachedValues.get(arrayInstance))[columnIndex];
                            if (entryClassifier.containsKey(element)) {
                                ((Set)entryClassifier.get(element)).add(arrayInstance);
                            } else {
                                Set<ArrayInstance> instanceSet = new HashSet();
                                instanceSet.add(arrayInstance);
                                entryClassifier.put(element, instanceSet);
                            }
                        }

                        var15 = entryClassifier.values().iterator();

                        while(true) {
                            Set branch;
                            do {
                                if (!var15.hasNext()) {
                                    continue label84;
                                }

                                branch = (Set)var15.next();
                            } while(branch.size() <= 1);

                            Set<ArrayInstance> terminatedArrays = new HashSet();
                            Iterator var26 = branch.iterator();

                            while(var26.hasNext()) {
                                ArrayInstance instance = (ArrayInstance)var26.next();
                                if (instance.getLength() == columnIndex + 1) {
                                    terminatedArrays.add(instance);
                                }
                            }

                            branch.removeAll(terminatedArrays);
                            if (terminatedArrays.size() > 1) {
                                int byteArraySize = -1;
                                ArrayList<Instance> duplicateBitmaps = new ArrayList();

                                ArrayInstance terminatedArray;
                                for(Iterator var20 = terminatedArrays.iterator(); var20.hasNext(); byteArraySize = terminatedArray.getLength()) {
                                    terminatedArray = (ArrayInstance)var20.next();
                                    duplicateBitmaps.add((Instance)byteArrayToBitmapMap.get(terminatedArray));
                                }

                                results.add(new DuplicatedBitmapAnalyzerTask.DuplicatedBitmapEntry(new ArrayList(duplicateBitmaps), byteArraySize));
                            }

                            if (branch.size() > 1) {
                                reducedPrefixSets.add(branch);
                            }
                        }
                    }

                    commonPrefixSets.clear();
                    commonPrefixSets.addAll(reducedPrefixSets);
                    reducedPrefixSets.clear();
                }

                return results;
            }
        }
    }

    public String getTaskName() {
        return "Duplicated Bitmaps";
    }

    public String getTaskDescription() {
        return "Detects duplicated bitmaps in the application.";
    }

    public static final class DuplicatedBitmapEntry extends MemoryAnalysisResultEntry {
        private final int mByteArraySize;

        private DuplicatedBitmapEntry(List<Instance> duplicates, int byteArraySize) {
            super("Duplicated Bitmap", duplicates);
            this.mByteArraySize = byteArraySize;
        }

        public String getWarningMessage() {
            return String.format("%d instances: \"%s\"", this.mOffender.getOffenders().size(), this.mOffender.getOffendingDescription());
        }

        public String getCategory() {
            return "Duplicated Bitmaps";
        }

        public int getByteArraySize() {
            return this.mByteArraySize;
        }
    }
}
