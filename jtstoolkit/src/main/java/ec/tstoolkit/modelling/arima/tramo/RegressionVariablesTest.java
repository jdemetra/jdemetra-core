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
public class RegressionVariablesTest extends AbstractTramoModule implements IPreprocessingModule {

    public static final double CVAL = 1.96;
    public static final double T0 = 2, T1 = 2.6;
    public static final double T2 = 2.2;
    private IRegressionTest tdTest_, wdTest_, lpTest_, mhTest_, meanTest_;

    public RegressionVariablesTest(boolean join) {
        tdTest_ = join ? new JointRegressionTest(.05) : new SeparateRegressionTest(T0, T1);
        wdTest_ = new SeparateRegressionTest(T2);
        lpTest_ = new SeparateRegressionTest(T0);
        mhTest_ = new SeparateRegressionTest(CVAL);
        meanTest_ = new SeparateRegressionTest(CVAL);
    }

    private void addInfo(ModelDescription desc, InformationSet information) {
        InformationSet subset = information.subSet(PreprocessingDictionary.CALENDAR);
        subset.set("count", Variable.usedVariablesCount(desc.getCalendars()));
        InformationSet esubset = information.subSet(PreprocessingDictionary.EASTER);
        esubset.set("easter", Variable.usedVariablesCount(desc.getMovingHolidays()));
    }

    private ModelDescription createTestModel(ModellingContext context) {
        ModelDescription model = context.description.clone();
        model.setAirline(context.hasseas);
        model.setMean(true);

        boolean hastd = model.getCalendars() != null && !model.getCalendars().isEmpty();
        //RegStatus.ToRemove);
        boolean hasmh = model.getMovingHolidays() != null && !model.getMovingHolidays().isEmpty();
        boolean hasuser = model.getUserVariables() != null && !model.getUserVariables().isEmpty();
        model.setPrespecifiedOutliers(null);
        if (hastd || hasmh || hasuser || context.automodelling) {
            return model;
        } else {
            return null;
        }
    }

    @Override
    public ProcessingResult process(final ModellingContext context) {
        // estimate the model.
        ModelDescription tmpModel = createTestModel(context);
        if (tmpModel == null) {
            return ProcessingResult.Unprocessed;
        }
        boolean changed=false;
        ModelEstimation regarima = new ModelEstimation(tmpModel.buildRegArima());
        int nhp = tmpModel.getArimaComponent().getFreeParametersCount();
        GlsSarimaMonitor monitor = getMonitor();
        monitor.setPrecision(1e-4);
        regarima.compute(monitor, nhp);
        ConcentratedLikelihood ll = regarima.getLikelihood();
        InformationSet tdsubset = context.information.subSet(PreprocessingDictionary.CALENDAR);
        InformationSet esubset = context.information.subSet(PreprocessingDictionary.EASTER);

        // td
        TsVariableList x = context.description.buildRegressionVariables();
        TsVariableSelection sel = x.selectCompatible(ITradingDaysVariable.class);
        TsVariableSelection.Item<ITsVariable>[] items = sel.elements();
        boolean usetd = false;
        int start = tmpModel.getRegressionVariablesStartingPosition();
        for (int i = 0; i < items.length; ++i) {
            Variable search = Variable.search(context.description.getCalendars(), items[i].variable);
            if (search.status.needTesting()) {
                IRegressionTest test = items[i].variable.getDim() == 1 ? wdTest_ : tdTest_;
                if (test.accept(ll, nhp, start + items[i].position, items[i].variable.getDim(), tdsubset)) {
                    search.status = RegStatus.Accepted;
                    usetd = true;
                } else {
                    search.status = RegStatus.Rejected;
                    changed=true;
                }
            }
        }

        sel = x.selectCompatible(ILengthOfPeriodVariable.class);
        items = sel.elements();
        for (int i = 0; i < items.length; ++i) {
            Variable search = Variable.search(context.description.getCalendars(), items[i].variable);
            if (search.status.needTesting()) {
                if (usetd && lpTest_.accept(ll, nhp, start + items[i].position, items[i].variable.getDim(), tdsubset)) {
                    search.status = RegStatus.Accepted;
                } else {
                    search.status = RegStatus.Rejected;
                    changed=true;
                }
            }
        }

        sel = x.selectCompatible(IMovingHolidayVariable.class);
        items = sel.elements();
        for (int i = 0; i < items.length; ++i) {
            Variable search = Variable.search(context.description.getMovingHolidays(), items[i].variable);
            if (search.status.needTesting()) {
                if (mhTest_.accept(ll, nhp, start + items[i].position, items[i].variable.getDim(), esubset)) {
                    search.status = RegStatus.Accepted;
                } else {
                    search.status = RegStatus.Rejected;
                    changed=true;
                }
            }
        }

        // other user variables
        // td
        if (!testUsers(x, context.description.getCalendars(), start, ll, nhp))
            changed=true;
        if (! testUsers(x, context.description.getMovingHolidays(), start, ll, nhp))
            changed=true;
        if (! testUsers(x, context.description.getUserVariables(), start, ll, nhp))
            changed=true;

        if (context.automodelling && tmpModel.isMean()&& ! meanTest_.accept(ll, nhp, 0, 1, esubset)) {
            context.description.setMean(false);
            changed=true;
        }

        addInfo(context.description, context.information);
        return changed ? ProcessingResult.Changed : ProcessingResult.Unchanged;
    }

    private boolean testUsers(final TsVariableList x, final List<Variable> vars, int start, ConcentratedLikelihood ll, int nhp) {
        TsVariableSelection sel = x.select(new SelectorImpl(vars));
        if (sel.isEmpty()) {
            return true;
        }
        TsVariableSelection.Item[] items = sel.elements();
        boolean changed=false;
        for (int i = 0; i < items.length; ++i) {
            Variable search = Variable.search(vars, items[i].variable);
            if (search.status.needTesting()) {
                IRegressionTest test = items[i].variable.getDim() == 1 ? wdTest_ : tdTest_;
                if (test.accept(ll, nhp, start + items[i].position, items[i].variable.getDim(), null)) {
                    search.status = RegStatus.Accepted;
                } else {
                    search.status = RegStatus.Rejected;
                    changed=true;
                }
            }
        }
        return changed ;
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
