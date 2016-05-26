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

import ec.tstoolkit.modelling.arima.tramo.*;
import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.arima.estimation.LikelihoodStatistics;
import ec.tstoolkit.dstats.F;
import ec.tstoolkit.dstats.ProbabilityType;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.RegStatus;
import ec.tstoolkit.modelling.Variable;
import ec.tstoolkit.modelling.arima.*;
import ec.tstoolkit.timeseries.DayClustering;
import ec.tstoolkit.timeseries.calendars.GenericTradingDays;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.*;
import java.util.List;

/**
 * * @author gianluca, jean Correction 22/7/2014. pre-specified Easter effect
 * was not handled with auto-td
 */
public class TradingDaysSelectionModule extends DemetraModule implements IPreprocessingModule {

    private static final String REGS = "Regression variables";

    private static final double DEF_MODEL_EPS = .01, DEF_CONSTRAINT_EPS = .03;
    private static final GenericTradingDays[] DEF_TD
            = new GenericTradingDays[]{
                GenericTradingDays.contrasts(DayClustering.TD7),
                GenericTradingDays.contrasts(DayClustering.TD4),
                GenericTradingDays.contrasts(DayClustering.TD3),
                GenericTradingDays.contrasts(DayClustering.TD2)
            };
    private final GenericTradingDays[] tdVars;
    private PreprocessingModel tdModel;
    private LikelihoodStatistics ntdStats;
    private LikelihoodStatistics[] tdStats;
    private final double pftd, pfdel;
    private double[] pdel, ptd, bic;
    private double sigma;
    private static final double DEF_TVAL = 1.96;
    private double tval = DEF_TVAL;
    private int choice;

    public TradingDaysSelectionModule(final double pftd, final double pfdel) {
        this.pftd = pftd;
        this.pfdel = pfdel;
        tdVars = DEF_TD;
    }

    public TradingDaysSelectionModule(final double pftd, final double pfdel, final GenericTradingDays[] td) {
        this.pftd = pftd;
        this.pfdel = pfdel;
        tdVars = td;
    }

    public double getPftd() {
        return pftd;
    }

    public double getTValue() {
        return tval;
    }

    public void setTvalue(double tval) {
        this.tval = tval;
    }

    @Override
    public ProcessingResult process(ModellingContext context) {
        tdStats = new LikelihoodStatistics[tdVars.length];
        // Computes the more general model, the parameters are kept in more restrivitve models
        PreprocessingModel tdm = createModel(context, tdVars[0], LengthOfPeriodType.None);
        if (tdm == null) {
            return ProcessingResult.Failed;
        }
        tdModel = tdm;
        ConcentratedLikelihood ll = tdm.estimation.getLikelihood();
        tdStats[0] = tdm.estimation.getStatistics();
        int nhp = tdm.description.getArimaComponent().getFreeParametersCount();
        int df = ll.getN() - ll.getNx() - nhp;
        sigma = ll.getSsqErr() / df;
        ntdStats = check(null, LengthOfPeriodType.LeapYear);

        // compute the other models
        for (int i = 1; i < tdVars.length; ++i) {
            tdStats[i] = check(tdVars[i], LengthOfPeriodType.None);
        }

        calcProb();

        int sel = 0;
        choice = -1;
        for (int i = 0; i < pdel.length; ++i) {
            if (pdel[i] < pfdel && ptd[i] < pftd) {
                choice = i;
                break;
            }
        }
        if (choice < 0 && ptd[pdel.length] < pftd) {// Prefer TD
            choice = pdel.length;

        }
//        addTDInfo(context, 1 - pwd, 1 - ptd, 1 - pdel, sel);
        GenericTradingDays best = choice < 0 ? null : tdVars[choice];
        tdModel = createModel(context, best, LengthOfPeriodType.LeapYear);
        if (best == null || !checkLY(tdModel)) {
            boolean mean = Math.abs(tdModel.estimation.getLikelihood().getTStats(true, 2)[0]) > 1.96;
            context.description = backModel(context, best, LengthOfPeriodType.None, mean);
        } else {
            boolean mean = Math.abs(tdModel.estimation.getLikelihood().getTStats(true, 2)[0]) > 1.96;
            context.description = backModel(context, best, LengthOfPeriodType.LeapYear, mean);
        }
        context.estimation = null;
        return ProcessingResult.Changed;
    }

