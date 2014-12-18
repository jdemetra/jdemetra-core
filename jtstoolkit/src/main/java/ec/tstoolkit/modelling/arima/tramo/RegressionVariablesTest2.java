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

import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.RegStatus;
import ec.tstoolkit.modelling.Variable;
import ec.tstoolkit.modelling.arima.*;
import ec.tstoolkit.sarima.estimation.GlsSarimaMonitor;
import ec.tstoolkit.timeseries.regression.ILengthOfPeriodVariable;
import ec.tstoolkit.timeseries.regression.IMovingHolidayVariable;
import ec.tstoolkit.timeseries.regression.ITradingDaysVariable;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.IUserTsVariable;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.regression.TsVariableSelection;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class RegressionVariablesTest2 extends AbstractTramoModule implements IPreprocessingModule {

    private final IRegressionTest tdTest_, wdTest_, lpTest_, mhTest_, meanTest_;

    public RegressionVariablesTest2(double cval, double mval) {
        tdTest_ = new SeparateRegressionTest(cval, cval);
        wdTest_ = new SeparateRegressionTest(cval);
        lpTest_ = new SeparateRegressionTest(cval);
        mhTest_ = new SeparateRegressionTest(cval);
        meanTest_ = new SeparateRegressionTest(mval);
    }

    private void addInfo(ModelDescription desc, InformationSet information) {
        InformationSet subset = information.subSet(PreprocessingDictionary.CALENDAR);
        subset.set("count", Variable.usedVariablesCount(desc.getCalendars()));
        InformationSet esubset = information.subSet(PreprocessingDictionary.EASTER);
        esubset.set("easter", Variable.usedVariablesCount(desc.getMovingHolidays()));
    }

    @Override
    public ProcessingResult process(final ModellingContext context) {
        // estimate the model.
        boolean changed = false;
        int nhp = context.description.getArimaComponent().getFreeParametersCount();
        ConcentratedLikelihood ll = context.estimation.getLikelihood();
        InformationSet tdsubset = context.information.subSet(PreprocessingDictionary.CALENDAR);
        InformationSet esubset = context.information.subSet(PreprocessingDictionary.EASTER);

        // td and lp together !
        TsVariableList x = context.description.buildRegressionVariables();
        TsVariableSelection tdsel = x.selectCompatible(ITradingDaysVariable.class);
        TsVariableSelection lpsel = x.selectCompatible(ILengthOfPeriodVariable.class);

        boolean usetd = false;
        TsVariableSelection.Item<ITsVariable>[] items = tdsel.elements();
        int start = context.description.getRegressionVariablesStartingPosition();
        for (int i = 0; i < items.length; ++i) {
            Variable search = Variable.search(context.description.getCalendars(), items[i].variable);
            if (search.status.needTesting()) {
                IRegressionTest test = items[i].variable.getDim() == 1 ? wdTest_ : tdTest_;
                if (test.accept(ll, nhp, start + items[i].position, items[i].variable.getDim(), tdsubset)) {
                    usetd = true;
                }
            }
        }

        if (!usetd) {
            items = lpsel.elements();
            for (int i = 0; i < items.length; ++i) {
                Variable search = Variable.search(context.description.getCalendars(), items[i].variable);
                if (search.status.needTesting()) {
                    if (lpTest_.accept(ll, nhp, start + items[i].position, items[i].variable.getDim(), tdsubset)) {
                        usetd = true;
                    }
                }
            }
        }

        items = tdsel.elements();
        for (int i = 0; i < items.length; ++i) {
            Variable search = Variable.search(context.description.getCalendars(), items[i].variable);
            if (search.status.needTesting()) {
                if (usetd) {
                    search.status = RegStatus.Accepted;
                } else {
                    search.status = RegStatus.Rejected;
                    changed = true;
                }
            }
        }
        items = lpsel.elements();
        for (int i = 0; i < items.length; ++i) {
            Variable search = Variable.search(context.description.getCalendars(), items[i].variable);
            if (search.status.needTesting()) {
                if (usetd) {
                    search.status = RegStatus.Accepted;
                } else {
                    search.status = RegStatus.Rejected;
                    changed = true;
                }
            }
        }

        TsVariableSelection mhsel = x.selectCompatible(IMovingHolidayVariable.class);
        items = mhsel.elements();
        for (int i = 0; i < items.length; ++i) {
            Variable search = Variable.search(context.description.getMovingHolidays(), items[i].variable);
            if (search.status.needTesting()) {
                if (mhTest_.accept(ll, nhp, start + items[i].position, items[i].variable.getDim(), esubset)) {
                    search.status = RegStatus.Accepted;
                } else {
                    search.status = RegStatus.Rejected;
                    changed = true;
                }
            }
        }

        // other user variables
        // td
        if (!testUsers(x, context.description.getCalendars(), start, ll, nhp)) {
            changed = true;
        }
        if (!testUsers(x, context.description.getMovingHolidays(), start, ll, nhp)) {
            changed = true;
        }
        if (!testUsers(x, context.description.getUserVariables(), start, ll, nhp)) {
            changed = true;
        }

        if (context.automodelling && context.description.isMean() && !meanTest_.accept(ll, nhp, 0, 1, esubset)) {
            context.description.setMean(false);
            changed = true;
        }

        addInfo(context.description, context.information);
        if (changed)
            context.estimation=null;
        return changed ? ProcessingResult.Changed : ProcessingResult.Unchanged;
    }

    private boolean testUsers(final TsVariableList x, final List<Variable> vars, int start, ConcentratedLikelihood ll, int nhp) {
        TsVariableSelection sel = x.select(new SelectorImpl(vars));
        if (sel.isEmpty()) {
            return true;
        }
        TsVariableSelection.Item[] items = sel.elements();
        boolean changed = false;
        for (int i = 0; i < items.length; ++i) {
            Variable search = Variable.search(vars, items[i].variable);
            if (search.status.needTesting()) {
                IRegressionTest test = items[i].variable.getDim() == 1 ? wdTest_ : tdTest_;
                if (test.accept(ll, nhp, start + items[i].position, items[i].variable.getDim(), null)) {
                    search.status = RegStatus.Accepted;
                } else {
                    search.status = RegStatus.Rejected;
                    changed = true;
                }
            }
        }
        return changed;
    }

    private static class SelectorImpl implements TsVariableList.ISelector {

        private final List<Variable> vars;

        public SelectorImpl(List<Variable> vars) {
            this.vars = vars;
        }

        @Override
        public boolean accept(ITsVariable var) {
            return var instanceof IUserTsVariable && Variable.search(vars, var) != null;
        }
    }
}
