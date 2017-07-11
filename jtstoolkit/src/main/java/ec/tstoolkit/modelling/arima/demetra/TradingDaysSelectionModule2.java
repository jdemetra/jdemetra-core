/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package ec.tstoolkit.modelling.arima.demetra;

import ec.tstoolkit.arima.estimation.LikelihoodStatistics;
import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.RegStatus;
import ec.tstoolkit.modelling.Variable;
import ec.tstoolkit.modelling.arima.*;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.timeseries.DayClustering;
import ec.tstoolkit.timeseries.calendars.GenericTradingDays;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.regression.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

/**
 *
 */
public class TradingDaysSelectionModule2 extends DemetraModule implements IPreprocessingModule {

    private static final String REGS = "Regression variables";

    private static final double DEF_MODEL_EPS = .01, DEF_CONSTRAINT_EPS = .01;
    private static final GenericTradingDays[] DEF_TD
            = new GenericTradingDays[]{
                GenericTradingDays.contrasts(DayClustering.TD7),
                GenericTradingDays.contrasts(DayClustering.TD4),
                GenericTradingDays.contrasts(DayClustering.TD3),
                GenericTradingDays.contrasts(DayClustering.TD3c),
                GenericTradingDays.contrasts(DayClustering.TD2)
            };
    private static final double DEF_TVAL = 1.96;

    private final boolean fix;
    private final GenericTradingDays[] tdVars;
    private PreprocessingModel[] models;
    private Comparator<PreprocessingModel> comparator;
    private double tval = DEF_TVAL;
    private int choice;

    public TradingDaysSelectionModule2() {
        fix = false;
        tdVars = DEF_TD;
        comparator = (PreprocessingModel l1, PreprocessingModel l2) -> Double.compare(-l1.estimation.getStatistics().AIC, -l2.estimation.getStatistics().AIC);
    }

    public TradingDaysSelectionModule2(boolean fix, Comparator<PreprocessingModel> comparator) {
        this.fix = fix;
        tdVars = DEF_TD;
        this.comparator = comparator;
    }

    public TradingDaysSelectionModule2(boolean fix, final GenericTradingDays[] td, Comparator<PreprocessingModel> comparator) {
        this.fix = fix;
        tdVars = td;
        this.comparator = comparator;
    }

    public double getTValue() {
        return tval;
    }

    public void setTvalue(double tval) {
        this.tval = tval;
    }

    @Override
    public ProcessingResult process(ModellingContext context) {
        models = new PreprocessingModel[tdVars.length + 1];
        // Computes the more general model, the parameters are kept in more restrivitve models
        models[0] = refModel(context, tdVars[0], LengthOfPeriodType.LeapYear);
        for (int i = 1; i < tdVars.length; ++i) {
            models[i] = model(tdVars[i], LengthOfPeriodType.LeapYear);
        }
        models[tdVars.length] = model(null, LengthOfPeriodType.LeapYear);
        Optional<PreprocessingModel> max = Arrays.stream(models).max(comparator);
        for (int i = 0; i < models.length; ++i) {
            if (models[i] == max.get()) {
                choice = i;
                break;
            }
        }

//        addTDInfo(context, 1 - pwd, 1 - ptd, 1 - pdel, sel);
        GenericTradingDays best = choice == tdVars.length ? null : tdVars[choice];
        context.description = backModel(context, best, checkLY(max.get()) ? LengthOfPeriodType.LeapYear : LengthOfPeriodType.None);
        context.estimation = null;
        return ProcessingResult.Changed;
    }

    private PreprocessingModel refModel(ModellingContext context, GenericTradingDays td, LengthOfPeriodType lp) {
        ModelDescription model = context.description.clone();
        model.setAirline(context.hasseas);
        model.setMean(true);
        model.setOutliers(null);

        // remove previous calendar effects 
        model.removeVariable(var->var.isCalendar());
        if (td != null) {
            GenericTradingDaysVariables vars = new GenericTradingDaysVariables(td);
            model.addVariable(Variable.calendarVariable(vars, RegStatus.Accepted));
        }
        if (lp != LengthOfPeriodType.None) {
            model.addVariable(Variable.calendarVariable(new LeapYearVariable(lp), RegStatus.Accepted));
        }
        ModellingContext cxt = new ModellingContext();
        cxt.description = model;
        ModelEstimation estimation = new ModelEstimation(model.buildRegArima());
        int nhp = model.getArimaComponent().getFreeParametersCount();
        estimation.compute(monitor(), nhp);
        cxt.estimation = estimation;
        return cxt.current(true);
    }

    private PreprocessingModel model(GenericTradingDays td, LengthOfPeriodType lp) {
        ModelDescription model = models[0].description.clone();

        // remove previous calendar effects 
        model.removeVariable(var->var.isCalendar());
        if (td != null) {
            GenericTradingDaysVariables vars = new GenericTradingDaysVariables(td);
            model.addVariable(Variable.calendarVariable(vars, RegStatus.Accepted));
        }
        if (lp != LengthOfPeriodType.None) {
            model.addVariable(Variable.calendarVariable(new LeapYearVariable(lp), RegStatus.Accepted));
        }
        ModellingContext cxt = new ModellingContext();
        cxt.description = model;
        int nhp = model.getArimaComponent().getFreeParametersCount();
        if (fix) {
            ModelEstimation estimation = new ModelEstimation(model.buildRegArima());
            estimation.computeLikelihood(nhp);
            cxt.estimation = estimation;
        } else {
            RegArimaEstimation<SarimaModel> nmodel = monitor().optimize(model.buildRegArima());
            ModelEstimation estimation = new ModelEstimation(nmodel.model);
            estimation.computeLikelihood(nhp);
            cxt.estimation = estimation;

        }
        return cxt.current(true);
    }

    private ModelDescription backModel(ModellingContext context, GenericTradingDays td, LengthOfPeriodType lp) {
        ModelDescription model = context.description.clone();
        if (context.automodelling) {
            model.setMean(true);
        }
        model.setOutliers(null);
        model.removeVariable(var->var.isCalendar());
        if (td != null) {
            GenericTradingDaysVariables vars = new GenericTradingDaysVariables(td);
            model.addVariable(Variable.calendarVariable(vars, RegStatus.Accepted));
        }
        if (lp != LengthOfPeriodType.None) {
            model.addVariable(Variable.calendarVariable(new LeapYearVariable(lp), RegStatus.Accepted));
        }
        return model;
    }

    private boolean checkLY(PreprocessingModel model) {
        boolean retval = true;
        ConcentratedLikelihood ll = model.estimation.getLikelihood();
        int start = model.description.getRegressionVariablesStartingPosition();
        TsVariableList x = model.description.buildRegressionVariables();
        TsVariableSelection sel = x.selectCompatible(ILengthOfPeriodVariable.class);
        TsVariableSelection.Item<ITsVariable>[] items = sel.elements();
        double[] Tstat = ll.getTStats(true, 2);//airline
        double t = Tstat[start + items[items.length - 1].position];
        if (Math.abs(t) < tval) {
            retval = false;
        }
        return retval;
    }

    /**
     * @return the choice
     */
    public int getChoice() {
        return choice;
    }

    /**
     * @return the tdVars
     */
    public GenericTradingDays[] getTdVars() {
        return tdVars;
    }

    public PreprocessingModel[] getModels() {
        return models;
    }

    public LikelihoodStatistics[] statistics() {
        LikelihoodStatistics[] stats = new LikelihoodStatistics[models.length];
        for (int i = 0; i < models.length; ++i) {
            stats[i] = models[i] != null ? models[i].estimation.getStatistics() : null;
        }
        return stats;
    }
}
