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
package demetra.x12;

import demetra.arima.IResidualsComputer;
import demetra.arima.internal.AnsleyFilter;
import demetra.data.DataBlock;
import demetra.design.BuilderPattern;
import demetra.design.Development;
import demetra.regarima.IRegArimaProcessor;
import demetra.regarima.RegArimaEstimation;
import demetra.regarima.RegArimaModel;
import demetra.regarima.ami.IOutliersDetectionModule;
import demetra.regarima.outlier.AbstractSingleOutlierDetector;
import demetra.regarima.outlier.CriticalValueComputer;
import demetra.regarima.outlier.ExactSingleOutlierDetector;
import demetra.regarima.outlier.IRobustStandardDeviationComputer;
import demetra.sarima.GlsSarimaProcessor;
import demetra.sarima.SarimaModel;
import demetra.sarima.internal.HannanRissanenInitializer;
import demetra.modelling.regression.AdditiveOutlier;
import demetra.modelling.regression.IOutlier;
import demetra.modelling.regression.LevelShift;
import demetra.modelling.regression.PeriodicOutlier;
import demetra.modelling.regression.TransitoryChange;
import java.util.ArrayList;

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
        private double cv = 0, pc = 0.14286;
        private IRegArimaProcessor<SarimaModel> processor;
        private int maxOutliers = DEF_MAXOUTLIERS;
        private int maxRound = DEF_MAXROUND;
        private AbstractSingleOutlierDetector sod = new ExactSingleOutlierDetector(IRobustStandardDeviationComputer.mad(false), 
                IResidualsComputer.mlComputer(), 
                new AnsleyFilter());


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

        public Builder reductionOfCriticalValue(double pc) {
            this.pc = pc;
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
            this.sod.addOutlierFactory(LevelShift.FACTORY_ZEROENDED);
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
                p = GlsSarimaProcessor.builder().initializer(HannanRissanenInitializer.builder().stabilize(true).build()).precision(eps).build();
            }
            return new OutliersDetectionModule(sod, p, maxRound, maxOutliers, cv, pc);
        }
    }

    private final int maxRound, maxOutliers;
    private RegArimaModel<SarimaModel> regarima;
    private final ArrayList<int[]> outliers = new ArrayList<>(); // Outliers : (position, type)
    private final AbstractSingleOutlierDetector sod;
    private final IRegArimaProcessor<SarimaModel> processor;
    private final double cv;
    private final double pc;
    private double[] tstats;
    private int round;
    private int selectivity;
    private double curcv;
    public static final double MINCV = 2.8;

    private OutliersDetectionModule(final AbstractSingleOutlierDetector sod, final IRegArimaProcessor<SarimaModel> processor,
            final int maxOutliers, final int maxRound, final double cv, final double pc) {
        this.sod = sod;
        this.processor = processor;
        this.maxOutliers = maxOutliers;
        this.maxRound = maxRound;
        this.cv = cv;
        this.pc = pc;
    }

    @Override
    public boolean process(RegArimaModel<SarimaModel> initialModel) {
        try {
            clear();
            clear();
            int n = initialModel.getY().length();
            sod.setBounds(0, n);
            sod.prepare(n);
            regarima = initialModel;
            estimateModel(true);
            if (curcv == 0) {
                curcv = calcCv();
            }
            return execute();
        } catch (RuntimeException err) {
            return false;
        }
    }

    @Override
    public void setBounds(int start, int end) {
        sod.setBounds(start, end);
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
     */
    public void clear() {
        outliers.clear();
        regarima = null;
    }

    private boolean execute() {
        boolean changed = false;
        double max;
        round = 0;

        do {
            if (!sod.process(regarima)) {
                break;
            }
            max = sod.getMaxTStat();
            if (Math.abs(max) > curcv) {
                round++;
            int type = sod.getMaxOutlierType();
            int pos = sod.getMaxOutlierPosition();
            addOutlier(pos, type);
                changed = true;
                if (!estimateModel(true)) {
                    outliers.remove(outliers.size()-1);
                    estimateModel(false);
                    break;
                }
                /*
                 * int v = verifymodel(cv_); if (v == -1) break; else if (v ==
                 * 0) reestimatemodel();
                 */
                // updatesod();
            } else {
                break;// no outliers to remove...
            }
        } while (round < maxRound && outliers.size() < maxOutliers);

        while (!verifymodel(curcv)) {
            if (!estimateModel(false)) {
                break;
            }
            changed = true;
        }

        return changed;
    }

    public double getCritivalValue() {
        return cv;
    }

    public double getPc() {
        return pc;
    }

    public int getMaxIter() {
        return maxRound;
    }

    /**
     *
     * @return
     */
    public RegArimaModel<SarimaModel> getModel() {
        return regarima;
    }

    /**
     *
     * @return
     */
    public int getOutlierFactoriesCount() {
        return sod.getOutlierFactoriesCount();
    }

    /**
     *
     * @return
     */
    public int getOutliersCount() {
        return outliers.size();
    }

    private boolean estimateModel(boolean full) {
        
        RegArimaEstimation<SarimaModel>estimation = full ? processor.process(regarima) : processor.optimize(regarima);
        if (estimation == null)
            return false;
        regarima = estimation.getModel();
        tstats=estimation.getConcentratedLikelihood().tstats(0, false);
        return true;
     }

    /**
     *
     * @return
     */
    public boolean isInitialized() {
        return regarima != null;
    }

    // step I.1
    // / <summary>
    // / Initialize the model.
    // / 1. If m_irflag, compute the coefficients by OLS. That happens in the
    // first run (no previous estimation)
    // / or when the HR (if used) provides bad estimation (unstable models).
    // / 2. Calc by HR. Used in the first run or when a new outlier is added.
    // The model of the SOD is updated, unless
    // / exact ll estimation is used or HR provides an unstable model.
    // / 3. Calc by exact ll. (option or unstable HR (first round only)).
    // / 4. Gls estimation of the new model
    // / </summary>
    private void removeOutlier(int idx) {
        //
        int opos = regarima.getVariablesCount() - outliers.size() + idx;
        regarima = regarima.toBuilder().removeX(opos).build();
        outliers.remove(idx);
    }

    private boolean verifymodel(double cv) {
        if (regarima == null) {
            return true;
        }
        if (outliers.isEmpty()) {
            return true; // no outlier detected
        }
        int nx0 = regarima.getVariablesCount() - outliers.size();
        int imin = 0;
        for (int i = 1; i < outliers.size(); ++i) {
            if (Math.abs(tstats[i + nx0]) < Math.abs(tstats[imin + nx0])) {
                imin = i;
            }
        }

        if (Math.abs(tstats[nx0 + imin]) >= curcv) {
            return true;
        }
        int[] toremove = outliers.get(imin);
        sod.allow(toremove[0], toremove[1]);
        removeOutlier(imin);
        return false;
    }

    @Override
    public boolean reduceSelectivity() {
        if (curcv == 0) {
            return false;
        }
        --selectivity;
        if (curcv == MINCV) {
            return false;
        }
        curcv = Math.max(MINCV, curcv * (1 - pc));
        return true;
    }

    @Override
    public void setSelectivity(int level) {
        if (selectivity != level) {
            selectivity = level;
            curcv = 0;
        }
    }

    @Override
    public int getSelectivity() {
        return selectivity;
    }

    private double calcCv() {
        double va = this.cv;
        if (va == 0) {
            va = CriticalValueComputer.advancedComputer().applyAsDouble(regarima.getObservationsCount());
        }
        for (int i = 0; i < -selectivity; ++i) {
            va *= (1 - pc);
        }
        return Math.max(va, MINCV);
    }

}
