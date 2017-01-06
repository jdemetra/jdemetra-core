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
package ec.tstoolkit.modelling.arima.x13;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.modelling.arima.IPreprocessingModule;
import ec.tstoolkit.modelling.arima.IRegressionTest;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.ProcessingResult;
import ec.tstoolkit.modelling.RegStatus;
import ec.tstoolkit.modelling.arima.SeparateRegressionTest;
import ec.tstoolkit.modelling.Variable;
import ec.tstoolkit.modelling.arima.DerivedRegressionTest;
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
public class RegressionVariablesTest2 implements IPreprocessingModule {

    public static final double CVAL = 1.96;
    public static final double TSIG = 1;
    private IRegressionTest test_, mu_;
    private DerivedRegressionTest derived_;

    public RegressionVariablesTest2(double cval) {
        test_ = new SeparateRegressionTest(cval);
        derived_ = new DerivedRegressionTest(cval);
        mu_ = new SeparateRegressionTest(cval);
    }

    public RegressionVariablesTest2(double cval, double tsig) {
        test_ = new SeparateRegressionTest(cval);
        derived_ = new DerivedRegressionTest(cval);
        mu_ = new SeparateRegressionTest(tsig);
    }

    public void reset() {
    }

    @Override
    public ProcessingResult process(ModellingContext context) {
        ConcentratedLikelihood ll = context.estimation.getLikelihood();
        //InformationSet tdsubset = context.information.subSet(PreprocessingDictionary.PREPROCESSING).subSet(PreprocessingDictionary.CALENDAR);
        //InformationSet esubset = context.information.subSet(PreprocessingDictionary.PREPROCESSING).subSet(PreprocessingDictionary.EASTER);

        // td and lp are jointly tested
        TsVariableList x = context.description.buildRegressionVariables();
        int start = context.description.getRegressionVariablesStartingPosition();
        boolean changed = false;

        TsVariableSelection tdsel = x.selectCompatible(ITradingDaysVariable.class);
        TsVariableSelection.Item<ITsVariable>[] tditems = tdsel.elements();
        boolean usetd = false;
        for (int i = 0; i < tditems.length; ++i) {
            Variable search = context.description.searchVariable(tditems[i].variable);
            if (search.status.needTesting()) {
                if (!test_.accept(ll, -1, start + tditems[i].position, tditems[i].variable.getDim(), null)
                        && !derived_.accept(ll, -1, start + tditems[i].position, tditems[i].variable.getDim(), null)) {
                    search.status=RegStatus.Rejected;
                    changed = true;
                } else {
                    usetd = true;
                }
            }
        }
        TsVariableSelection lpsel = x.selectCompatible(ILengthOfPeriodVariable.class);
        TsVariableSelection.Item<ITsVariable>[] lpitems = lpsel.elements();
        for (int i = 0; i < lpitems.length; ++i) {
            Variable search = context.description.searchVariable(lpitems[i].variable);
            if (search.status.needTesting()) {
                if (!usetd || !test_.accept(ll, -1, start + lpitems[i].position, lpitems[i].variable.getDim(), null)) {
                    search.status=RegStatus.Rejected;
                    changed = true;
                }
            }
        }

        TsVariableSelection mhsel = x.selectCompatible(IMovingHolidayVariable.class);
        TsVariableSelection.Item<ITsVariable>[] mhitems = mhsel.elements();
        for (int i = 0; i < mhitems.length; ++i) {
            Variable search = context.description.searchVariable(mhitems[i].variable);
            if (search.status.needTesting()) {
                if (!test_.accept(ll, -1, start + mhitems[i].position, mhitems[i].variable.getDim(), null)) {
                    search.status=RegStatus.Rejected;
                    changed = true;
                }
            }
        }

        if (context.automodelling && context.description.isEstimatedMean()) {
            if (!mu_.accept(ll, -1, 0, 1, null)) {
                context.description.setMean(false);
                changed = true;
            }
        }

        if (changed) {
            context.estimation = null;
            return ProcessingResult.Changed;
        } else {
            return ProcessingResult.Unchanged;
        }

    }
}
