package com.tcl.tools.profilers.memory.perflib.analyzer;



import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class AnalysisReport {
    private Set<AnalysisReport.Listener> mListeners = new HashSet();
    private List<AnalysisResultEntry<?>> mAnalysisResults = new ArrayList();
    private volatile boolean mCompleted = false;
    private volatile boolean mCancelled = false;

    public AnalysisReport() {
    }

    public void addAnalysisResultEntries(List<AnalysisResultEntry<?>> entries) {
        this.mAnalysisResults.addAll(entries);
        Iterator var2 = this.mListeners.iterator();

        while(var2.hasNext()) {
           AnalysisReport.Listener listener = (AnalysisReport.Listener)var2.next();
            listener.onResultsAdded(entries);
        }

    }

    public void setCompleted() {
        if (!this.mCompleted && !this.mCancelled) {
            this.mCompleted = true;
            Iterator var1 = this.mListeners.iterator();

            while(var1.hasNext()) {
               AnalysisReport.Listener listener = (AnalysisReport.Listener)var1.next();
                listener.onAnalysisComplete();
            }

        }
    }

    public void setCancelled() {
        if (!this.mCompleted && !this.mCancelled) {
            this.mCancelled = true;
            Iterator var1 = this.mListeners.iterator();

            while(var1.hasNext()) {
               AnalysisReport.Listener listener = (AnalysisReport.Listener)var1.next();
                listener.onAnalysisCancelled();
            }

        }
    }

    public void addResultListeners(Set<AnalysisReport.Listener> listeners) {
        this.mListeners.addAll(listeners);
    }

    public void removeResultListener(Set<AnalysisReport.Listener> listener) {
        this.mListeners.removeAll(listener);
    }

    public interface Listener {
        void onResultsAdded(List<AnalysisResultEntry<?>> entries);

        void onAnalysisComplete();

        void onAnalysisCancelled();
    }
}
