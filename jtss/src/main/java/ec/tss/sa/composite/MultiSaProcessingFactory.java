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
package ec.tss.sa.composite;

import ec.satoolkit.ISaSpecification;
import ec.satoolkit.benchmarking.MultiSaBenchmarkingSpec;
import ec.tss.*;
import ec.tss.sa.SaManager;
import ec.tstoolkit.algorithm.*;
import ec.tstoolkit.algorithm.IProcessing.Status;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.satoolkit.DecompositionMode;
import ec.benchmarking.simplets.TsMultiBenchmarking;
import ec.benchmarking.simplets.TsCholette;
import ec.benchmarking.simplets.TsMultiBenchmarking.ContemporaneousConstraintDescriptor;
import ec.tstoolkit.information.ProxyResults;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.*;

/**
 *
 * @author pcuser
 */
public class MultiSaProcessingFactory implements IProcessingFactory<MultiSaSpecification, TsCollection, CompositeResults> {

    public static final String COMPONENTS = "components", DIFFERENCES = "differences", BENCHMARKING = "benchmarking";
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor("Seasonal adjustment", "Composite", "1.0");
    private static final String TOTAL = "__total__", ANNUALSERIES = "__y__";

    public static final MultiSaProcessingFactory instance = new MultiSaProcessingFactory();

    protected MultiSaProcessingFactory() {
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
        return spec instanceof MultiSaSpecification;
    }

    @Override
    public IProcessing<TsCollection, CompositeResults> generateProcessing(final MultiSaSpecification specification, ProcessingContext context) {
        SequentialProcessing<TsCollection> all = new SequentialProcessing<>();
        all.add(createComponentsNode(specification));
        all.add(createDirectNode(specification));
        all.add(createIndirectNode());
        all.add(createDifferenceNode());
        if (specification.getBenchmarkingSpecification().isEnabled()) {
            all.add(createBenchmarkingStep(specification.getBenchmarkingSpecification()));
        }
        return all;
    }

    public IProcessing<TsCollection, CompositeResults> generateProcessing(final MultiSaSpecification specification) {
        return generateProcessing(specification);
    }

    private static IProcessingNode<TsCollection> createComponentsNode(final MultiSaSpecification specs) {
        return new IProcessingNode<TsCollection>() {
            @Override
            public String getName() {
                return COMPONENTS;
            }

            @Override
            public String getPrefix() {
                return null;
            }

            @Override
            public IProcessing.Status process(final TsCollection input, Map<String, IProcResults> results) {

                if (input == null || input.getCount() < 2) {
                    return IProcessing.Status.Invalid;
                }

                ParallelProcessingNode<TsCollection> cmps = new ParallelProcessingNode<>(COMPONENTS, null);
                int n = input.getCount();
                for (int i = 0; i < n; ++i) {
                    cmps.add(createNode(specs.getSpecification(i), i));
                }
                return cmps.process(input, results);
            }
        };
    }

    private static IProcessingNode<TsCollection> createNode(final ISaSpecification spec, final int pos) {
        return new IProcessingNode<TsCollection>() {
            @Override
            public String getName() {
                return MultiSaSpecification.COMPONENT + pos;
            }

            @Override
            public String getPrefix() {
                return getName();
            }

            @Override
            public IProcessing.Status process(TsCollection input, Map<String, IProcResults> results) {
                Ts ts = input.get(pos);
                CompositeResults process = SaManager.instance.process(spec, ts.getTsData());
                results.put(getName(), process);
                return process == null ? IProcessing.Status.Invalid : IProcessing.Status.Valid;
            }
        };
    }

    private static TsData sum(TsCollection input) {

        TsData sum = null;
        for (Ts s : input) {
            if (s.hasData() == TsStatus.Undefined) {
                s.load(TsInformationType.Data);
            }
            sum = TsData.add(sum, s.getTsData());
        }
        return sum;
    }

    private IProcessingNode<TsCollection> createDirectNode(final MultiSaSpecification specification) {
        return new IProcessingNode<TsCollection>() {
            @Override
            public String getName() {
                return MultiSaSpecification.DIRECT;
            }

            @Override
            public String getPrefix() {
                return MultiSaSpecification.DIRECT;
            }

            @Override
            public IProcessing.Status process(final TsCollection input, Map<String, IProcResults> results) {
                IProcResults rslts = SaManager.instance.process(specification.getTotalSpecification(), sum(input));
                results.put(getName(), rslts);
                if (rslts != null) {
                    return IProcessing.Status.Valid;
                } else {
                    return IProcessing.Status.Invalid;
                }
            }
        };
    }

    private IProcessingNode<TsCollection> createIndirectNode() {
        return new IProcessingNode<TsCollection>() {
            @Override
            public String getName() {
                return MultiSaSpecification.INDIRECT;
            }

            @Override
            public String getPrefix() {
                return MultiSaSpecification.INDIRECT;
            }

            @Override
            public IProcessing.Status process(final TsCollection input, Map<String, IProcResults> results) {
                TsData sum = null;
                IProcResults all = results.get(COMPONENTS);
                int n = input.getCount();
                for (int i = 0; i < n; ++i) {
                    TsData s = all.getData(InformationSet.item(MultiSaSpecification.COMPONENT + i, "sa"), TsData.class);
                    sum = TsData.add(sum, s);
                }
                SingleTsData sa = new SingleTsData("sa", sum);
                results.put(MultiSaSpecification.INDIRECT, sa);
                return IProcessing.Status.Valid;
            }
        };
    }