    private void calcProb() {
        F fstat = new F();
        pdel = new double[tdVars.length - 1];
        ptd = new double[tdVars.length];
        bic = new double[tdVars.length + 1];

        int nhp = tdModel.description.getArimaComponent().getFreeParametersCount();
        ConcentratedLikelihood ll = tdModel.estimation.getLikelihood();
        int df = ll.getN() - ll.getNx() - nhp;
        fstat.setDFDenom(df);
        int nall=tdVars[0].getCount();
        double ftd = (ntdStats.SsqErr - tdStats[0].SsqErr) / ( nall * sigma);
        if (ftd > 0) {
            fstat.setDFNum(nall);
            ptd[0] = fstat.getProbability(ftd, ProbabilityType.Upper);
        }
        bic[0] = tdStats[0].BICC;
        bic[tdVars.length] = ntdStats.BICC;
        for (int i = 1; i < ptd.length; ++i) {
            bic[i] = tdStats[i].BICC;
            int ncur = tdVars[i].getCount(), nprev=tdVars[i-1].getCount(), ndel = nprev - ncur;
            double fdel = (tdStats[i].SsqErr - tdStats[i-1].SsqErr) / (ndel * sigma);
            if (fdel > 0) {
                fstat.setDFNum(ndel);
                pdel[i - 1] = fstat.getProbability(fdel, ProbabilityType.Upper);
            }
            double fcur = (ntdStats.SsqErr - tdStats[i].SsqErr) / (ncur * sigma);
            if (fcur > 0) {
                fstat.setDFNum(ncur);
                ptd[i] = fstat.getProbability(fcur, ProbabilityType.Upper);
            }
        }
    }

    private PreprocessingModel createModel(ModellingContext context, GenericTradingDays td, LengthOfPeriodType lp) {
        ModelDescription model = context.description.clone();
        model.setAirline(context.hasseas);
        model.setMean(true);
        model.setOutliers(null);

        // remove previous calendar effects 
        model.getCalendars().clear();
        if (td != null) {
            GenericTradingDaysVariables vars = new GenericTradingDaysVariables(td);
            model.getCalendars().add(new Variable(vars, ComponentType.CalendarEffect, RegStatus.Accepted));
        }
        if (lp != LengthOfPeriodType.None) {
            model.getCalendars().add(new Variable(new LeapYearVariable(lp), ComponentType.CalendarEffect, RegStatus.Accepted));
        }
        ModellingContext cxt = new ModellingContext();
        cxt.description = model;
        ModelEstimation estimation = new ModelEstimation(model.buildRegArima());
        int nhp = model.getArimaComponent().getFreeParametersCount();
        estimation.compute(monitor(), nhp);
        cxt.estimation = estimation;
        return cxt.current(true);
    }

    private LikelihoodStatistics check(GenericTradingDays td, LengthOfPeriodType lp) {
        ModelDescription model = tdModel.description.clone();

        // remove previous calendar effects 
        model.getCalendars().clear();
        if (td != null) {
            GenericTradingDaysVariables vars = new GenericTradingDaysVariables(td);
            model.getCalendars().add(new Variable(vars, ComponentType.CalendarEffect, RegStatus.Accepted));
        }
        if (lp != LengthOfPeriodType.None) {
            model.getCalendars().add(new Variable(new LeapYearVariable(lp), ComponentType.CalendarEffect, RegStatus.Accepted));
        }
        ModelEstimation estimation = new ModelEstimation(model.buildRegArima());
        int nhp = model.getArimaComponent().getFreeParametersCount();
        estimation.computeLikelihood(nhp);
        return estimation.getStatistics();
    }

