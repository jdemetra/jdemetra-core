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
package ec.tss.sa.processors;

import ec.satoolkit.GenericSaResults;
import ec.satoolkit.ISaSpecification;
import ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tss.sa.EstimationPolicyType;
import ec.tss.sa.ISaProcessingFactory;
import ec.tss.sa.SaItem;
import ec.tss.sa.documents.SaDocument;
import ec.tss.sa.documents.TramoSeatsDocument;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.algorithm.AlgorithmDescriptor;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.algorithm.IProcessing;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.ArimaSpec;
import ec.tstoolkit.modelling.arima.tramo.CalendarSpec;
import ec.tstoolkit.modelling.arima.tramo.EasterSpec;
import ec.tstoolkit.modelling.arima.tramo.OutlierSpec;
import ec.tstoolkit.modelling.arima.tramo.RegressionSpec;
import ec.tstoolkit.modelling.arima.tramo.TradingDaysSpec;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.modelling.arima.tramo.TransformSpec;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.IEasterVariable;
import ec.tstoolkit.timeseries.regression.ILengthOfPeriodVariable;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import ec.tstoolkit.timeseries.regression.ITradingDaysVariable;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.OutlierDefinition;
import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.regression.TsVariableSelection;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = ISaProcessingFactory.class)
public class TramoSeatsProcessor implements ISaProcessingFactory<TramoSeatsSpecification> {

    public static final AlgorithmDescriptor DESCRIPTOR = TramoSeatsProcessingFactory.DESCRIPTOR;

    public TramoSeatsProcessor() {
    }

    @Override
    public ISaSpecification createSpecification(InformationSet info) {
        TramoSeatsSpecification spec = new TramoSeatsSpecification();
        if (spec.read(info)) {
            return spec;
        } else {
            return null;
        }
    }

    @Override
    public ISaSpecification createSpecification(SaItem doc, TsDomain frozen, EstimationPolicyType policy, boolean nospan) {
        TramoSeatsSpecification spec = (TramoSeatsSpecification) doc.getPointSpecification();
        TramoSeatsSpecification defspec = (TramoSeatsSpecification) doc.getDomainSpecification();
        if (policy == EstimationPolicyType.Complete || spec == null) {
            TramoSeatsSpecification nspec = defspec.clone();
            if (nospan) {
                nspec.getTramoSpecification().getTransform().getSpan().all();
            }
            return nspec;
        }

        TramoSeatsSpecification newspec = spec.clone();

        TramoSpecification ntspec = newspec.getTramoSpecification(),
                dtspec = defspec.getTramoSpecification();
        if (nospan) {
            ntspec.getTransform().getSpan().all();
        }

        // automodel/arima
        refreshArimaSpec(ntspec, dtspec, policy);
        refreshOutliersSpec(ntspec, dtspec, frozen, policy);

        RegressionSpec nrspec = ntspec.getRegression();
        if (policy == EstimationPolicyType.Fixed) {
            // fix all the coefficients of the regression variables
            Map<String, double[]> all = nrspec.getAllCoefficients();
            all.forEach((n, c) -> nrspec.setFixedCoefficients(n, c));
        } else {
            // copy back the initial fixed coefficients
            nrspec.clearAllFixedCoefficients();
            Map<String, double[]> all = dtspec.getRegression().getAllFixedCoefficients();
            all.forEach((n, c) -> nrspec.setFixedCoefficients(n, c));
        }
        nrspec.clearAllCoefficients();
       return newspec;
    }

    private void refreshArimaSpec(TramoSpecification spec, TramoSpecification defspec, EstimationPolicyType policy) {
        ArimaSpec arima = spec.getArima(), defarima = defspec.isUsingAutoModel() ? null : defspec.getArima();
        switch (policy) {
            case Fixed:
                if (arima.isMean()) {
                    arima.fixMu();
                }
            case FixedParameters:
                arima.setParameterType(ParameterType.Fixed);
                break;
            case FreeParameters:
            case LastOutliers:
            case Outliers:
                // clear only free parameters !
                if (defarima != null) {
                    spec.setArima(defarima.clone());
                } else {
                    arima.clearParameters();
                }
                break;
            case Outliers_StochasticComponent:
                if (defarima != null) {
                    spec.setArima(defarima.clone());
                } else {
                    spec.setAutoModel(defspec.getAutoModel());
                }
                break;

        }
    }

    private void refreshOutliersSpec(TramoSpecification spec, TramoSpecification defspec, TsDomain frozen, EstimationPolicyType policy) {
        RegressionSpec rspec = spec.getRegression(), defrspec = defspec.getRegression();
        OutlierSpec defospec = defspec.getOutliers();
        switch (policy) {
            case Fixed:
            case FixedParameters:
            case FreeParameters:
                // nothing to do
                break;

            case LastOutliers:
                OutlierDefinition[] o = rspec.getOutliers();
                // reset the default outliers detection an the default pre-specified outliers, if any
                rspec.setOutliers(defrspec.getOutliers());
                if (frozen != null && o != null) {
                    for (int j = 0; j < o.length; ++j) {
                        OutlierDefinition cur = o[j];
                        if (frozen.search(cur.getPosition()) >= 0 && !defrspec.contains(cur)) {
                            rspec.add(cur);
                        }
                    }
                }
                // reset the default outliers detection, if any
                OutlierSpec no = defospec.clone();
                if (frozen != null) {
                    no.getSpan().from(frozen.getEnd().firstday());
                }
                spec.setOutliers(no);
                break;
            case Outliers:
            case Outliers_StochasticComponent:
                // reset the default outliers detection and the default pre-specified outliers, if any
                spec.setOutliers(defospec.clone());
                rspec.setOutliers(defrspec.getOutliers());
                break;
        }
    }

