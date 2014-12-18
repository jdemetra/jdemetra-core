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
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.algorithm.AlgorithmDescriptor;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.algorithm.IProcessing;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.CalendarSpec;
import ec.tstoolkit.modelling.arima.tramo.EasterSpec;
import ec.tstoolkit.modelling.arima.tramo.OutlierSpec;
import ec.tstoolkit.modelling.arima.tramo.RegressionSpec;
import ec.tstoolkit.modelling.arima.tramo.TradingDaysSpec;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.modelling.arima.tramo.TransformSpec;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.EasterVariable;
import ec.tstoolkit.timeseries.regression.ILengthOfPeriodVariable;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import ec.tstoolkit.timeseries.regression.ITradingDaysVariable;
import ec.tstoolkit.timeseries.regression.OutlierDefinition;
import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.regression.TsVariableSelection;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
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

        // automodel
        if (policy == EstimationPolicyType.Outliers_StochasticComponent) {
            if (dtspec.isUsingAutoModel()) {
                ntspec.setAutoModel(dtspec.getAutoModel().clone());
            } else {
                ntspec.setArima(dtspec.getArima().clone());
            }
        }
        // outliers
        RegressionSpec nrspec = ntspec.getRegression(), drspec = dtspec.getRegression();
        if (policy == EstimationPolicyType.Outliers_StochasticComponent || policy == EstimationPolicyType.Outliers) {
            ntspec.setOutliers(dtspec.getOutliers().clone());
            // reset the default outliers detection and the default pre-specified outliers, if any
            nrspec.setOutliers(OutlierDefinition.prespecify(drspec.getOutliers(), true));
        }

        // frozen outliers
        if (policy == EstimationPolicyType.LastOutliers) {
            OutlierDefinition[] o = nrspec.getOutliers();
            // reset the default outliers detection an the default pre-specified outliers, if any
            nrspec.setOutliers(OutlierDefinition.prespecify(drspec.getOutliers(), true));
            if (frozen != null && o != null) {
                for (int j = 0; j < o.length; ++j) {
                    OutlierDefinition cur = o[j];
                    if (frozen.search(cur.position) >= 0 && !drspec.contains(cur)) {
                        nrspec.add(cur.prespecify(true));
                    }
                }
            }
            // reset the default outliers detection, if any
            ntspec.setOutliers(dtspec.getOutliers().clone());
            if (frozen != null) {
                ntspec.getOutliers().getSpan().from(frozen.getEnd().firstday());
            }
        }
        if (policy == EstimationPolicyType.FixedParameters || policy == EstimationPolicyType.FreeParameters) {
            // pre-specify all outliers
            nrspec.setOutliers(OutlierDefinition.prespecify(nrspec.getOutliers(), true));
        }

        // parameters of the regarima model
        if (policy == EstimationPolicyType.Outliers || policy == EstimationPolicyType.LastOutliers || policy == EstimationPolicyType.FreeParameters) {
            ntspec.getArima().clearParameters();
        }

        if (policy == EstimationPolicyType.FixedParameters) {
            ntspec.getArima().setParameterType(ParameterType.Fixed);
        }

        return newspec;
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
            if (vars.select(EasterVariable.class).isEmpty()) {
                espec.setOption(EasterSpec.Type.Unused);
            } else {
                espec.setTest(false);
            }
        }
        // outliers (if any)
        OutlierSpec ospec = tspec.getOutliers();
        if (ospec.isUsed()) {
            TsVariableSelection<IOutlierVariable> sel = vars.select(OutlierType.Undefined, false);
            if (!sel.isEmpty()) {
                for (TsVariableSelection.Item<IOutlierVariable> o : sel.elements()) {
                    rspec.add(o.variable);
                }
            }
            ospec.clearTypes();
        }
        // stochastic arima model (including mean)
        tspec.getArima().setArimaComponent(regarima.description.getArimaComponent());
        tspec.setUsingAutoModel(false);
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
    public Map<String, Class> getOutputDictionary() {
        return TramoSeatsProcessingFactory.instance.getOutputDictionary();
    }
}