    private ModelDescription backModel(ModellingContext context, GenericTradingDays td, LengthOfPeriodType lp, boolean mean) {
        ModelDescription model = context.description.clone();
        if (context.automodelling) {
            model.setMean(mean);
        }
        model.setOutliers(null);
        model.getCalendars().clear();
        if (td != null) {
            GenericTradingDaysVariables vars = new GenericTradingDaysVariables(td);
            model.getCalendars().add(new Variable(vars, ComponentType.CalendarEffect, RegStatus.Accepted));
        }
        if (lp != LengthOfPeriodType.None) {
            model.getCalendars().add(new Variable(new LeapYearVariable(lp), ComponentType.CalendarEffect, RegStatus.Accepted));
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
            addLPInfo(model, t);
            retval = false;
        }
        return retval;
    }

    private boolean checkEE(PreprocessingModel model) {
        boolean retval = true;
        TsVariableList x = model.description.buildRegressionVariables();
        TsVariableSelection sel = x.selectCompatible(EasterVariable.class);
        if (sel.isEmpty()) {
            return false;
        }
        TsVariableSelection.Item<ITsVariable>[] items = sel.elements();
        Variable search = Variable.search(model.description.getMovingHolidays(), items[items.length - 1].variable);
        if (search == null) { // should never happen
            return false;
        }
        if (!search.status.needTesting()) {
            return true;
        }
        ConcentratedLikelihood ll = model.estimation.getLikelihood();
        int start = model.description.getRegressionVariablesStartingPosition();
        double[] Tstat = ll.getTStats(true, 2);//airline
        double t = Tstat[start + items[items.length - 1].position];
        if (Math.abs(t) < 2.2) {
            addEasterInfo(model, t);
            retval = false;
        }
        return retval;
    }

    private void addLPInfo(PreprocessingModel model, double tstat) {
//        StringBuilder builder = new StringBuilder();
//        builder.append("Mean not significant (T=").append(tstat).append(')');
//        model.addProcessingInformation(ProcessingInformation.info(REGS,
//                RegressionTestTD.class.getName(), builder.toString(), null));
    }

    private void addEasterInfo(PreprocessingModel model, double tstat) {
//           StringBuilder builder = new StringBuilder();
//            builder.append("Easter not significant (T=").append(tstat).append(')');
//            model.addProcessingInformation(ProcessingInformation.info(REGS,
//                    RegressionTestTD.class.getName(), builder.toString(), null));
    }

    private void addTDInfo(ModellingContext context, double pwd, double ptd, double pdel, int sel) {
//        if (context.processingLog != null) {
//            StringBuilder builder = new StringBuilder();
//            builder.append("F-test on TD (P-value): ").append(ptd);
//            context.processingLog.add(ProcessingInformation.info(REGS,
//                    RegressionTestTD.class.getName(), builder.toString(), null));
//            builder = new StringBuilder();
//            builder.append("F-test on WD (P-value): ").append(pwd);
//            context.processingLog.add(ProcessingInformation.info(REGS,
//                    RegressionTestTD.class.getName(), builder.toString(), null));
//            builder = new StringBuilder();
//            builder.append("F-test on the restriction from TD to WD (P-value): ").append(pdel);
//            context.processingLog.add(ProcessingInformation.info(REGS,
//                    RegressionTestTD.class.getName(), builder.toString(), null));
//            String msg;
//            switch (sel) {
//                case 1:
//                    msg = "WD selected";
//                    break;
//                case 6:
//                    msg = "TD selected";
//                    break;
//                default:
//                    msg = "No trading days effects";
//                    break;
//            }
//            context.processingLog.add(ProcessingInformation.info(REGS,
//                    RegressionTestTD.class.getName(), msg, null));
//        }
    }

    /**
     * @return the tdStats
     */
    public LikelihoodStatistics[] getTdStats() {
        return tdStats;
    }

    /**
     * @return the pdel
     */
    public double[] getPdel() {
        return pdel;
    }

    /**
     * @return the ptd
     */
    public double[] getPtd() {
        return ptd;
    }

    /**
     * @return the choice
     */
    public int getChoice() {
        return choice;
    }

    /**
     * @return the bic
     */
    public double[] getBic() {
        return bic;
    }
}
