/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package ec.jwsacruncher;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import ec.tss.sa.SaItem;
import ec.tstoolkit.algorithm.CompositeResults;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 *
 * @author Kristof Bayens
 */
public class SaBatchProcessor {

    ISaBatchInformation info_;
    ISaBatchFeedback feedback_;
//    SaProcessing processing_;
    private final String QUERY = "Loading information...", PROCESS = "Processing...", FLUSH = "Flushing bundle...", OPEN = "Opening...", CLOSE = "Closing...", GENERATEOUTPUT = "Generate Output";

    public SaBatchProcessor(ISaBatchInformation info, ISaBatchFeedback fb) {
        info_ = info;
        feedback_ = fb;
    }

    public boolean open() {
//        processing_ = new SaProcessing();
        if (feedback_ != null) {
            feedback_.showAction(OPEN);
        }
        return info_.open();
    }

    public boolean process() {
        if (!open()) {
            return false;
        }
//        if (!loadContext())
//            return false;

        Iterator<ISaBundle> iter = info_.start();
        while (iter.hasNext()) {
            ISaBundle current = iter.next();
            Collection<SaItem> items = current.getItems();
//            processing_.addAll(items);
            compute(items);
            if (feedback_ != null) {
                feedback_.showAction(FLUSH);
            }
            generateOutput();
            current.flush(feedback_);
            //SaManager.instance.remove(items);
        }


        info_.close();
        return true;
    }

    public void generateOutput() {
        if (feedback_ != null) {
            feedback_.showAction(GENERATEOUTPUT);
        }
    }

    private List<Callable<String>> createTasks(Collection<SaItem> items) {
        List<Callable<String>> result = new ArrayList(items.size());
        if (!items.isEmpty()) {
            for (final SaItem o : items) {
                result.add(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        CompositeResults result = o.process();
                        String rslt = (result == null ? " failed" : " processed");
                        if (feedback_ != null) {
                            feedback_.showItem(o.getTs().getName(), rslt);
                        }
                        return rslt;
                    }
                });
            }
        }
        return result;
    }
    private static final int NBR_EXECUTORS = Runtime.getRuntime().availableProcessors();
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder().setDaemon(true).setPriority(Thread.MIN_PRIORITY).build();

    private void compute(Collection<SaItem> items) {

        List<Callable<String>> tasks = createTasks(items);
        if (tasks.isEmpty()) {
            return;
        }
        ExecutorService executorService = Executors.newFixedThreadPool(NBR_EXECUTORS, THREAD_FACTORY);
        try {
            executorService.invokeAll(tasks);
        } catch (InterruptedException ex) {
        } finally {
            executorService.shutdown();
        }
    }
}
