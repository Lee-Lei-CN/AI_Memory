package com.tcl.tools.profilers.memory.perflib.heap.memoryanaluzer;



import com.tcl.tools.profilers.memory.perflib.analyzer.AnalysisReport;
import com.tcl.tools.profilers.memory.perflib.analyzer.AnalysisResultEntry;
import com.tcl.tools.profilers.memory.perflib.analyzer.Capture;
import com.tcl.tools.profilers.memory.perflib.analyzer.CaptureGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

final class TaskRunner {
    TaskRunner() {
    }

    static List<AnalysisResultEntry<?>> runTasks(Set<MemoryAnalyzerTask> tasks, Set<AnalysisReport.Listener> listeners, CaptureGroup captureGroup) {
        final List<AnalysisResultEntry<?>> generatedEntries = new ArrayList();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Set<AnalysisReport.Listener> listenerSet = new HashSet();
        listenerSet.addAll(listeners);
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean cancelledOrInterrupted = new AtomicBoolean(false);
        listenerSet.add(new AnalysisReport.Listener() {
            public void onResultsAdded(List<AnalysisResultEntry<?>> entries) {
                generatedEntries.addAll(entries);
            }

            public void onAnalysisComplete() {
                latch.countDown();
            }

            public void onAnalysisCancelled() {
                cancelledOrInterrupted.set(true);
                latch.countDown();
            }
        });
        MemoryAnalyzer memoryAnalyzer = new MemoryAnalyzer();
        memoryAnalyzer.analyze(captureGroup, listenerSet, tasks, executorService, executorService);

        try {
            latch.await();
        } catch (InterruptedException var10) {
            cancelledOrInterrupted.set(true);
        }

        executorService.shutdownNow();
        return !cancelledOrInterrupted.get() ? generatedEntries : null;
    }

    static List<AnalysisResultEntry<?>> runTasks(Set<MemoryAnalyzerTask> tasks, CaptureGroup captureGroup) {
        return runTasks(tasks, Collections.emptySet(), captureGroup);
    }

    static List<AnalysisResultEntry<?>> runTasks(Set<MemoryAnalyzerTask> tasks, Capture... captures) {
        CaptureGroup captureGroup = new CaptureGroup();
        Capture[] var3 = captures;
        int var4 = captures.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            Capture capture = var3[var5];
            captureGroup.addCapture(capture);
        }

        return runTasks(tasks, Collections.emptySet(), captureGroup);
    }
}
