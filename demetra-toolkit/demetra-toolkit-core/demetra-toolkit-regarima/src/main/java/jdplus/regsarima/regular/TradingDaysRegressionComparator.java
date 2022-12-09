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
package jdplus.regsarima.regular;

import demetra.stats.ProbabilityType;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.GenericTradingDays;
import demetra.timeseries.regression.GenericTradingDaysVariable;
import demetra.timeseries.regression.ILengthOfPeriodVariable;
import demetra.timeseries.regression.Variable;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaUtility;
import jdplus.sarima.SarimaModel;
import demetra.timeseries.regression.ITradingDaysVariable;
import java.util.Comparator;
import java.util.function.ToDoubleFunction;
import jdplus.dstats.F;
import jdplus.regarima.IRegArimaComputer;
import jdplus.regarima.RegArimaModel;
import jdplus.stats.likelihood.ConcentratedLikelihoodWithMissing;

/**
 */
@lombok.experimental.UtilityClass
public class TradingDaysRegressionComparator {

    public static final ITradingDaysVariable[] ALL = new ITradingDaysVariable[]{
        new GenericTradingDaysVariable(DayClustering.TD2, GenericTradingDays.Type.CONTRAST),
        new GenericTradingDaysVariable(DayClustering.TD3, GenericTradingDays.Type.CONTRAST),
        new GenericTradingDaysVariable(DayClustering.TD3c, GenericTradingDays.Type.CONTRAST),
        new GenericTradingDaysVariable(DayClustering.TD4, GenericTradingDays.Type.CONTRAST),
        new GenericTradingDaysVariable(DayClustering.TD7, GenericTradingDays.Type.CONTRAST)};

    public static final ITradingDaysVariable[] ALL_NESTED = new ITradingDaysVariable[]{
        new GenericTradingDaysVariable(DayClustering.TD2, GenericTradingDays.Type.CONTRAST),
        new GenericTradingDaysVariable(DayClustering.TD3, GenericTradingDays.Type.CONTRAST),
        new GenericTradingDaysVariable(DayClustering.TD4, GenericTradingDays.Type.CONTRAST),
        new GenericTradingDaysVariable(DayClustering.TD7, GenericTradingDays.Type.CONTRAST)};

    public static final ITradingDaysVariable[] DEFAULT = new ITradingDaysVariable[]{
        new GenericTradingDaysVariable(DayClustering.TD2, GenericTradingDays.Type.CONTRAST),
        new GenericTradingDaysVariable(DayClustering.TD3, GenericTradingDays.Type.CONTRAST),
        new GenericTradingDaysVariable(DayClustering.TD7, GenericTradingDays.Type.CONTRAST)};

    public static final ITradingDaysVariable[] LEGACY = new ITradingDaysVariable[]{
        new GenericTradingDaysVariable(DayClustering.TD2, GenericTradingDays.Type.CONTRAST),
        new GenericTradingDaysVariable(DayClustering.TD7, GenericTradingDays.Type.CONTRAST)};

    /**
     *
     * @param description
     * @param td Set of td variables to be tested
     * @param lp Leap year variable. Will not be taken into account if the model
     * is pre-adjusted. could be null
     * @param eps
     * @return
     */
    public RegArimaEstimation<SarimaModel>[] test(ModelDescription description, ITradingDaysVariable[] td, ILengthOfPeriodVariable lp, double eps) {

        RegArimaEstimation<SarimaModel>[] rslt = new RegArimaEstimation[td.length + 2];

        ModelDescription refdesc = ModelDescription.copyOf(description);
        refdesc.remove("td");
        refdesc.remove("lp");
        refdesc.setAirline(true);
        refdesc.setMean(true);

        boolean useLp = lp != null && !refdesc.isAdjusted();

        IRegArimaComputer<SarimaModel> processor = RegArimaUtility.processor(true, eps);
        rslt[0] = refdesc.estimate(processor);
        if (useLp) {
            refdesc.addVariable(Variable.variable("lp", lp));
            rslt[1] = refdesc.estimate(processor);
        }
        for (int i = 0; i < td.length; ++i) {
            ModelDescription cdesc = ModelDescription.copyOf(refdesc);
            cdesc.addVariable(Variable.variable("td", td[i]));
            rslt[i + 2] = cdesc.estimate(processor);
        }

        return rslt;
    }

