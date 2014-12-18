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
package ec.tss.businesscycle.processors;

import ec.businesscycle.impl.HodrickPrescott;
import ec.satoolkit.ISaSpecification;
import ec.satoolkit.ISeriesDecomposition;
import ec.satoolkit.SaSpecification;
import ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory;
import ec.satoolkit.algorithm.implementation.X13ProcessingFactory;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.satoolkit.x13.X13Specification;
import ec.tss.businesscycle.documents.BusinessCycleDecomposition;
import ec.tss.businesscycle.documents.HodrickPrescottSpecification;
import ec.tstoolkit.BaseException;
import ec.tstoolkit.algorithm.*;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.ComponentInformation;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
public class HodrickPrescottProcessingFactory implements IProcessingFactory<HodrickPrescottSpecification, TsData, CompositeResults> {

    public static final String FAMILY = "Business Cycle", METHOD = "hodrickprescott";
    public static final String VERSION = "0.1.0.0";
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, METHOD, VERSION);
    public static final String INPUT = "input", SA = "sa", BC = "bc";
    public final static int MAX_REPEAT_COUNT = 50;

    public static final HodrickPrescottProcessingFactory instance = new HodrickPrescottProcessingFactory();

    @Override
    public void dispose() {
    }

    @Override
    public AlgorithmDescriptor getInformation() {
        return DESCRIPTOR;
    }

    @Override
    public boolean canHandle(IProcSpecification spec) {
        return spec instanceof HodrickPrescottSpecification;
    }

    @Override
    public IProcessing<TsData, CompositeResults> generateProcessing(HodrickPrescottSpecification specification, ProcessingContext context) {
        return create(specification);
    }

    public IProcessing<TsData, CompositeResults> generateProcessing(HodrickPrescottSpecification specification) {
        return create(specification);
    }

    protected static TsData series(Map<String, IProcResults> results) {
        IProcResults input = results.get(INPUT);
        if (input == null || !(input instanceof SingleTsData)) {
            return null;
        }
        SingleTsData sdata = (SingleTsData) input;
        return sdata.getSeries();
    }

    protected static ISeriesDecomposition getSa(Map<String, IProcResults> results) {
        IProcResults decomp = results.get(SA);
        if (decomp == null || !(decomp instanceof ISeriesDecomposition)) {
            return null;
        }
        return (ISeriesDecomposition) decomp;
    }

    private static SequentialProcessing<TsData> create(HodrickPrescottSpecification xspec) {
        SequentialProcessing processing = new SequentialProcessing();
        processing.add(createInitialStep(new TsPeriodSelector()));
        processing.add(createSaStep(xspec.getSaSpecification()));
        processing.add(createHpStep(xspec));
        return processing;
    }

    public static CompositeResults process(TsData s, HodrickPrescottSpecification xspec) {
        SequentialProcessing<TsData> processing = create(xspec);
        return processing.process(s);
    }

    protected static IProcessingNode<TsData> createInitialStep(final TsPeriodSelector selector) {
        return new IProcessingNode<TsData>() {

            @Override
            public String getName() {
                return INPUT;
            }

            @Override
            public String getPrefix() {
                return null;
            }

            @Override
            public IProcessing.Status process(TsData input, Map<String, IProcResults> results) {
                SingleTsDataProcessing processing = new SingleTsDataProcessing(selector);
                processing.setValidation(new SingleTsDataProcessing.Validation() {

                    @Override
                    public boolean validate(TsData s) {
                        testSeries(s);
                        return true;
                    }
                });
                SingleTsData rslt = processing.process(input);
                if (rslt != null) {
                    results.put(INPUT, rslt);
                    return IProcessing.Status.Valid;
                } else {
                    return IProcessing.Status.Invalid;
                }
            }
        };
    }

    protected static IProcessingNode<TsData> createSaStep(final SaSpecification spec) {
        return new IProcessingNode<TsData>() {

            @Override
            public String getName() {
                return SA;
            }

            @Override
            public String getPrefix() {
                return SA;
            }

            @Override
            public IProcessing.Status process(TsData ts, Map<String, IProcResults> results) {
                ISaSpecification xspec = spec.getFullSpecification();
                if (xspec == null) {
                    return IProcessing.Status.Valid;
                }
                TsData s = series(results);
                if (s == null) {
                    s = ts;
                }
                if (s == null) {
                    return IProcessing.Status.Invalid;
                }
                try {
                    ISeriesDecomposition sa = null;
                    if (xspec instanceof TramoSeatsSpecification) {
                        sa = TramoSeatsProcessingFactory.process(s, (TramoSeatsSpecification) xspec).get(TramoSeatsProcessingFactory.FINAL, ISeriesDecomposition.class);
                    } else if (xspec instanceof X13Specification) {
                        sa = X13ProcessingFactory.process(s, (X13Specification) xspec).get(X13ProcessingFactory.FINAL, ISeriesDecomposition.class);
                    }
                    if (sa == null) {
                        return IProcessing.Status.Invalid;
                    } else {
                        results.put(SA, sa);
                        return IProcessing.Status.Valid;
                    }
                } catch (Exception err) {
                    return IProcessing.Status.Invalid;
                }
            }
        };
    }

    protected static IProcessingNode<TsData> createHpStep(final HodrickPrescottSpecification spec) {
        return new IProcessingNode<TsData>() {

            @Override
            public String getName() {
                return BC;
            }

            @Override
            public String getPrefix() {
                return BC;
            }

            @Override
            public IProcessing.Status process(TsData ts, Map<String, IProcResults> results) {
                boolean mul = false;
                TsData s = null;
                if (spec.getTarget() == HodrickPrescottSpecification.Target.Original) {
                    s = series(results);
                    if (s == null) {
                        s = ts;
                    }
                } else {
                    ISeriesDecomposition sa = getSa(results);
                    if (sa == null) {
                        return IProcessing.Status.Invalid;
                    }
                    mul = sa.getMode().isMultiplicative();
                    switch (spec.getTarget()) {
                        case Trend:
                            s = sa.getSeries(ComponentType.Trend, ComponentInformation.Value);
                            break;
                        case Sa:
                            s = sa.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value);
                            break;
                        case Original:
                            s = sa.getSeries(ComponentType.Series, ComponentInformation.Value);
                            break;
                    }
                    if (s == null) {
                        return IProcessing.Status.Invalid;
                    }
                }
                try {
                    TsData input = s;
                    if (mul) {
                        input = input.log();
                    }
                    HodrickPrescott hp = new HodrickPrescott();
                    double lambda = spec.getLambda();
                    if (spec.getCycleLength() != 0) {
                        // compute lambda...
                        lambda = defaultLambda(spec.getCycleLength(), s.getFrequency().intValue());
                    }
                    hp.setLambda(lambda);
                    if (!hp.process(input.getValues().internalStorage())) {
                        return IProcessing.Status.Invalid;
                    } else {
                        TsData t = new TsData(s.getStart(), hp.getSignal(), false);
                        if (mul) {
                            t = t.exp();
                        }
                        TsData c = mul ? TsData.divide(s, t) : TsData.subtract(s, t);
                        BusinessCycleDecomposition bc = new BusinessCycleDecomposition(mul, s, t, c);
                        results.put(BC, bc);
                        return IProcessing.Status.Valid;
                    }
                } catch (Exception err) {
                    return IProcessing.Status.Invalid;
                }
            }
        ;
    }

    ;
   }
    
   public static double defaultLambda(double ylen, int freq) {
        double w = 2 * Math.PI / (freq * ylen);
        double x = 1 - Math.cos(w);
        return .75 / (x * x);
    }

    private static void testSeries(final TsData y) {
        if (y == null) {
            throw new BaseException("Missing series");
        }
        int nz = y.getLength();
        int ifreq = y.getFrequency().intValue();
        if (nz < Math.max(8, 3 * ifreq)) {
            throw new BaseException("Not enough data");
        }
        int nrepeat = y.getValues().getRepeatCount();
        if (nrepeat > MAX_REPEAT_COUNT * nz / 100) {
            throw new BaseException("Too many identical values");
        }
    }

    @Override
    public Map<String, Class> getSpecificationDictionary(Class<HodrickPrescottSpecification> specClass) {
        HashMap<String, Class> dic = new HashMap<>();
        HodrickPrescottSpecification.fillDictionary(null, dic);
        return dic;
    }

    @Override
    public Map<String, Class> getOutputDictionary() {
        LinkedHashMap<String, Class> dic = new LinkedHashMap<>();
        BusinessCycleDecomposition.fillDictionary(BC, dic);
        return dic;
    }
}
