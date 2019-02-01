/*
 * Copyright 2016 National Bank of Belgium
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
package demetra.regarima.ami;

import demetra.arima.IArimaModel;
import demetra.data.DataBlock;
import demetra.design.BuilderPattern;
import demetra.likelihood.ConcentratedLikelihoodWithMissing;
import demetra.regarima.IRegArimaProcessor;
import demetra.regarima.RegArimaEstimation;
import demetra.regarima.RegArimaModel;
import demetra.regarima.internal.ConcentratedLikelihoodComputer;
import demetra.regarima.outlier.SingleOutlierDetector;
import demetra.regarima.outlier.FastOutlierDetector;
import demetra.regarima.outlier.CriticalValueComputer;
import demetra.modelling.regression.AdditiveOutlierFactory;
import demetra.modelling.regression.IOutlierFactory;
import demetra.modelling.regression.LevelShiftFactory;
import demetra.modelling.regression.PeriodicOutlierFactory;
import demetra.modelling.regression.TransitoryChangeFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 *
 * @author Jean Palate
 * @param <T>
 */
public class OutliersDetectionModule<T extends IArimaModel>
        implements IGenericOutliersDetectionModule<T> {

    public static int DEF_MAXROUND = 100;
    public static int DEF_MAXOUTLIERS = 50;

    @BuilderPattern(OutliersDetectionModule.class)
    public static class Builder<T extends IArimaModel> {

        private SingleOutlierDetector<T> sod = new FastOutlierDetector<>(null);
        private IRegArimaProcessor<T> processor;
        private int maxOutliers = DEF_MAXOUTLIERS;
        private int maxRound = DEF_MAXROUND;

        public Builder<T> detector(SingleOutlierDetector<T> sod) {
            this.sod = sod;
            this.sod.clearOutlierFactories();
            return this;
        }

        public Builder<T> processor(IRegArimaProcessor<T> processor) {
            this.processor = processor;
            return this;
        }

        public Builder<T> maxOutliers(int max) {
            this.maxOutliers = max;
            return this;
        }

        public Builder<T> maxRound(int max) {
            this.maxRound = max;
            return this;
        }

        public Builder<T> addFactory(IOutlierFactory factory) {
            this.sod.addOutlierFactory(factory);
            return this;
        }

        public Builder<T> addFactories(IOutlierFactory[] factories) {
            if (factories != null) {
                for (int i = 0; i < factories.length; ++i) {
                    this.sod.addOutlierFactory(factories[i]);
                }
            }
            return this;
        }

        /**
         *
         * @return
         */
        public Builder<T> setDefault() {
            this.sod.clearOutlierFactories();
            this.sod.addOutlierFactory(AdditiveOutlierFactory.FACTORY);
            this.sod.addOutlierFactory(LevelShiftFactory.FACTORY_ZEROENDED);
            return this;
        }

        /**
         *
         * @return
         */
        public Builder<T> setAll() {
            setDefault();
            this.sod.addOutlierFactory(new TransitoryChangeFactory(.7));
            return this;
        }

        /**
         *
         * @param period
         * @return
         */
        public Builder<T> setAll(int period) {
            setAll();
            this.sod.addOutlierFactory(new PeriodicOutlierFactory(period, true));
            return this;
        }

        public OutliersDetectionModule build() {
            return new OutliersDetectionModule(sod, processor, maxOutliers, maxRound);
        }
    }

    private RegArimaModel<T> regarima; // current regarima model
    private ConcentratedLikelihoodWithMissing cll;
    private final ArrayList<int[]> outliers = new ArrayList<>(); // Outliers : (position, type)
    private final SingleOutlierDetector<T> sod;
    private final IRegArimaProcessor<T> processor;
    private final int maxOutliers;
    private final int maxRound;

    private int nhp;
    private int round;
    // festim = true if the model has to be re-estimated
    private boolean exit;
    private int[] lastremoved;

    private double cv;
    public static final double MINCV = 2.0;
    private Consumer<int[]> addHook;
    private Consumer<int[]> removeHook;

    public static <T extends IArimaModel> Builder<T> build(Class<T> tclass) {
        return new Builder<>();
    }

    private OutliersDetectionModule(final SingleOutlierDetector sod, final IRegArimaProcessor<T> processor, final int maxOutliers, final int maxRound) {
        this.sod = sod;
        this.processor = processor;
        this.maxOutliers = maxOutliers;
        this.maxRound = maxRound;
    }

    /**
     * @return the regarima_
     */
    public RegArimaModel<T> getRegarima() {
        return regarima;
    }

    /**
     * @return the outliers_
     */
    @Override
    public int[][] getOutliers() {
        return outliers.toArray(new int[outliers.size()][]);
    }

    /**
     * @return the addHook
     */
    public Consumer<int[]> getAddHook() {
        return addHook;
    }

    /**
     * @param addHook the addHook to set
     */
    public void setAddHook(Consumer<int[]> addHook) {
        this.addHook = addHook;
    }

    /**
     * @return the removeHook
     */
    public Consumer<int[]> getRemoveHook() {
        return removeHook;
    }

    /**
     * @param removeHook the removeHook to set
     */
    public void setRemoveHook(Consumer<int[]> removeHook) {
        this.removeHook = removeHook;
    }

    @Override
    public boolean process(RegArimaModel<T> initialModel) {
        clear();
        regarima = initialModel;
        nhp = 0;
        if (!estimateModel(true)) {
            return false;
        }
        try {
            calc();
            return true;
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

    private void calc() {
        double max;
        exit = false;
        do {
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
            addOutlier(pos, type);
            estimateModel(false);
            if (!verifyModel()) {
                cll = ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(regarima);
                if (exit) {
                    break;
                }
            }
        } while (round <= maxRound && outliers.size() <= maxOutliers);

        while (!verifyModel()) {
            cll = ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(regarima);
//            estimateModel(false);
        }
    }

    private boolean estimateModel(boolean full) {
        RegArimaEstimation<T> est = full ? processor.process(regarima) : processor.optimize(regarima);
        regarima = est.getModel();
        cll = est.getConcentratedLikelihood();
        return true;
    }

    private void clear() {
        cll = null;
        regarima = null;
        nhp = 0;
        outliers.clear();
        round = 0;
        lastremoved = null;
        // festim = true if the model has to be re-estimated
    }

    /**
     * Backward procedure (without re-estimation of the model)
     *
     * @param exit
     * @return True means that the model was not modified
     */
    private boolean verifyModel() {
        if (outliers.isEmpty()) {
            return true;
        }

        int nx0 = regarima.getVariablesCount() - outliers.size();
        // nx0 is the position of the first outlier in the list.
        int imin = 0;
        double tmin = Math.abs(cll.tstat(nx0, nhp, true));
        for (int i = 1; i < outliers.size(); ++i) {
            double tcur = Math.abs(cll.tstat(i + nx0, nhp, true));
            if (tcur < tmin) {
                imin = i;
                tmin = tcur;
            }
        }

        if (tmin >= cv) {
            return true;
        }
        int[] toremove = outliers.get(imin);
        sod.allow(toremove[0], toremove[1]);
        removeOutlier(imin);
        if (removeHook != null) {
            removeHook.accept(toremove);
        }
        if (lastremoved != null) {
            if (Arrays.equals(toremove, lastremoved)) {
                exit = true;
            }
        }
        lastremoved = toremove;
        return false;
    }

    private void addOutlier(int pos, int type) {
        int[] o = new int[]{pos, type};
        outliers.add(o);
        double[] xo = new double[regarima.getObservationsCount()];
        DataBlock XO = DataBlock.ofInternal(xo);
        sod.factory(type).fill(pos, XO);
        regarima = regarima.toBuilder().addX(XO).build();
        cll = null;
        sod.exclude(pos, type);
        if (addHook != null) {
            addHook.accept(o);
        }
    }

    /**
     *
     * @param model
     * @return
     */
    private void removeOutlier(int idx) {
        // position of the outlier in the regression variables
        int opos = regarima.getX().size() - outliers.size() + idx;
        regarima = regarima.toBuilder().removeX(opos).build();
        cll = null;

        outliers.remove(idx);
    }

    /**
     *
     * @param fac
     * @return
     */
    public List<IOutlierFactory> factories() {
        return Collections.unmodifiableList(sod.getFactories());
    }

    public void setCriticalValue(double value) {
        cv = value;
    }

    public double getCritivalValue() {
        return cv;
    }

    public static double calcCv(int nobs) {
        return Math.max(CriticalValueComputer.simpleComputer().applyAsDouble(nobs), MINCV);
    }

    @Override
    public void exclude(int pos, int type) {
        sod.exclude(pos, type);
    }

    public IOutlierFactory getFactory(int i) {
        return sod.getOutlierFactory(i);
    }

}
