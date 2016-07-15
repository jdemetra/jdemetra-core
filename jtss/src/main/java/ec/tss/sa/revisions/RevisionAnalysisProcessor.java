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
package ec.tss.sa.revisions;

import ec.satoolkit.DecompositionMode;
import ec.satoolkit.GenericSaProcessingFactory;
import ec.tss.Ts;
import ec.tss.TsCollection;
import ec.tstoolkit.algorithm.AlgorithmDescriptor;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.algorithm.IProcessing;
import ec.tstoolkit.algorithm.IProcessingFactory;
import ec.tstoolkit.algorithm.IProcessingNode;
import ec.tstoolkit.algorithm.ParallelProcessingNode;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.algorithm.SequentialProcessing;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.ProxyResults;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author maggima
 */
public class RevisionAnalysisProcessor implements IProcessingFactory<RevisionAnalysisSpec, TsCollection, CompositeResults> {

    public static final String BATCH = "batch", SERIES = "series", SUMMARY = "summary";

    public static final String FAMILY = GenericSaProcessingFactory.FAMILY;
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, "Revision Analysis", null);

    private final Map<String, Class> dictionary = new HashMap<>();

    public static final RevisionAnalysisProcessor instance = new RevisionAnalysisProcessor();

    public RevisionAnalysisProcessor() {
        setDefaultDictionary();
    }

    public void setDictionary(Map<String, Class> dic) {
        synchronized (dictionary) {
            dictionary.clear();
            dictionary.putAll(dic);
        }
    }

    @Override
    public void dispose() {
    }

    @Override
    public AlgorithmDescriptor getInformation() {
        return DESCRIPTOR;
    }

    @Override
    public boolean canHandle(IProcSpecification spec) {
        return spec instanceof RevisionAnalysisSpec;
    }

    @Override
    public IProcessing<TsCollection, CompositeResults> generateProcessing(final RevisionAnalysisSpec spec, ProcessingContext context) {
        synchronized (dictionary) {
            final Map<String, Class> localdictionary = new HashMap<>();
            localdictionary.putAll(dictionary);
            SequentialProcessing<TsCollection> all = new SequentialProcessing<>();
            IProcessingNode<TsCollection> saStep = new IProcessingNode<TsCollection>() {

                @Override
                public String getName() {
                    return BATCH;
                }

                @Override
                public String getPrefix() {
                    return getName();
                }

                @Override
                public IProcessing.Status process(final TsCollection input, Map<String, IProcResults> results) {

                    if (input == null || input.isEmpty()) {
                        return IProcessing.Status.Invalid;
                    }

                    ParallelProcessingNode<TsCollection> cmps = new ParallelProcessingNode<>(BATCH, null);
                    for (int i = 0; i < input.getCount(); ++i) {
                        cmps.add(createNode(spec, i));
                    }
                    return cmps.process(input, results);
                }

                private IProcessingNode<TsCollection> createNode(final RevisionAnalysisSpec spec, final int pos) {
                    return new IProcessingNode<TsCollection>() {
                        @Override
                        public String getName() {
                            return SERIES + pos;
                        }

                        @Override
                        public String getPrefix() {
                            return getName();
                        }

                        @Override
                        public IProcessing.Status process(TsCollection input, Map<String, IProcResults> results) {
                            Ts ts = input.get(pos);
                            SingleRevisionAnalysisProcessor proc = new SingleRevisionAnalysisProcessor(spec, ts.getTsData());
                            if (!proc.process()) {
                                return IProcessing.Status.Invalid;
                            }
                            InformationSet rinfo = proc.search(localdictionary);

                            results.put(getName(), new ProxyResults(rinfo, null));
                            return IProcessing.Status.Valid;
                        }
                    };
                }
            };
            all.add(saStep);
            IProcessingNode<TsCollection> statsStep = new IProcessingNode<TsCollection>() {

                @Override
                public String getName() {
                    return SUMMARY; //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public String getPrefix() {
                    return SUMMARY; //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public IProcessing.Status process(TsCollection input, Map<String, IProcResults> results) {
                    IProcResults rslts = results.get(BATCH);
                    if (rslts != null) {
                        
                        results.put(SUMMARY, new RevisionStatistics(input, rslts, spec.isOutOfSample(), spec.isTargetFinal()));
                        return IProcessing.Status.Valid;
                    } else {
                        return IProcessing.Status.Unprocessed;
                    }
                }
            };
            all.add(statsStep);
            return all;
        }
    }

    @Override
    public Map<String, Class> getSpecificationDictionary(Class<RevisionAnalysisSpec> specClass) {
        Map<String, Class> dic = new HashMap<>();
        RevisionAnalysisSpec.fillDictionary(null, dic);
        return dic;
    }

    @Override
    public Map<String, Class> getOutputDictionary() {
        Map<String, Class> dic = new HashMap<>();
        return dic;
    }

    private void setDefaultDictionary() {
        dictionary.clear();
        dictionary.put("sa", TsData.class);
        dictionary.put("s", TsData.class);
        //dictionary.put("t", TsData.class);
        //dictionary.put("i", TsData.class);
        dictionary.put("cal", TsData.class);
        dictionary.put("s_lin", TsData.class);
        dictionary.put("sa_lin", TsData.class);
        dictionary.put("mode", DecompositionMode.class);
        dictionary.put("residuals.ser", Double.class);
    }
}
