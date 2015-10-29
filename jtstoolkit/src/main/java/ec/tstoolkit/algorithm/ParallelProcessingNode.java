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
package ec.tstoolkit.algorithm;

import ec.tstoolkit.algorithm.IProcessing.Status;
import ec.tstoolkit.information.InformationSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Jean Palate
 */
public class ParallelProcessingNode<I> implements IProcessingNode<I> {

    private final int NBR_EXECUTORS = Runtime.getRuntime().availableProcessors();
    private final String name, prefix;

    public ParallelProcessingNode(final String name, final String prefix) {
        this.name = name;
        this.prefix = prefix;
    }
    private ArrayList<IProcessingNode<I>> nodes_ = new ArrayList<>();

    public void add(IProcessingNode<I> node) {
        nodes_.add(node);
    }

    private List<Callable<IProcessing.Status>> createTasks(final I input, final Map<String, IProcResults> map, final Map<String, String> errors) {
        ArrayList<Callable<IProcessing.Status>> result = new ArrayList<>();
        for (final IProcessingNode<I> o : nodes_) {
            result.add(new Callable<IProcessing.Status>() {
                @Override
                public IProcessing.Status call() throws Exception {
                    try {
                        return o.process(input, map);
                    } catch (Exception err) {
                        errors.put(o.getName(), err.getMessage());
                        return IProcessing.Status.Invalid;
                    }
                }
            });
        }
        result.trimToSize();
        return result;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public Status process(I input, Map<String, IProcResults> results) {
        CompositeResults cresults = new CompositeResults();
        final ConcurrentHashMap<String, IProcResults> map = new ConcurrentHashMap<>();
        ExecutorService executorService = Executors.newFixedThreadPool(NBR_EXECUTORS, CustomThreadFactory.INSTANCE);

        Map<String, String> errors=new ConcurrentHashMap<>();
        List<Callable<IProcessing.Status>> tasks = createTasks(input, map, errors);
        try {
            executorService.invokeAll(tasks);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return Status.Invalid;
        }

        executorService.shutdown();
        for (IProcessingNode<I> cur : nodes_) {

            String cname = cur.getName();
            IProcResults cresult = map.get(cname);
            cresults.put(cname, cresult, cur.getPrefix());
        }
        if (! errors.isEmpty()){
            for (Entry<String, String> n : errors.entrySet()){
                cresults.addInformation(ProcessingInformation.error(n.getKey(), n.getValue()));
            }
        }
        results.put(name, cresults);
        return Status.Valid;
    }

    private enum CustomThreadFactory implements ThreadFactory {

        INSTANCE;
        //
        final ThreadGroup group;
        final AtomicInteger threadNumber = new AtomicInteger(1);
        final String namePrefix;

        private CustomThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "pool-" + CustomThreadFactory.class.getSimpleName() + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            t.setDaemon(true);
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        }
    }
}