    @Override
    public boolean updatePointSpecification(SaItem item) {
        if (item.getStatus() != SaItem.Status.Valid) {
            return false;
        }
        TramoSeatsSpecification spec = (TramoSeatsSpecification) item.getEstimationSpecification().clone();
        CompositeResults rslts = item.process();
        PreprocessingModel regarima = GenericSaResults.getPreprocessingModel(rslts);
        TramoSpecification tspec = spec.getTramoSpecification();
        if (regarima == null) {
            return false;
        }
        TransformSpec transform = tspec.getTransform();
        if (regarima.isMultiplicative()) {
            transform.setFunction(DefaultTransformationType.Log);
        } else {
            transform.setFunction(DefaultTransformationType.None);
        }
        RegressionSpec rspec = tspec.getRegression();
        CalendarSpec cspec = rspec.getCalendar();
        TradingDaysSpec tdspec = cspec.getTradingDays();
        EasterSpec espec = cspec.getEaster();
        TsVariableList vars = regarima.description.buildRegressionVariables();

        if (tdspec.isUsed() && (tdspec.isTest() || tdspec.isAutomatic())) {
            // leap year
            boolean used = false;
            if (vars.select(ILengthOfPeriodVariable.class).isEmpty()) {
                tdspec.setLeapYear(false);
            }
            if (vars.select(ITradingDaysVariable.class).isEmpty()) {
                tdspec.setTradingDaysType(TradingDaysType.None);
            } else {
                used = true;
            }
            if (used) {
                if (tdspec.isAutomatic()) {
                    tdspec.setAutomatic(false);
                    int ntd = vars.select(ITradingDaysVariable.class).getVariablesCount();
                    if (ntd >= 6) {
                        tdspec.setTradingDaysType(TradingDaysType.TradingDays);
                    } else {
                        tdspec.setTradingDaysType(TradingDaysType.WorkingDays);
                    }
                }
                tdspec.setTest(false);
            } else {
                tdspec.disable();

            }
        }
        if (espec.isUsed() && espec.isTest()) {
            if (vars.select(IEasterVariable.class).isEmpty()) {
                espec.setOption(EasterSpec.Type.Unused);
            } else {
                espec.setTest(false);
            }
        }
        // outliers (if any)
        OutlierSpec ospec = tspec.getOutliers();
        if (ospec.isUsed()) {
            TsVariableSelection<IOutlierVariable> sel = vars.select(OutlierType.Undefined);
            if (!sel.isEmpty()) {
                for (TsVariableSelection.Item<IOutlierVariable> o : sel.elements()) {
                    if (!regarima.description.isPrespecified(o.variable)) {
                        rspec.add(o.variable);
                    }
                }
            }
            ospec.clearTypes();
        }
        // stochastic arima model (including mean)
        tspec.getArima().setArimaComponent(regarima.description.getArimaComponent());
        tspec.setUsingAutoModel(false);
        // update the coefficients of the regarima model
        rspec.clearAllCoefficients();
        double[] b = regarima.estimation.getLikelihood().getB();
        if (b != null) {
            int pos = regarima.description.getRegressionVariablesStartingPosition();
            for (ITsVariable var : vars.items()) {
                int npos = pos + var.getDim();
                rspec.setCoefficients(ITsVariable.shortName(var.getName()), Arrays.copyOfRange(b, pos, npos));
                pos = npos;
            }
        }

        item.setPointSpecification(spec);
        return true;
    }
    // stochastic arima model (including mean)

//    @Override
//    public boolean updateSummary(SaItem item) {
//        throw new UnsupportedOperationException("Not supported yet.");
//
//    }
    @Override
    public SaDocument<?> createDocument() {
        return new TramoSeatsDocument();
    }

    @Override
    public List<ISaSpecification> defaultSpecifications() {
        ArrayList<ISaSpecification> specs = new ArrayList<>();
        specs.add(TramoSeatsSpecification.RSA0);
        specs.add(TramoSeatsSpecification.RSA1);
        specs.add(TramoSeatsSpecification.RSA2);
        specs.add(TramoSeatsSpecification.RSA3);
        specs.add(TramoSeatsSpecification.RSA4);
        specs.add(TramoSeatsSpecification.RSA5);
        specs.add(TramoSeatsSpecification.RSAfull);
        return specs;
    }

    @Override
    public void dispose() {
    }

    @Override
    public AlgorithmDescriptor getInformation() {
        return TramoSeatsProcessingFactory.instance.getInformation();
    }

    @Override
    public boolean canHandle(IProcSpecification spec) {
        return TramoSeatsProcessingFactory.instance.canHandle(spec);
    }

    @Override
    public IProcessing<TsData, CompositeResults> generateProcessing(TramoSeatsSpecification specification, ProcessingContext context) {
        return TramoSeatsProcessingFactory.instance.generateProcessing(specification, context);
    }

    @Override
    public Map<String, Class> getSpecificationDictionary(Class<TramoSeatsSpecification> specClass) {
        return TramoSeatsProcessingFactory.instance.getSpecificationDictionary(specClass);
    }

    @Override
    public Map<String, Class> getOutputDictionary(boolean compact) {
        return TramoSeatsProcessingFactory.instance.getOutputDictionary(compact);
    }
}