    /**
     *
     * @param description
     * @param td Set of td variables to be tested
     * @param lp Leap year variable. Will not be taken into account if the model
     * is pre-adjusted. could be null
     * @param eps
     * @return
     */
    public RegArimaEstimation<SarimaModel>[] testRestrictions(ModelDescription description, ITradingDaysVariable[] td, ILengthOfPeriodVariable lp, double eps) {
        RegArimaEstimation<SarimaModel>[] rslt = new RegArimaEstimation[td.length + 2];
        int lastModel = td.length - 1;

        ModelDescription refdesc = ModelDescription.copyOf(description);
        refdesc.remove("td");
        refdesc.remove("lp");
        refdesc.setAirline(true);
        refdesc.setMean(true);
        boolean useLp = lp != null && !refdesc.isAdjusted();
        if (useLp) {
            refdesc.addVariable(Variable.variable("lp", lp));
        }
        ModelDescription cdesc = ModelDescription.copyOf(refdesc);
        cdesc.addVariable(Variable.variable("td", td[lastModel]));
        IRegArimaComputer<SarimaModel> processor = RegArimaUtility.processor(true, eps);
        RegArimaEstimation<SarimaModel> fullEstimation = cdesc.estimate(processor);
        rslt[lastModel + 2] = fullEstimation;
        double llcorr = fullEstimation.getLlAdjustment();
        RegArimaModel<SarimaModel> reg;
        for (int i = 0; i < lastModel; ++i) {
            cdesc = ModelDescription.copyOf(refdesc);
            cdesc.addVariable(Variable.variable("td", td[i]));
            reg = RegArimaModel.of(cdesc.regarima(), fullEstimation.getModel().arima());
            rslt[i + 2] = RegArimaEstimation.of(reg, llcorr, 2);
        }
        if (useLp) {
            reg = RegArimaModel.of(refdesc.regarima(), fullEstimation.getModel().arima());
            rslt[1] = RegArimaEstimation.of(reg, llcorr, 2);
            refdesc.remove("lp");
        }
        reg = RegArimaModel.of(refdesc.regarima(), fullEstimation.getModel().arima());
        rslt[0] = RegArimaEstimation.of(reg, llcorr, 2);
        return rslt;
    }

    public int bestModel(RegArimaEstimation<SarimaModel>[] rslt, Comparator<RegArimaEstimation<SarimaModel>> cmp) {
        int best = 0;
        while (rslt[best] == null && best < rslt.length) {
            ++best;
        }
        if (best == rslt.length) {
            return -1;
        }

        for (int i = best + 1; i < rslt.length; ++i) {
            if (rslt[i] != null && cmp.compare(rslt[best], rslt[i]) > 0) {
                best = i;
            }
        }
        return best;
    }

    public int waldTest(RegArimaEstimation<SarimaModel>[] rslt, double pval, double prest) {
        // we see if we accept restrictions or not
        int best = rslt.length - 1;
        ConcentratedLikelihoodWithMissing ll = rslt[best].getConcentratedLikelihood();
        ConcentratedLikelihoodWithMissing ll0 = rslt[0].getConcentratedLikelihood();
        int df = ll.degreesOfFreedom() - 2;
        double sigma = ll.ssq() / df;

        double[] pvals = new double[rslt.length];
        double[] ssq = new double[rslt.length];
        int[] nx = new int[rslt.length];
        nx[0] = ll0.nx();
        nx[best] = ll.nx();
        ssq[0] = ll0.ssq();
        ssq[best] = ll.ssq();
        for (int i = 1; i < best; ++i) {
            if (rslt[i] != null) {
                ConcentratedLikelihoodWithMissing lli = rslt[i].getConcentratedLikelihood();
                nx[i] = lli.nx();
                ssq[i] = lli.ssq();
            }
        }

        for (int i = 1; i <= best; ++i) {
            int n = nx[i] - nx[0];
            if (n == 0) {
                pvals[i] = 1;
            } else {
                double f = (ssq[0] - ssq[i]) / (n * sigma);
                if (f > 0) {
                    F fdist = new F(n, df);
                    pvals[i] = fdist.getProbability(f, ProbabilityType.Upper);
                } else {
                    pvals[i] = 1;
                }
            }
        }

        while (best > 0 && pvals[best] > pval) {
            --best;
        }
        if (best <= 1) {
            return best;
        }
        // try to impose restrictions
        int next = best - 1;
        while (next > 0) {
            // just check acceptable cases
            if (pvals[next] < pval) {
                int ndel = nx[best] - nx[next];
                double fdel = (ssq[next] - ssq[best]) / (ndel * sigma);
                if (fdel > 0) {
                    F f = new F(ndel, df);
                    double pdel = f.getProbability(fdel, ProbabilityType.Upper);
                    if (pdel > prest) {
                        best = next;
                    }
                }
            }
            --next;
        }
        return best;
    }

    public Comparator<RegArimaEstimation<SarimaModel>> aicComparator() {
        return comparator(e -> e.statistics().getAIC());
    }

    public Comparator<RegArimaEstimation<SarimaModel>> aiccComparator() {
        return comparator(e -> e.statistics().getAICC());
    }

    public Comparator<RegArimaEstimation<SarimaModel>> biccComparator() {
        return comparator(e -> e.statistics().getBICC());
    }

    public Comparator<RegArimaEstimation<SarimaModel>> bicComparator() {
        return comparator(e -> e.statistics().getBIC());
    }

    private Comparator<RegArimaEstimation<SarimaModel>> comparator(ToDoubleFunction<RegArimaEstimation<SarimaModel>> fn) {
        return (RegArimaEstimation<SarimaModel> e1, RegArimaEstimation<SarimaModel> e2) -> {
            if (e1 == null) {
                return 1;
            } else if (e2 == null) {
                return -1;
            } else {
                return Double.compare(fn.applyAsDouble(e1), fn.applyAsDouble(e2));
            }
        };
    }
}
