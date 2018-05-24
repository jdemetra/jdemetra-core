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
package demetra.x12.internal;

import demetra.arima.IResidualsComputer;
import demetra.arima.internal.AnsleyFilter;
import demetra.data.DataBlock;
import demetra.design.BuilderPattern;
import demetra.design.Development;
import demetra.regarima.IRegArimaProcessor;
import demetra.regarima.RegArimaEstimation;
import demetra.regarima.RegArimaModel;
import demetra.regarima.ami.IOutliersDetectionModule;
import demetra.regarima.outlier.SingleOutlierDetector;
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
public class RawOutliersDetectionModule implements IOutliersDetectionModule<SarimaModel> {

    public static int DEF_MAXROUND = 100;
    public static int DEF_MAXOUTLIERS = 50;
    public static final double EPS = 1e-5;

    public static SingleOutlierDetector<SarimaModel> defaultOutlierDetector(int period){
        SingleOutlierDetector sod = new ExactSingleOutlierDetector(IRobustStandardDeviationComputer.mad(false),
                IResidualsComputer.mlComputer(),
                new AnsleyFilter());
        sod.addOutlierFactory(AdditiveOutlier.FACTORY);
        sod.addOutlierFactory(LevelShift.FACTORY_ZEROENDED);
        
        sod.addOutlierFactory(new TransitoryChange.Factory(EPS));
        return sod;
    }

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(RawOutliersDetectionModule.class)
    public static class Builder {

        private double cv;
        private IRegArimaProcessor<SarimaModel> processor;
        private int maxOutliers = DEF_MAXOUTLIERS;
        private int maxRound = DEF_MAXROUND;
        private SingleOutlierDetector<SarimaModel> sod;

        private Builder() {
        }

        public Builder detector(SingleOutlierDetector<SarimaModel> sod) {
            this.sod = sod;
            this.sod.clearOutlierFactories();
            return this;
        }


        public Builder criticalValue(double cv) {
            this.cv = cv;
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

        public Builder factory(IOutlier.IOutlierFactory factory) {
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

        public RawOutliersDetectionModule build() {
            return new RawOutliersDetectionModule(sod, cv, processor, maxRound, maxOutliers);
        }
    }

    private final int maxRound, maxOutliers;
    private RegArimaModel<SarimaModel> regarima;
    private final ArrayList<int[]> outliers = new ArrayList<>(); // Outliers : (position, type)
    private final SingleOutlierDetector sod;
    private final IRegArimaProcessor<SarimaModel> processor;
    private final double cv;
    private double[] tstats;
    private int round;

    private RawOutliersDetectionModule(final SingleOutlierDetector sod, final double cv, final IRegArimaProcessor<SarimaModel> processor,
            final int maxOutliers, final int maxRound) {
        this.sod = sod;
        this.cv = cv;
        this.processor = processor;
        this.maxOutliers = maxOutliers;
        this.maxRound = maxRound;
    }

    @Override
    public boolean process(RegArimaModel<SarimaModel> initialModel) {
        try {
            clear();
            regarima = initialModel;
            estimateModel(true);
            return execute();
        } catch (RuntimeException err) {
            return false;
        }
    }

    @Override
    public void prepare(int n) {
        sod.prepare(n);
    }

    @Override
    public void setBounds(int start, int end) {
        sod.setBounds(start, end);
    }

    @Override
    public void exclude(int pos, int type) {
        sod.exclude(pos, type);
    }

    public void exclude(int pos) {
        int n = this.getOutlierFactoriesCount();
        for (int i = 0; i < n; ++i) {
            sod.exclude(pos, i);
        }
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

    public String[] outlierTypes() {
        ArrayList<IOutlier.IOutlierFactory> factories = sod.getFactories();
        String[] types = new String[factories.size()];
        for (int i = 0; i < types.length; ++i) {
            types[i] = factories.get(i).getCode();
        }
        return types;
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
            if (Math.abs(max) > cv) {
                round++;
                int type = sod.getMaxOutlierType();
                int pos = sod.getMaxOutlierPosition();
                addOutlier(pos, type);
                changed = true;
                if (!estimateModel(true)) {
                    outliers.remove(outliers.size() - 1);
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

        while (!verifymodel()) {
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

        RegArimaEstimation<SarimaModel> estimation = full ? processor.process(regarima) : processor.optimize(regarima);
        if (estimation == null) {
            return false;
        }
        regarima = estimation.getModel();
        tstats = estimation.getConcentratedLikelihood().tstats(0, false);
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

    private boolean verifymodel() {
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

        if (Math.abs(tstats[nx0 + imin]) >= cv) {
            return true;
        }
        int[] toremove = outliers.get(imin);
        sod.allow(toremove[0], toremove[1]);
        removeOutlier(imin);
        return false;
    }

}
