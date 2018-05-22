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
import demetra.likelihood.ConcentratedLikelihood;
import demetra.regarima.IRegArimaProcessor;
import demetra.regarima.RegArimaEstimation;
import demetra.regarima.RegArimaModel;
import demetra.regarima.internal.ConcentratedLikelihoodComputer;
import demetra.regarima.outlier.AbstractSingleOutlierDetector;
import demetra.regarima.outlier.FastOutlierDetector;
import demetra.regarima.outlier.CriticalValueComputer;
import demetra.modelling.regression.AdditiveOutlier;
import demetra.modelling.regression.IOutlier.IOutlierFactory;
import demetra.modelling.regression.LevelShift;
import demetra.modelling.regression.PeriodicOutlier;
import demetra.modelling.regression.TransitoryChange;
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
        implements IOutliersDetectionModule<T> {

    public static int DEF_MAXROUND = 100;
    public static int DEF_MAXOUTLIERS = 50;

    @BuilderPattern(OutliersDetectionModule.class)
    public static class Builder<T extends IArimaModel> {

        private AbstractSingleOutlierDetector<T> sod = new FastOutlierDetector<>(null);
        private IRegArimaProcessor<T> processor;
        private int maxOutliers = DEF_MAXOUTLIERS;
        private int maxRound = DEF_MAXROUND;

        public Builder<T> detector(AbstractSingleOutlierDetector<T> sod) {
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

        /**
         *
         * @return
         */
        public Builder<T> setDefault() {
            this.sod.clearOutlierFactories();
            this.sod.addOutlierFactory(AdditiveOutlier.FACTORY);
            this.sod.addOutlierFactory(LevelShift.FACTORY_ZEROENDED);
            return this;
        }

        /**
         *
         * @return
         */
        public Builder<T> setAll() {
            setDefault();
            this.sod.addOutlierFactory(new TransitoryChange.Factory(.7));
            return this;
        }

        /**
         *
         * @param period
         * @return
         */
        public Builder<T> setAll(int period) {
            setAll();
            this.sod.addOutlierFactory(new PeriodicOutlier.Factory(period, true));
            return this;
        }

        public OutliersDetectionModule build() {
            return new OutliersDetectionModule(sod, processor, maxOutliers, maxRound);
        }
    }


    private RegArimaModel<T> regarima; // current regarima model
    private final ArrayList<int[]> outliers = new ArrayList<>(); // Outliers : (position, type)
    private final AbstractSingleOutlierDetector<T> sod;
    private final IRegArimaProcessor<T> processor;
    private final int maxOutliers;
    private final int maxRound;

    private double[] tstats;
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

    private OutliersDetectionModule(final AbstractSingleOutlierDetector sod, final IRegArimaProcessor<T> processor, final int maxOutliers, final int maxRound) {
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
        int n = initialModel.getY().length();
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

            while (!verifyModel()) {
                if (exit) {
                    break;
                }
//                    estimateModel(false);
                ConcentratedLikelihood ce = ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(regarima);
                updateLikelihood(ce);
            }
            if (exit || outliers.size() == maxOutliers) {
                break;
            }
        } while (round < maxRound);

        while (!verifyModel()) {
//                updateLikelihood(regarima_.computeLikelihood());
            estimateModel(false);
        }
    }

    private boolean estimateModel(boolean full) {
        RegArimaEstimation<T> est = full ? processor.process(regarima) : processor.optimize(regarima);
        regarima = est.getModel();
        updateLikelihood(est.getConcentratedLikelihood());
        return true;
    }

    private void updateLikelihood(ConcentratedLikelihood likelihood) {
        tstats = likelihood.tstats(nhp, true);
    }

    private void clear() {
        nhp = 0;
        outliers.clear();
        round = 0;
        lastremoved = null;
        tstats = null;
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
        int[] toremove = outliers.get(imin);
        //sod.allow(toremove[0], toremove[1]);
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
        //
        int opos = regarima.getVariablesCount() - outliers.size() + idx;
        regarima = regarima.toBuilder().removeX(opos).build();
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
