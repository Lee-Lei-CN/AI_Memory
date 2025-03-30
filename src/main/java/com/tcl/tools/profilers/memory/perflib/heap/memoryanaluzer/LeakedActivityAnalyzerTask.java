package com.tcl.tools.profilers.memory.perflib.heap.memoryanaluzer;


import com.android.tools.perflib.analyzer.AnalysisResultEntry;
import com.android.tools.perflib.heap.ClassInstance;
import com.android.tools.perflib.heap.ClassObj;
import com.android.tools.perflib.heap.Heap;
import com.android.tools.perflib.heap.Instance;
import com.android.tools.perflib.heap.Snapshot;
import com.android.tools.perflib.heap.ClassInstance.FieldValue;
import com.android.tools.perflib.heap.memoryanalyzer.MemoryAnalysisResultEntry;
import com.android.tools.perflib.heap.memoryanalyzer.MemoryAnalyzerTask;
import com.android.tools.perflib.heap.memoryanalyzer.MemoryAnalyzerTask.Configuration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class LeakedActivityAnalyzerTask extends MemoryAnalyzerTask {
    public LeakedActivityAnalyzerTask() {
    }

    protected List<AnalysisResultEntry<?>> analyze(Configuration configuration, Snapshot snapshot) {
        List<Instance> leakingInstances = new ArrayList();
        List<ClassObj> activityClasses = snapshot.findAllDescendantClasses("android.app.Activity");
        Iterator var5 = activityClasses.iterator();

        label72:
        while(var5.hasNext()) {
            ClassObj activityClass = (ClassObj)var5.next();
            List<Instance> instances = new ArrayList();
            Iterator var8 = configuration.mHeaps.iterator();

            while(var8.hasNext()) {
                Heap heap = (Heap)var8.next();
                instances.addAll(activityClass.getHeapInstances(heap.getId()));
            }

            var8 = instances.iterator();

            while(true) {
                label68:
                while(true) {
                    Instance immediateDominator;
                    Instance instance;
                    do {
                        do {
                            if (!var8.hasNext()) {
                                continue label72;
                            }

                            instance = (Instance)var8.next();
                            immediateDominator = instance.getImmediateDominator();
                        } while(!(instance instanceof ClassInstance));
                    } while(immediateDominator == null);

                    Iterator var11 = ((ClassInstance)instance).getValues().iterator();

                    FieldValue value;
                    do {
                        do {
                            if (!var11.hasNext()) {
                                continue label68;
                            }

                            value = (FieldValue)var11.next();
                        } while(!"mFinished".equals(value.getField().getName()) && !"mDestroyed".equals(value.getField().getName()));
                    } while(instance.getDistanceToGcRoot() == 2147483647 || !(value.getValue() instanceof Boolean) || !(Boolean)value.getValue());

                    leakingInstances.add(instance);
                }
            }
        }

        List<AnalysisResultEntry<?>> results = new ArrayList(leakingInstances.size());
        Iterator var14 = leakingInstances.iterator();

        while(var14.hasNext()) {
            Instance instance = (Instance)var14.next();
            results.add(new LeakedActivityEntry(instance.getClassObj().getClassName(), instance));
        }

        return results;
    }

    public String getTaskName() {
        return "Detect Leaked Activities";
    }

    public String getTaskDescription() {
        return "Detects leaked activities in Android applications.";
    }

    public static class LeakedActivityEntry extends MemoryAnalysisResultEntry {
        private LeakedActivityEntry(String offenseDescription, Instance offendingInstance) {
            super(offenseDescription, Collections.singletonList(offendingInstance));
        }

        public String getWarningMessage() {
            return this.mOffender.getOffendingDescription();
        }

        public String getCategory() {
            return "Leaked Activities";
        }
    }
}
