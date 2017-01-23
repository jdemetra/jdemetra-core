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
package ec.tss.disaggregation.processors;

import ec.benchmarking.DisaggregationModel;
import ec.benchmarking.simplets.TsDisaggregation;
import ec.tss.disaggregation.documents.DisaggregationResults;
import ec.tss.disaggregation.documents.DisaggregationSpecification;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.algorithm.AlgorithmDescriptor;
import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.algorithm.IProcessing;
import ec.tstoolkit.algorithm.IProcessingFactory;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.polynomials.UnitRoots;
import ec.tstoolkit.ssf.ISsf;
import ec.tstoolkit.ssf.arima.SsfAr1;
import ec.tstoolkit.ssf.arima.SsfArima;
import ec.tstoolkit.ssf.arima.SsfRw;
import ec.tstoolkit.ssf.arima.SsfRwAr1;
import ec.tstoolkit.timeseries.regression.Constant;
import ec.tstoolkit.timeseries.regression.LinearTrend;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.Map;

/**
 *
 * @author Jean
 */
public class DisaggregationProcessor implements ITemporalDisaggregationProcessingFactory, IProcessingFactory<DisaggregationSpecification, TsData[], DisaggregationResults> {

    public static final String NAME="Regression-based disaggregation";
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, NAME, null);
    public static final DisaggregationProcessor instance=new DisaggregationProcessor();
    
    protected DisaggregationProcessor(){}
    
    @Override
    public void dispose() {
    }

    @Override
    public AlgorithmDescriptor getInformation() {
        return DESCRIPTOR;
    }

    @Override
    public boolean canHandle(IProcSpecification spec) {
        return spec instanceof DisaggregationSpecification;
    }

    @Override
    public IProcessing<TsData[], DisaggregationResults> generateProcessing(final DisaggregationSpecification spec, ProcessingContext context) {
        return new DefaultProcessing(spec);
    }

   public IProcessing<TsData[], DisaggregationResults> generateProcessing(final DisaggregationSpecification spec) {
        return new DefaultProcessing(spec);
    }

   @Override
    public Map<String, Class> getSpecificationDictionary(Class<DisaggregationSpecification> specClass) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<String, Class> getOutputDictionary(boolean compact) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static class DefaultProcessing implements IProcessing<TsData[], DisaggregationResults> {

        private final DisaggregationSpecification spec;

        public DefaultProcessing(DisaggregationSpecification spec) {
            this.spec = spec.clone();
        }

        @Override
        public DisaggregationResults process(TsData[] input) {
            DisaggregationModel model = prepare(input, spec.getDefaultFrequency());
            if (model == null) {
                return null;
            }
            TsDisaggregation<? extends ISsf> disagg;

            if (spec.getModel() == DisaggregationSpecification.Model.Ar1) {
                disagg = initChowLin();
            } else if (spec.getModel() == DisaggregationSpecification.Model.Wn) {
                disagg = initOLS();
            } else if (spec.getModel() == DisaggregationSpecification.Model.RwAr1) {
                disagg = initLitterman();
            } else if (spec.getModel() == DisaggregationSpecification.Model.Rw) {
                disagg = initFernandez();
            }else{
                disagg = initI(spec.getModel().getDifferencingOrder());
            }
                
            disagg.useML(spec.isML());
            disagg.calculateVariance(true);
            disagg.setSsfOption(spec.getOption());
            if (spec.isDiffuseRegression()) {
                disagg.setDiffuseRegressorsCount(model.getX().getVariablesCount());
            }


            if (!disagg.process(model, null)) {
                return null;
            } else {
                int n = model.getX().getVariablesCount();
                if (spec.isConstant()) {
                    --n;
                }
                if (spec.isTrend()) {
                    --n;
                }
                return new DisaggregationResults(disagg, n);
            }
        }

        private TsDisaggregation<SsfAr1> initChowLin() {
            TsDisaggregation<SsfAr1> disagg = new TsDisaggregation<>();
            SsfAr1 ssf = new SsfAr1();
            Parameter p = spec.getParameter();
            if (p != null && p.isFixed()) {
                ssf.setRho(p.getValue());
            } else {
                disagg.setMapping(new SsfAr1.Mapping(spec.isZeroInitialization(), spec.getTruncatedRho(), 1));
            }
            ssf.useZeroInitialization(spec.isZeroInitialization());
            disagg.setSsf(ssf);
            return disagg;
        }

        private TsDisaggregation<SsfAr1> initOLS() {
            TsDisaggregation<SsfAr1> disagg = new TsDisaggregation<>();
            SsfAr1 ssf = new SsfAr1();
            ssf.setRho(0);
            disagg.setSsf(ssf);
            return disagg;
        }

        private TsDisaggregation<SsfRwAr1> initLitterman() {
            TsDisaggregation<SsfRwAr1> disagg = new TsDisaggregation<>();
            SsfRwAr1 ssf = new SsfRwAr1();
            Parameter p = spec.getParameter();
            if (p != null && p.isFixed()) {
                ssf.setRho(p.getValue());
            } else {
                disagg.setMapping(new SsfRwAr1.Mapping(spec.isZeroInitialization(), spec.getTruncatedRho(), 1));
            }
            ssf.useZeroInitialization(spec.isZeroInitialization());
            disagg.setSsf(ssf);
            return disagg;
        }

        private TsDisaggregation<SsfRw> initFernandez() {
            TsDisaggregation<SsfRw> disagg = new TsDisaggregation<>();
            SsfRw ssf = new SsfRw();
            ssf.useZeroInitialization(spec.isZeroInitialization());
            disagg.setSsf(ssf);
            return disagg;
        }

        private TsDisaggregation<SsfArima> initI(int diff) {
            TsDisaggregation<SsfArima> disagg = new TsDisaggregation<>();
            ArimaModel sarima = new ArimaModel(null, new BackFilter(UnitRoots.D(1, diff)), null, 1);
            SsfArima ssf = new SsfArima(sarima);
            disagg.setSsf(ssf);
            return disagg;
        }

        private DisaggregationModel prepare(TsData[] input, TsFrequency def) {
            if (input == null || input.length == 0) {
                return null;
            }
            TsData y = input[0].select(spec.getSpan());
            DisaggregationModel model = new DisaggregationModel(def);
            model.setY(y);
            if (input.length == 1) {
                if (def == TsFrequency.Undefined || !y.getFrequency().contains(def)) {
                    return null;
                } else {
                    model.setDefaultForecastCount(def.intValue());
                }
            }
            TsVariableList x = new TsVariableList();
            if (spec.isConstant() && (spec.getModel().isStationary() || spec.isZeroInitialization())) {
                x.add(new Constant());
            }
            if (spec.isTrend()) {
                x.add(new LinearTrend(input[0].getStart().firstday()));
            }
            for (int i = 1; i < input.length; ++i) {
                x.add(new TsVariable("var-" + i, input[i]));
            }
            if (!x.isEmpty()) {
                model.setX(x);
            }
            model.setAggregationType(spec.getType());
            return model;
        }
    }
}
