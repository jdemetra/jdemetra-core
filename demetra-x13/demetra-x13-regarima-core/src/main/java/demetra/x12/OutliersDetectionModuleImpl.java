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

import jdplus.data.DataBlock;
import demetra.design.BuilderPattern;
import demetra.design.Development;
import demetra.regarima.IRegArimaProcessor;
import demetra.regarima.RegArimaEstimation;
import demetra.regarima.RegArimaModel;
import demetra.regarima.outlier.SingleOutlierDetector;
import demetra.regarima.outlier.ExactSingleOutlierDetector;
import jdplus.sarima.SarimaModel;
import demetra.modelling.regression.AdditiveOutlierFactory;
import demetra.modelling.regression.IOutlierFactory;
import demetra.modelling.regression.LevelShiftFactory;
import demetra.modelling.regression.TransitoryChangeFactory;
import java.util.ArrayList;
import demetra.regarima.ami.IGenericOutliersDetectionModule;
import demetra.regarima.outlier.RobustStandardDeviationComputer;
import internal.jdplus.arima.AnsleyFilter;
import jdplus.arima.estimation.ResidualsComputer;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
class OutliersDetectionModuleImpl implements IGenericOutliersDetectionModule<SarimaModel> {

    static int DEF_MAXROUND = 100;
    static int DEF_MAXOUTLIERS = 50;
    static final double EPS = 1e-5;

    static SingleOutlierDetector<SarimaModel> defaultOutlierDetector(int period) {
        SingleOutlierDetector sod = ExactSingleOutlierDetector.builder()
                .robustStandardDeviationComputer(RobustStandardDeviationComputer.mad(false))
                .armaFilter(new AnsleyFilter())
                .residualsComputer(ResidualsComputer.mlComputer())
                .build();
        sod.addOutlierFactory(AdditiveOutlierFactory.FACTORY);
        sod.addOutlierFactory(LevelShiftFactory.FACTORY_ZEROENDED);
        sod.addOutlierFactory(new TransitoryChangeFactory(EPS));
        return sod;
    }

    static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(OutliersDetectionModuleImpl.class)
    static class Builder {

        private double cv;
        private IRegArimaProcessor<SarimaModel> processor;
        private int maxOutliers = DEF_MAXOUTLIERS;
        private int maxRound = DEF_MAXROUND;
        private SingleOutlierDetector<SarimaModel> sod;

        private Builder() {
        }

        Builder singleOutlierDetector(SingleOutlierDetector<SarimaModel> sod) {
            this.sod = sod;
            return this;
        }

        Builder criticalValue(double cv) {
            this.cv = cv;
            return this;
        }

        Builder processor(IRegArimaProcessor<SarimaModel> processor) {
            this.processor = processor;
            return this;
        }

        Builder maxOutliers(int max) {
            this.maxOutliers = max;
            return this;
        }

        Builder maxRound(int max) {
            this.maxRound = max;
            return this;
        }

        public OutliersDetectionModuleImpl build() {
            return new OutliersDetectionModuleImpl(sod, cv, processor, maxRound, maxOutliers);
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
    private boolean changed;

    private OutliersDetectionModuleImpl(final SingleOutlierDetector sod, final double cv, final IRegArimaProcessor<SarimaModel> processor,
            final int maxOutliers, final int maxRound) {
        this.sod = sod;
        this.cv = cv;
        this.processor = processor;
        this.maxOutliers = maxOutliers;
        this.maxRound = maxRound;
    }

    @Override
    public boolean process(RegArimaModel<SarimaModel> initialModel) {
        changed = false;
        regarima = initialModel;
        estimateModel(true);
        execute();
        return changed;
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

    public IOutlierFactory getFactory(int i) {
        return sod.getOutlierFactory(i);
    }

    public String[] outlierTypes() {
        ArrayList<IOutlierFactory> factories = sod.getFactories();
        String[] types = new String[factories.size()];
        for (int i = 0; i < types.length; ++i) {
            types[i] = factories.get(i).getCode();
        }
        return types;
    }

    private void addNewOutlier(int pos, int type) {
        int[] o = new int[]{pos, type};
        outliers.add(o);
        double[] xo = new double[regarima.getObservationsCount()];
        DataBlock XO = DataBlock.of(xo);
        sod.factory(type).fill(pos, XO);
        regarima = regarima.toBuilder().addX(XO).build();
        sod.exclude(pos, type);
        changed = true;
    }

    public void addOutlier(int pos, int type) {
        int[] o = new int[]{pos, type};
        outliers.add(o);
        sod.exclude(pos, type);
    }

    /**
     *
     */
    public void clear() {
        outliers.clear();
        regarima = null;
    }

    private void execute() {
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
                addNewOutlier(pos, type);
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
        }
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

    private void removeOutlier(int idx) {
        int opos = regarima.getVariablesCount() - outliers.size() + idx;
        regarima = regarima.toBuilder().removeX(opos).build();
        outliers.remove(idx);
        changed = true;
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