    private IProcessingNode<TsCollection> createDifferenceNode() {
        return new IProcessingNode<TsCollection>() {
            @Override
            public String getName() {
                return DIFFERENCES;
            }

            @Override
            public String getPrefix() {
                return null;
            }

            @Override
            public IProcessing.Status process(final TsCollection input, Map<String, IProcResults> results) {
                TsData d = null, i = null;
                IProcResults dr = results.get(MultiSaSpecification.DIRECT);
                boolean mul = false;
                if (dr != null) {
                    d = dr.getData("sa", TsData.class);
                    DecompositionMode mode = dr.getData(ModellingDictionary.MODE, DecompositionMode.class);
                    if (mode != null) {
                        mul = mode.isMultiplicative();
                    }
                }
                IProcResults ir = results.get(MultiSaSpecification.INDIRECT);
                if (ir != null) {
                    i = ir.getData("sa", TsData.class);
                }
                SingleTsData del = new SingleTsData(DIFFERENCES, mul ? TsData.divide(d, i).minus(1) : TsData.subtract(d, i));
                results.put(DIFFERENCES, del);
                return IProcessing.Status.Valid;
            }
        };
    }

    private IProcessingNode<TsCollection> createBenchmarkingStep(final MultiSaBenchmarkingSpec bspec) {
        return new IProcessingNode<TsCollection>() {
            @Override
            public String getName() {
                return BENCHMARKING;
            }

            @Override
            public String getPrefix() {
                return BENCHMARKING;
            }

            @Override
            public Status process(final TsCollection input, Map<String, IProcResults> results) {
                TsData dsa = getDirectSa(results);
                if (dsa == null) {
                    return Status.Invalid;
                }

                TsMultiBenchmarking bench = new TsMultiBenchmarking();
                bench.setRho(bspec.getRho());
                bench.setLambda(bspec.getLambda());
                int n = input.getCount();
                List<String> sa = getSa(bench, results, n, dsa.getDomain());
                if (sa == null) {
                    return Status.Invalid;
                }
                if (!bspec.isAnnualConstraint()) {
                    bench.addInput(TOTAL, dsa);
                } else {
                    // we have to benchmark the direct sa. We do it with the same parameters
                    TsData y = sum(input).changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true);
                    bench.addInput(TOTAL, benchDsa(y, dsa, bspec));
                }

                fillContemporaneousConstraints(bench, sa, bspec.getContemporaneousConstraintType());
                if (bspec.isAnnualConstraint()) {
                    fillAnnualConstraints(bench, input, sa);
                }
                if (!bench.process()) {
                    return Status.Invalid;
                }
                boolean bsa = bspec.isAnnualConstraint() || bspec.getContemporaneousConstraintType() == MultiSaBenchmarkingSpec.ConstraintType.Free;
                fillResults(bench, sa, results, bsa);
                return Status.Valid;
            }

            List<String> getSa(TsMultiBenchmarking bench, Map<String, IProcResults> results, int n, TsDomain domain) {
                IProcResults cmps = results.get(COMPONENTS);
                if (cmps == null) {
                    return null;
                }
                ArrayList<String> data = new ArrayList<>(n);
                for (int i = 0; i < n; ++i) {
                    String id = MultiSaSpecification.COMPONENT + i;
                    TsData s = cmps.getData(InformationSet.item(id, "sa"), TsData.class);
                    if (s != null) {
                        bench.addInput(id, s.fittoDomain(domain));
                        data.add(id);
                    }
                }
                return data;
            }

            void fillAnnualConstraints(TsMultiBenchmarking bench, TsCollection input, List<String> sa) {
                for (int i = 0; i < input.getCount(); ++i) {
                    String id = ANNUALSERIES + i;
                    bench.addInput(id, input.get(i).getTsData().changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true));
                    bench.addTemporalConstraint(id, sa.get(i));
                }
            }

            void fillContemporaneousConstraints(TsMultiBenchmarking bench, List<String> sa, MultiSaBenchmarkingSpec.ConstraintType type) {

                ContemporaneousConstraintDescriptor desc;
                switch (type) {
                    case Fixed:
                        desc = new ContemporaneousConstraintDescriptor(TOTAL);
                        for (String s : sa) {
                            desc.add(s, 1.0);
                        }
                        break;
                    case Free:
                        desc = new ContemporaneousConstraintDescriptor(0);
                        desc.add(TOTAL, -1.0);
                        for (String s : sa) {
                            desc.add(s, 1.0);
                        }
                        break;
                    default:
                        return;
                }
                bench.addContemporaneousConstraint(desc);
            }

            TsData getDirectSa(Map<String, IProcResults> results) {
                IProcResults dr = results.get(MultiSaSpecification.DIRECT);
                if (dr != null) {
                    return dr.getData("sa", TsData.class);
                } else {
                    return null;
                }
            }

            private void fillResults(TsMultiBenchmarking bench, List<String> sa, Map<String, IProcResults> results, boolean b) {
                InformationSet info = new InformationSet();
                TsData bsa = null;
                for (String s : sa) {
                    TsData data = bench.getResult(s);
                    if (b) {
                        bsa = TsData.add(bsa, data);
                    }
                    info.set(s, data);
                }
                if (b) {
                    info.set("sa", bsa);
                }

                results.put(BENCHMARKING, new ProxyResults(info, null));
            }

            private TsData benchDsa(TsData sum, TsData dsa, MultiSaBenchmarkingSpec bspec) {
                //
                TsCholette cholette = new TsCholette();
                cholette.setRho(bspec.getRho());
                cholette.setLambda(bspec.getLambda());
                return cholette.process(dsa, sum);
            }
        };
    }

    @Override
    public Map<String, Class> getSpecificationDictionary(Class<MultiSaSpecification> specClass) {
        HashMap<String, Class> dic = new HashMap<>();
        MultiSaSpecification.fillDictionary(null, dic);
        return dic;
    }

    @Override
    public Map<String, Class> getOutputDictionary() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
