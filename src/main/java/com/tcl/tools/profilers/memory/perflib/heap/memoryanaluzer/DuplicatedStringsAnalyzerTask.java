package com.tcl.tools.profilers.memory.perflib.heap.memoryanaluzer;


import com.google.common.collect.HashMultimap;
import com.tcl.tools.profilers.memory.perflib.analyzer.AnalysisResultEntry;
import com.tcl.tools.profilers.memory.perflib.heap.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DuplicatedStringsAnalyzerTask extends MemoryAnalyzerTask {
    public DuplicatedStringsAnalyzerTask() {
    }

    protected List<AnalysisResultEntry<?>> analyze(Configuration configuration, Snapshot snapshot) {
        List<AnalysisResultEntry<?>> results = new ArrayList();
        HashMultimap<String, ClassInstance> stringIndex = HashMultimap.create();
        ClassObj stringClass = snapshot.findClass("java.lang.String");
        if (stringClass == null) {
            return Collections.emptyList();
        } else {
            Iterator var6 = configuration.mHeaps.iterator();

            while(var6.hasNext()) {
                Heap heap = (Heap)var6.next();
                List<Instance> instances = stringClass.getHeapInstances(heap.getId());
                Iterator var9 = instances.iterator();

                while(var9.hasNext()) {
                    Instance instance = (Instance)var9.next();

                    assert instance instanceof ClassInstance;

                    ClassInstance stringInstance = (ClassInstance)instance;
                    if (stringInstance.getDistanceToGcRoot() != 2147483647) {
                        String text = stringInstance.getAsString();
                        if (text != null) {
                            stringIndex.put(text, stringInstance);
                        }
                    }
                }
            }

            var6 = stringIndex.keySet().iterator();

            while(var6.hasNext()) {
                String key = (String)var6.next();
                Set<ClassInstance> classInstanceSet = stringIndex.get(key);
                if (classInstanceSet.size() > 1) {
                    results.add(new DuplicatedStringsEntry(key, new ArrayList(classInstanceSet)));
                }
            }

            return results;
        }
    }

    public String getTaskName() {
        return "Find Duplicate Strings";
    }

    public String getTaskDescription() {
        return "Detects duplicate strings in the application.";
    }

    public static class DuplicatedStringsEntry extends MemoryAnalysisResultEntry {
        private DuplicatedStringsEntry(String offendingString, List<Instance> duplicates) {
            super(offendingString, duplicates);
        }

        public String getWarningMessage() {
            return String.format("%d instances: \"%s\"", this.mOffender.getOffenders().size(), this.mOffender.getOffendingDescription());
        }

        public String getCategory() {
            return "Duplicated Strings";
        }
    }
}
