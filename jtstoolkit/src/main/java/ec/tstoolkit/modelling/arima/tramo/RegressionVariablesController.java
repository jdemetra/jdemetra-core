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
package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.arima.IPreprocessingModule;
import ec.tstoolkit.modelling.arima.IRegressionTest;
import ec.tstoolkit.modelling.arima.ModelEstimation;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.ProcessingResult;
import ec.tstoolkit.modelling.RegStatus;
import ec.tstoolkit.modelling.arima.SeparateRegressionTest;
import ec.tstoolkit.modelling.Variable;
import ec.tstoolkit.modelling.arima.*;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.timeseries.regression.ILengthOfPeriodVariable;
import ec.tstoolkit.timeseries.regression.IMovingHolidayVariable;
import ec.tstoolkit.timeseries.regression.ITradingDaysVariable;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.regression.TsVariableSelection;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class RegressionVariablesController extends AbstractTramoModule implements IPreprocessingModule {

    private IRegressionTest tdTest_, lpTest_, mhTest_, meanlTest_, meanuTest_;
    private final static double DEF_CVAL = 2, DEF_LVAL = .5, DEF_UVAL = 1.96, DEF_FPROB = 0.01, DEF_ECVAL = 2.2;

    public RegressionVariablesController(double pftd, boolean join) {
        tdTest_ = join ? new JointRegressionTest(pftd) : new SeparateRegressionTest(DEF_CVAL);
        lpTest_ = new SeparateRegressionTest(DEF_CVAL);
        mhTest_ = new SeparateRegressionTest(DEF_ECVAL);
        meanlTest_ = new SeparateRegressionTest(DEF_LVAL);
        meanuTest_ = new SeparateRegressionTest(DEF_UVAL);
    }

    public void reset() {
    }

    @Override
    public ProcessingResult process(ModellingContext context) {
        boolean hasmean = context.description.isEstimatedMean();
        ModelEstimation oldestimation = context.estimation;
        int nhp = context.description.getArimaComponent().getFreeParametersCount();
        if (!context.description.isEstimatedMean()) {
            context.description.setMean(true);
            ModelEstimation estimation = new ModelEstimation(context.description.buildRegArima());
            estimation.compute(getMonitor(), nhp);
            context.estimation = estimation;
        }
        ConcentratedLikelihood ll = context.estimation.getLikelihood();

        boolean changed = false;
        int start = context.description.getRegressionVariablesStartingPosition();
        // td
        InformationSet tdsubset = context.information.subSet(PreprocessingDictionary.CALENDAR);
        InformationSet esubset = context.information.subSet(PreprocessingDictionary.EASTER);
        TsVariableList x = context.description.buildRegressionVariables();
        TsVariableSelection sel = x.selectCompatible(ITradingDaysVariable.class);
        TsVariableSelection.Item<ITsVariable>[] items = sel.elements();
        boolean usetd = false;
        for (int i = 0; i < items.length; ++i) {
            Variable search = context.description.searchVariable(items[i].variable);
            if (search.status.needTesting()) {
                if (!tdTest_.accept(ll, nhp, start + items[i].position, items[i].variable.getDim(), tdsubset)) {
                    search.status = RegStatus.Rejected;
                    changed = true;
                } else {
                    usetd = true;
                }
            }
        }

        sel = x.selectCompatible(ILengthOfPeriodVariable.class);
        items = sel.elements();
        for (int i = 0; i < items.length; ++i) {
            Variable search = context.description.searchVariable(items[i].variable);
            if (search.status.needTesting()) {
                if (!usetd || !lpTest_.accept(ll, nhp, start + items[i].position, items[i].variable.getDim(), tdsubset)) {
                    search.status = RegStatus.Rejected;
                    changed = true;
                }
            }
        }
        sel = x.selectCompatible(IMovingHolidayVariable.class);
        items = sel.elements();
        for (int i = 0; i < items.length; ++i) {
            Variable search = context.description.searchVariable(items[i].variable);
            if (search.status.needTesting()) {
                if (!mhTest_.accept(ll, nhp, start + items[i].position, items[i].variable.getDim(), esubset)) {
                    search.status = RegStatus.Rejected;
                    changed = true;
                }
            }
        }

        boolean mean = hasmean;
        if (context.automodelling) {
            if (!meanlTest_.accept(ll, nhp, 0, 1, null)) {
                mean = false;
            } else if (meanuTest_.accept(ll, nhp, 0, 1, null)) {
                mean = true;
            }
        }
        boolean mchanged = false;
        context.description.setMean(mean);
        if (mean != hasmean) {
            mchanged = true;
            if (!changed) {
                if (!mean) {
                    context.estimation = oldestimation;
                }
            }
        } else {
            if (!changed) {
                context.estimation = oldestimation;
            }
        }

        if (changed) {
            RegArimaModel<SarimaModel> nregarima = context.description.buildRegArima();
            nregarima.setArima(context.estimation.getArima());
            context.estimation = new ModelEstimation(nregarima, context.description.getLikelihoodCorrection());
            context.estimation.computeLikelihood(nhp);
            return ProcessingResult.Changed;
        } else {
            return mchanged ? ProcessingResult.Changed : ProcessingResult.Unchanged;
        }

    }
}
