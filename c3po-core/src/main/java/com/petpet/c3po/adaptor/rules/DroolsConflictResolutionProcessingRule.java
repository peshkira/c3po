package com.petpet.c3po.adaptor.rules;

import com.petpet.c3po.adaptor.rules.drools.DroolsResolutionStatisticsPrinter;
import com.petpet.c3po.adaptor.rules.drools.DroolsResolutionWorker;
import com.petpet.c3po.adaptor.rules.drools.DroolsResolutionWorkerFactory;
import com.petpet.c3po.api.adaptor.PostProcessingRule;
import com.petpet.c3po.api.model.Element;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DroolsConflictResolutionProcessingRule implements
        PostProcessingRule {

    public static final int PRIORITY = 500;

    /**
     * Hold a {@link DroolsResolutionWorker} for each thread to allow
     * multithreading without side-effects between threads.
     */
    private Map<Thread, DroolsResolutionWorker> workers;

    private DroolsResolutionWorkerFactory factory;

    public DroolsConflictResolutionProcessingRule() {

        this.factory = new DroolsResolutionWorkerFactory("rules/");
        this.workers = new ConcurrentHashMap<Thread, DroolsResolutionWorker>();
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @Override
    public void onCommandFinished() {
        // TODO: make the execution of these methods configurable


        if (this.factory.isRulesAvailable()){
            // RuleActivationListener.getInstance().printStatistics(System.out, false);
            DroolsResolutionStatisticsPrinter.printRuleActivation(System.out, false);
            DroolsResolutionStatisticsPrinter.printConflictsAccumulated(System.out,
                    false);
            // DroolsResolutionStatisticsPrinter.printConflicts(System.out, false);
        }
    }

    @Override
    public Element process(Element e) {
        if (this.factory.isRulesAvailable())    {
            DroolsResolutionWorker worker = this.workers.get(Thread.currentThread());
            if (worker == null) {
                worker = this.factory.createWorker();
                this.workers.put(Thread.currentThread(), worker);
            }

            worker.process(e);
        }
        return e;
    }
}
