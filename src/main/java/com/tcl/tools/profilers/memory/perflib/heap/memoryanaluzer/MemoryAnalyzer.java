package com.tcl.tools.profilers.memory.perflib.heap.memoryanaluzer;


import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.tcl.tools.profilers.memory.perflib.analyzer.*;
import com.tcl.tools.profilers.memory.perflib.heap.Heap;
import com.tcl.tools.profilers.memory.perflib.heap.Snapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

public class MemoryAnalyzer extends Analyzer {
    private Set<MemoryAnalyzerTask> mTasks = new HashSet();
    private AnalysisReport mOutstandingReport;
    private ListenableFuture<List<List<AnalysisResultEntry<?>>>> mRunningAnalyzers;
    private volatile boolean mCancelAnalysis = false;
    private boolean mAnalysisComplete = false;

    public MemoryAnalyzer() {
    }

    private static boolean accept(Capture capture) {
        return "hprof".equals(capture.getTypeName());
    }

    public boolean accept(CaptureGroup captureGroup) {
        Iterator var2 = captureGroup.getCaptures().iterator();

        Capture capture;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            capture = (Capture)var2.next();
        } while(!accept(capture));

        return true;
    }
// TODO: 2025/3/30 代码修改
//    public AnalysisReport analyze(CaptureGroup captureGroup, Set<AnalysisReport.Listener> listeners, Set<? extends AnalyzerTask> tasks, final Executor synchronizingExecutor, ExecutorService taskExecutor) {
//        if (this.mOutstandingReport != null) {
//            return this.mOutstandingReport;
//        } else {
//            Iterator var6 = tasks.iterator();
//
//            while(var6.hasNext()) {
//                AnalyzerTask task = (AnalyzerTask)var6.next();
//                if (task instanceof MemoryAnalyzerTask) {
//                    this.mTasks.add((MemoryAnalyzerTask)task);
//                }
//            }
//
//            this.mOutstandingReport = new AnalysisReport();
//            this.mOutstandingReport.addResultListeners(listeners);
//            List<ListenableFutureTask<List<AnalysisResultEntry<?>>>> futuresList = new ArrayList();
//            Iterator var16 = captureGroup.getCaptures().iterator();
//
//            while(true) {
//                final Snapshot snapshot = null;
//                do {
//                    Capture capture;
//                    do {
//                        if (!var16.hasNext()) {
//                            this.mRunningAnalyzers = Futures.allAsList(futuresList);
//                            Futures.addCallback(this.mRunningAnalyzers, new FutureCallback<List<List<AnalysisResultEntry<?>>>>() {
//                                public void onSuccess(List<List<AnalysisResultEntry<?>>> result) {
//                                    MemoryAnalyzer.this.mAnalysisComplete = true;
//                                    MemoryAnalyzer.this.mOutstandingReport.setCompleted();
//                                }
//
//                                public void onFailure(Throwable t) {
//                                    MemoryAnalyzer.this.mAnalysisComplete = true;
//                                    MemoryAnalyzer.this.mOutstandingReport.setCancelled();
//                                }
//                            }, synchronizingExecutor);
//                            return this.mOutstandingReport;
//                        }
//
//                        capture = (Capture)var16.next();
//                    } while(!accept(capture));
//
//                    snapshot = (Snapshot)capture.getRepresentation(Snapshot.class);
//                } while(snapshot == null);
//
//                List<Heap> heapsToUse = new ArrayList(snapshot.getHeaps().size());
//                Iterator var11 = snapshot.getHeaps().iterator();
//
//                while(var11.hasNext()) {
//                    Heap heap = (Heap)var11.next();
//                    if ("app".equals(heap.getName())) {
//                        heapsToUse.add(heap);
//                        break;
//                    }
//                }
//
//                final MemoryAnalyzerTask.Configuration configuration = new MemoryAnalyzerTask.Configuration(heapsToUse);
//                Iterator var18 = this.mTasks.iterator();
//
//                while(var18.hasNext()) {
//                    final MemoryAnalyzerTask task = (MemoryAnalyzerTask)var18.next();
//                    ListenableFutureTask<List<AnalysisResultEntry<?>>> futureTask = ListenableFutureTask.create(new Callable<List<AnalysisResultEntry<?>>>() {
//                        public List<AnalysisResultEntry<?>> call() throws Exception {
//                            return MemoryAnalyzer.this.mCancelAnalysis ? null : task.analyze(configuration, snapshot);
//                        }
//                    });
//                    Futures.addCallback(futureTask, new FutureCallback<List<AnalysisResultEntry<?>>>() {
//                        public void onSuccess(List<AnalysisResultEntry<?>> result) {
//                            if (!MemoryAnalyzer.this.mCancelAnalysis) {
//                                MemoryAnalyzer.this.mOutstandingReport.addAnalysisResultEntries(result);
//                            }
//                        }
//
//                        public void onFailure(Throwable t) {
//                        }
//                    }, synchronizingExecutor);
//                    taskExecutor.submit(futureTask);
//                    futuresList.add(futureTask);
//                }
//            }
//        }
//    }
    public AnalysisReport analyze(CaptureGroup captureGroup, Set<AnalysisReport.Listener> listeners, Set<? extends AnalyzerTask> tasks, final Executor synchronizingExecutor, ExecutorService taskExecutor) {
        if (this.mOutstandingReport != null) {
            return this.mOutstandingReport;
        } else {
            // 收集所有 MemoryAnalyzerTask
            for (AnalyzerTask task : tasks) {
                if (task instanceof MemoryAnalyzerTask) {
                    this.mTasks.add((MemoryAnalyzerTask) task);
                }
            }

            // 初始化 AnalysisReport
            this.mOutstandingReport = new AnalysisReport();
            this.mOutstandingReport.addResultListeners(listeners);
            List<ListenableFutureTask<List<AnalysisResultEntry<?>>>> futuresList = new ArrayList<>();

            // 处理每个 Capture
            for (Capture capture : captureGroup.getCaptures()) {
                if (!accept(capture)) {
                    continue;
                }

                Snapshot snapshot = capture.getRepresentation(Snapshot.class);
                if (snapshot == null) {
                    continue;
                }

                List<Heap> heapsToUse = new ArrayList<>(snapshot.getHeaps().size());
                for (Heap heap : snapshot.getHeaps()) {
                    if ("app".equals(heap.getName())) {
                        heapsToUse.add(heap);
                        break;
                    }
                }

                final MemoryAnalyzerTask.Configuration configuration = new MemoryAnalyzerTask.Configuration(heapsToUse);
                for (MemoryAnalyzerTask task : this.mTasks) {
                    ListenableFutureTask<List<AnalysisResultEntry<?>>> futureTask = ListenableFutureTask.create(() -> {
                        return MemoryAnalyzer.this.mCancelAnalysis ? null : task.analyze(configuration, snapshot);
                    });

                    Futures.addCallback(futureTask, new FutureCallback<List<AnalysisResultEntry<?>>>() {
                        public void onSuccess(List<AnalysisResultEntry<?>> result) {
                            if (!MemoryAnalyzer.this.mCancelAnalysis) {
                                MemoryAnalyzer.this.mOutstandingReport.addAnalysisResultEntries(result);
                            }
                        }

                        public void onFailure(Throwable t) {
                        }
                    }, synchronizingExecutor);

                    taskExecutor.submit(futureTask);
                    futuresList.add(futureTask);
                }
            }

            // 总结 Future 任务并添加回调
            this.mRunningAnalyzers = Futures.allAsList(futuresList);
            Futures.addCallback(this.mRunningAnalyzers, new FutureCallback<List<List<AnalysisResultEntry<?>>>>() {
                public void onSuccess(List<List<AnalysisResultEntry<?>>> result) {
                    MemoryAnalyzer.this.mAnalysisComplete = true;
                    MemoryAnalyzer.this.mOutstandingReport.setCompleted();
                }

                public void onFailure(Throwable t) {
                    MemoryAnalyzer.this.mAnalysisComplete = true;
                    MemoryAnalyzer.this.mOutstandingReport.setCancelled();
                }
            }, synchronizingExecutor);

            return this.mOutstandingReport;
        }

    }
    public void cancel() {
        if (this.mOutstandingReport != null && !this.mAnalysisComplete) {
            this.mCancelAnalysis = true;
            this.mRunningAnalyzers.cancel(true);
            this.mOutstandingReport.setCancelled();
        }
    }

    public boolean isRunning() {
        return !this.mRunningAnalyzers.isDone();
    }
}