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
package demetra.tramo;

import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import demetra.design.BuilderPattern;
import demetra.design.Development;
import demetra.likelihood.ConcentratedLikelihood;
import demetra.linearmodel.LeastSquaresResults;
import demetra.linearmodel.LinearModel;
import demetra.linearmodel.Ols;
import demetra.regarima.IRegArimaProcessor;
import demetra.regarima.RegArimaEstimation;
import demetra.regarima.RegArimaModel;
import demetra.regarima.RegArmaModel;
import demetra.regarima.ami.IOutliersDetectionModule;
import demetra.regarima.internal.ConcentratedLikelihoodComputer;
import demetra.regarima.outlier.AbstractSingleOutlierDetector;
import demetra.regarima.outlier.CriticalValueComputer;
import demetra.regarima.outlier.FastOutlierDetector;
import demetra.sarima.GlsSarimaProcessor;
import demetra.sarima.HannanRissanen;
import demetra.sarima.SarimaMapping;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import demetra.sarima.SarmaSpecification;
import demetra.modelling.regression.AdditiveOutlier;
import demetra.modelling.regression.IOutlier;
import demetra.modelling.regression.LevelShift;
import demetra.modelling.regression.PeriodicOutlier;
import demetra.modelling.regression.TransitoryChange;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class OutliersDetectionModule implements IOutliersDetectionModule<SarimaModel> {

    public static int DEF_MAXROUND = 100;
    public static int DEF_MAXOUTLIERS = 50;
    public static final double EPS = 1e-5;

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(OutliersDetectionModule.class)
    public static class Builder {

        private double eps = EPS;
        private double cv = 0;
        private boolean mvx;
        private IRegArimaProcessor<SarimaModel> processor;
        private int maxOutliers = DEF_MAXOUTLIERS;
        private int maxRound = DEF_MAXROUND;
        private AbstractSingleOutlierDetector sod = new FastOutlierDetector(null);

        private Builder() {
        }

        public Builder precision(double eps) {
            this.eps = eps;
            return this;
        }

        public Builder criticalValue(double cv) {
            this.cv = cv;
            return this;
        }

        public Builder maximumLikelihood(boolean mvx) {
            this.mvx = mvx;
            return this;
        }

        public Builder detector(AbstractSingleOutlierDetector sod) {
            this.sod = sod;
            this.sod.clearOutlierFactories();
            return this;
        }

        public Builder processor(IRegArimaProcessor<SarimaModel> processor) {
            this.processor = processor;
            return this;
        }

        public Builder maxOutliers(int max) {
            this.maxOutliers = max;
            return this;
        }

        public Builder maxRound(int max) {
            this.maxRound = max;
            return this;
        }

        public Builder addFactory(IOutlier.IOutlierFactory factory) {
            this.sod.addOutlierFactory(factory);
            return this;
        }

        /**
         *
         * @return
         */
        public Builder setDefault() {
            this.sod.clearOutlierFactories();
            this.sod.addOutlierFactory(AdditiveOutlier.FACTORY);
            this.sod.addOutlierFactory(LevelShift.FACTORY_ZEROSTARTED);
            return this;
        }

        /**
         *
         * @return
         */
        public Builder setAll() {
            setDefault();
            this.sod.addOutlierFactory(new TransitoryChange.Factory(.7));
            return this;
        }

        /**
         *
         * @param period
         * @return
         */
        public Builder setAll(int period) {
            setAll();
            this.sod.addOutlierFactory(new PeriodicOutlier.Factory(period, true));
            return this;
        }

        public OutliersDetectionModule build() {
            IRegArimaProcessor<SarimaModel> p = processor;
            if (p == null) {
                p = GlsSarimaProcessor.builder().precision(eps).build();
            }
            return new OutliersDetectionModule(sod, p, maxRound, maxOutliers, cv, mvx);
        }
    }

    private final int maxRound, maxOutliers;
    private RegArimaModel<SarimaModel> regarima;
    private final ArrayList<int[]> outliers = new ArrayList<>(); // Outliers : (position, type)
    private final AbstractSingleOutlierDetector sod;
    private final IRegArimaProcessor<SarimaModel> processor;
    private final double cv;
    private final boolean mvx;
    private double[] tstats;
    private int nhp;
    private int round;
    // festim = true if the model has to be re-estimated
    private boolean rflag_, backw_, exit_, festim_;
    private int[] lastremoved;
    private DoubleSequence coeff, res;
    //
    private boolean curMvx;
    public static final double MINCV = 2.0;

    private OutliersDetectionModule(final AbstractSingleOutlierDetector sod, final IRegArimaProcessor<SarimaModel> processor,
            final int maxOutliers, final int maxRound, final double cv, final boolean mvx) {
        this.sod = sod;
        this.processor = processor;
        this.maxOutliers = maxOutliers;
        this.maxRound = maxRound;
        this.mvx = mvx;
        this.cv = cv;
    }

    @Override
    public void setBounds(int start, int end) {
        sod.setBounds(start, end);
    }

    @Override
    public void prepare(int n) {
        sod.prepare(n);
    }

    @Override
    public void exclude(int pos, int type) {
        sod.exclude(pos, type);
    }

    /**
     * @return the outliers_
     */
    @Override
    public int[][] getOutliers() {
        return outliers.toArray(new int[outliers.size()][]);
    }

    public IOutlier.IOutlierFactory getFactory(int i) {
        return sod.getOutlierFactory(i);
    }

    @Override
    public boolean process(RegArimaModel<SarimaModel> initialModel) {
        clear();
        int n = initialModel.getY().length();
        sod.setBounds(0, n);
        sod.prepare(n);
        regarima = initialModel;
        nhp = 0;
        int test = comatip(initialModel.arima().specification(), initialModel.isMean(), n);
        if (test < 0) {
            return false;
        } else if (test > 0) {
            curMvx = true;
        } else {
            curMvx = false;
        }

        regarima = initialModel;
         nhp = 0;
        double max;
        try {
            do {
                if (!estimateModel()) {
                    return false;
                }
                boolean search = true;
                if (backw_) {
                    search = verifyModel();
                    if (exit_) {
                        break;
                    }
                }
                if (search) {
                    if (!sod.process(regarima)) {
                        break;
                    }
                    round++;
                    max = sod.getMaxTStat();
                    if (Math.abs(max) < cv) {
                        break;
                    }
                    int type = sod.getMaxOutlierType();
                    int pos = sod.getMaxOutlierPosition();
                    addOutlier(pos, type, sod.coeff(pos, type));
                    if (outliers.size() == maxOutliers) {
                        break;
                    }
                }
            } while (round < maxRound);

            // we should remove non signigicant outlier (witouht re-estimation)
            if (round == maxRound || outliers.size() == maxOutliers) {
                estimateModel();
            }
            festim_ = false;

            while (!verifyModel()) {
                estimateModel();
            }

            return true;
        } catch (RuntimeException err) {
            return false;
        }
    }

    private boolean estimateModel() {
        // step 1 Initial values by OLS
        SarimaModel sarima = regarima.arima();
        SarimaSpecification spec = sarima.specification();
        RegArmaModel<SarimaModel> dm = regarima.differencedModel();
        LinearModel lm = dm.asLinearModel();
        if (rflag_) {
            if (lm.getVariablesCount() > 0) {
                Ols ols = new Ols();
                LeastSquaresResults lsr = ols.compute(lm);
                if (lsr == null) {
                    return false;
                }
                res = lm.calcResiduals(lsr.getCoefficients());
            } else {
                res = lm.getY();
            }

        } else if (coeff != null) {
            res = lm.calcResiduals(coeff);
        } else {
            res = lm.getY();
        }
        boolean stable = true;
        rflag_ = false;

        if (festim_) {
            SarmaSpecification dspec = spec.doStationary();
            if (spec.getParametersCount() != 0) {
                HannanRissanen hr = HannanRissanen.builder().build();
                if (hr.process(res, dspec)) {
                    SarimaModel hrmodel = hr.getModel();
                    SarimaModel stmodel = SarimaMapping.stabilize(hrmodel);
                    stable = stmodel == hrmodel;
                    if (stable || curMvx || round == 0) {
                        regarima = RegArimaModel.of(regarima,
                                SarimaModel.builder(spec)
                                .parameters(stmodel.parameters())
                                .build());
                    } else {
                        rflag_ = true;
                        stable = true;
                    }
                }
            }
            if ((curMvx || !stable) && festim_) {
                return optimizeModel();
            }
        }
        if (lm.getVariablesCount() > 0) {
            updateLikelihood(ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(regarima));
        }

        return true;
    }

    private boolean optimizeModel() {
        RegArimaEstimation<SarimaModel> estimation = processor.optimize(regarima);
        regarima = estimation.getModel();
        updateLikelihood(estimation.getConcentratedLikelihood());
        return true;
    }

    private void updateLikelihood(ConcentratedLikelihood likelihood) {
        coeff = likelihood.coefficients();
        tstats = likelihood.tstats(nhp, true);
    }

    private void clear() {
        rflag_ = true;
        nhp = 0;
        outliers.clear();
        round = 0;
        lastremoved = null;
        coeff = null;
        tstats = null;
        festim_ = true;
        backw_ = false;
        exit_ = false;
        res = null;
        // festim = true if the model has to be re-estimated
    }

    /**
     * Backward procedure (without re-estimation of the model)
     *
     * @param exit
     * @return True means that the model was not modified
     */
    private boolean verifyModel() {
        festim_ = true;
        if (outliers.isEmpty()) {
            return true;
        }
        /*double[] t = m_model.computeLikelihood().getTStats(true,
         m_model.getArma().getParametersCount());*/
        int nx0 = regarima.getVariablesCount() - outliers.size();
        int imin = 0;
        for (int i = 1; i < outliers.size(); ++i) {
            if (Math.abs(tstats[i + nx0]) < Math.abs(tstats[imin + nx0])) {
                imin = i;
            }
        }

        if (Math.abs(tstats[nx0 + imin]) >= cv) {
            return true;
        }
        backw_ = false;
        festim_ = false;
        int[] toremove = outliers.get(imin);
        sod.allow(toremove[0], toremove[1]);
        removeOutlier(imin);
        if (lastremoved != null) {
            if (Arrays.equals(toremove, lastremoved)) {
                exit_ = true;
            }
        }
        lastremoved = toremove;
        return false;
    }

    private void addOutlier(int pos, int type, double c) {
        addOutlier(pos, type);
        double[] tmp;
        if (coeff == null) {
            coeff = DoubleSequence.of(c);
        } else {
            tmp = new double[coeff.length() + 1];
            coeff.copyTo(tmp, 0);
            tmp[coeff.length()] = c;
            coeff = DoubleSequence.ofInternal(tmp);
        }
        backw_ = true;
    }

    private void addOutlier(int pos, int type) {
        int[] o = new int[]{pos, type};
        outliers.add(o);
        double[] xo = new double[regarima.getObservationsCount()];
        DataBlock XO = DataBlock.ofInternal(xo);
        sod.factory(type).fill(pos, XO);
        regarima = regarima.toBuilder().addX(XO).build();
        sod.exclude(pos, type);
    }

    /**
     *
     * @param model
     * @return
     */
    private void removeOutlier(int idx) {
        //
        int opos = regarima.getVariablesCount() - outliers.size() + idx;
        regarima = regarima.toBuilder().removeX(opos).build();
        outliers.remove(idx);
        double[] tmp;
        if (coeff.length() == 1) {
            coeff = null;
        } else {
            if (regarima.isMean()) {
                ++opos;
            }
            tmp = new double[coeff.length() - 1];
            for (int i = 0; i < opos; ++i) {
                tmp[i] = coeff.get(i);
            }
            for (int i = opos + 1; i < coeff.length(); ++i) {
                tmp[i - 1] = coeff.get(i);
            }
            coeff = DoubleSequence.ofInternal(tmp);
        }
    }

    /**
     *
     * @return
     */
    public List<IOutlier.IOutlierFactory> factories() {
        return Collections.unmodifiableList(sod.getFactories());
    }

    public boolean hasUsedEML() {
        return curMvx;
    }

    public double getCritivalValue() {
        return cv;
    }

    private int comatip(SarimaSpecification spec, boolean mean, int n) {
        // int rslt = ml ? 1 : 0;
        // first, check if od is possible
        int nparm = Math.max(spec.getD() + spec.getP() + spec.getPeriod()
                * (spec.getBd() + spec.getBp()), spec.getQ()
                + spec.getPeriod() * spec.getBq())
                + (mean ? 1 : 0)
                + (15 * n) / 100 + spec.getPeriod();
        if (n - nparm <= 0) {
            return -1;
        }
        if (mvx) {
            return 1;
        }
        int ndf1 = TramoUtility.autlar(n, spec);
        int ndf2 = 0;
        if (spec.getP() + spec.getBp() > 0 && spec.getQ() + spec.getBq() > 0) {
            n -= spec.getP() + spec.getPeriod() * spec.getBp();
            spec.setP(0);
            spec.setBp(0);
            ndf2 = TramoUtility.autlar(n, spec);
        }
        if (ndf1 < 0 || ndf2 < 0) {
            return 1;
        } else {
            return 0;
        }
    }


    public static double calcCv(int nobs) {
        return Math.max(CriticalValueComputer.simpleComputer().applyAsDouble(nobs), MINCV);
    }

}
