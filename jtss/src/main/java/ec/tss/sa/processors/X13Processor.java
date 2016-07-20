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

import ec.satoolkit.DecompositionMode;
import ec.satoolkit.GenericSaResults;
import ec.satoolkit.ISaSpecification;
import ec.satoolkit.algorithm.implementation.X13ProcessingFactory;
import ec.satoolkit.x11.X11Specification;
import ec.satoolkit.x13.X13Specification;
import ec.tss.sa.EstimationPolicyType;
import ec.tss.sa.ISaProcessingFactory;
import ec.tss.sa.SaItem;
import ec.tss.sa.documents.SaDocument;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.algorithm.AlgorithmDescriptor;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.algorithm.IProcessing;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.RegressionTestSpec;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.x13.*;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.*;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = ISaProcessingFactory.class)
public class X13Processor implements ISaProcessingFactory<X13Specification> {

    public static final AlgorithmDescriptor DESCRIPTOR = X13ProcessingFactory.DESCRIPTOR;

    public X13Processor() {
    }

    @Override
    public ISaSpecification createSpecification(InformationSet info) {
        X13Specification spec = new X13Specification();
        if (spec.read(info)) {
            return spec;
        } else {
            return null;
        }
    }

    @Override
    public ISaSpecification createSpecification(SaItem doc, TsDomain frozen, EstimationPolicyType policy, boolean nospan) {

        X13Specification spec = (X13Specification) doc.getPointSpecification();
        X13Specification defspec = (X13Specification) doc.getDomainSpecification();
        if (policy == EstimationPolicyType.Complete || spec == null) {
            X13Specification nspec = defspec.clone();
            if (nospan) {
                nspec.getRegArimaSpecification().getBasic().getSpan().all();
            }
            return nspec;
        }

        X13Specification newspec = spec.clone();

        RegArimaSpecification ntspec = newspec.getRegArimaSpecification(),
                dtspec = defspec.getRegArimaSpecification(),
                tspec = spec.getRegArimaSpecification();
        if (nospan) {
            ntspec.getBasic().getSpan().all();
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
            // reset the default outliers detection an the default pre-specified outliers, if any
            nrspec.setOutliers(drspec.getOutliers());
        }

        // frozen outliers
        if (policy == EstimationPolicyType.LastOutliers) {
            OutlierDefinition[] o = nrspec.getOutliers();
            // reset the default outliers detection an the default pre-specified outliers, if any
            nrspec.setOutliers(drspec.getOutliers());
            if (frozen != null && o != null) {
                for (int j = 0; j < o.length; ++j) {
                    OutlierDefinition cur = o[j];
                    if (frozen.search(cur.getPosition()) >= 0 && !drspec.contains(cur)) {
                        nrspec.add(cur);
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
            nrspec.setOutliers(nrspec.getOutliers());
        }

        // parameters of the regarima model
        if (policy == EstimationPolicyType.Outliers || policy == EstimationPolicyType.LastOutliers || policy == EstimationPolicyType.FreeParameters) {
            ntspec.getArima().clearParameters();
        }

        if (policy == EstimationPolicyType.FixedParameters) {
            ntspec.getArima().setParameterType(ParameterType.Fixed);
        }

        // we should consider the X11 options chosen by the software ...
        return newspec;
    }

    @Override
    public boolean updatePointSpecification(SaItem item) {
        if (item.getStatus() != SaItem.Status.Valid) {
            return false;
        }
        X13Specification spec = (X13Specification) item.getEstimationSpecification().clone();
        CompositeResults rslts = item.process();
        PreprocessingModel regarima = GenericSaResults.getPreprocessingModel(rslts);
        RegArimaSpecification tspec = spec.getRegArimaSpecification();
        if (regarima == null) {
            // nothing to do. Simply copy the specification
            item.setPointSpecification(spec);
            return true;
        }
        TransformSpec transform = tspec.getTransform();
        X11Specification x11 = spec.getX11Specification();
        if (regarima.isMultiplicative()) {
            transform.setFunction(DefaultTransformationType.Log);
            if (x11.getMode() == DecompositionMode.Undefined) {
                x11.setMode(DecompositionMode.Multiplicative);
            }
        } else if (transform != null) {
            transform.setFunction(DefaultTransformationType.None);
            if (x11.getMode() == DecompositionMode.Undefined) {
                x11.setMode(DecompositionMode.Additive);
            }
        }
        RegressionSpec rspec = tspec.getRegression();
        TradingDaysSpec tdspec = rspec.getTradingDays();
        MovingHolidaySpec espec = rspec.getEaster();
        TsVariableList vars = regarima.description.buildRegressionVariables();

        if (tdspec.getTest() != RegressionTestSpec.None) {
            // leap year
            boolean used = false;
            tdspec.setTest(RegressionTestSpec.None);
            if (vars.select(ITradingDaysVariable.class).isEmpty()) {
                tdspec.setTradingDaysType(TradingDaysType.None);
            } else {
                used = true;
                if (tdspec.isAutoAdjust() && regarima.isMultiplicative()
                        && tdspec.getLengthOfPeriod() != LengthOfPeriodType.None) {
                    transform.setAdjust(tdspec.getLengthOfPeriod());
                }
            }
            if (vars.select(ILengthOfPeriodVariable.class).isEmpty()) {
                tdspec.setLengthOfPeriod(LengthOfPeriodType.None);
            }
            if (!used) {
                rspec.getTradingDays().disable();
            }
        }
        if (espec != null && espec.getTest() != RegressionTestSpec.None) {
            if (espec.getType() == MovingHolidaySpec.Type.Easter) {
                TsVariableSelection<EasterVariable> evar = vars.select(EasterVariable.class);
                if (evar.isEmpty()) {
                    rspec.removeMovingHolidays(espec);
                } else {
                    espec.setTest(RegressionTestSpec.None);
                    espec.setW(evar.get(0).variable.getDuration());
                }
            } else if (espec.getType() == MovingHolidaySpec.Type.JulianEaster) {
                TsVariableSelection<JulianEasterVariable> evar = vars.select(JulianEasterVariable.class);
                if (evar.isEmpty()) {
                    rspec.removeMovingHolidays(espec);
                } else {
                    espec.setTest(RegressionTestSpec.None);
                    espec.setW(evar.get(0).variable.getDuration());
                }
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
        item.setPointSpecification(spec);
        return true;
    }

    @Override
    public SaDocument<?> createDocument() {
        return new X13Document();
    }

    @Override
    public List<ISaSpecification> defaultSpecifications() {
        ArrayList<ISaSpecification> specs = new ArrayList<>();
        specs.add(X13Specification.RSAX11);
        specs.add(X13Specification.RSA0);
        specs.add(X13Specification.RSA1);
        specs.add(X13Specification.RSA2);
        specs.add(X13Specification.RSA3);
        specs.add(X13Specification.RSA4);
        specs.add(X13Specification.RSA5);
        return specs;
    }

    @Override
    public void dispose() {
    }

    @Override
    public AlgorithmDescriptor getInformation() {
        return X13ProcessingFactory.instance.getInformation(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean canHandle(IProcSpecification spec) {
        return X13ProcessingFactory.instance.canHandle(spec);
    }

    @Override
    public IProcessing<TsData, CompositeResults> generateProcessing(X13Specification specification, ProcessingContext context) {
        return X13ProcessingFactory.instance.generateProcessing(specification, context);
    }

    @Override
    public Map<String, Class> getSpecificationDictionary(Class<X13Specification> specClass) {
        return X13ProcessingFactory.instance.getSpecificationDictionary(specClass);
    }

    @Override
    public Map<String, Class> getOutputDictionary() {
        return X13ProcessingFactory.instance.getOutputDictionary();
    }
}
