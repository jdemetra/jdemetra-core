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
import demetra.likelihood.ConcentratedLikelihood;
import demetra.regarima.IRegArimaProcessor;
import demetra.regarima.RegArimaEstimation;
import demetra.regarima.RegArimaModel;
import demetra.regarima.internal.ConcentratedLikelihoodComputer;
import demetra.regarima.internal.ConcentratedLikelihoodEstimation;
import demetra.regarima.outlier.AbstractSingleOutlierDetector;
import demetra.regarima.outlier.FastOutlierDetector;
import demetra.regarima.outlier.CriticalValueComputer;
import demetra.timeseries.regression.AdditiveOutlier;
import demetra.timeseries.regression.IOutlier.IOutlierFactory;
import demetra.timeseries.regression.LevelShift;
import demetra.timeseries.regression.PeriodicOutlier;
import demetra.timeseries.regression.TransitoryChange;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 *
 * @author Jean Palate
 * @param <T>
 */
public class OutliersDetectionModule<T extends IArimaModel> {

    public static int MAXROUND = 100;
    public static int MAXOUTLIERS = 50;

    public static enum Estimation {

        Approximate,
        ExactIterative,
        ExactFixed
    }

    private RegArimaModel<T> regarima; // current regarima model
    private final ArrayList<int[]> outliers = new ArrayList<>(); // Outliers : (position, type)
    private final AbstractSingleOutlierDetector sod;
    private double[] tstats;
    private int nhp;
    private int round;
    // festim = true if the model has to be re-estimated
    private boolean exit;
    private int[] lastremoved;
    private IRegArimaProcessor<T> processor;

    private Estimation estimation = Estimation.ExactIterative;
    private int selectivity;
    private double cv, curcv;
    private double pc = 0.12;
    public static final double MINCV = 2.0;
    private Consumer<int[]> addHook;
    private Consumer<int[]> removeHook;

    /**
     * @return the estimation
     */
    public Estimation getEstimation() {
        return estimation;
    }

    /**
     * @param estimation the estimation to set
     */
    public void setEstimation(Estimation estimation) {
        this.estimation = estimation;
    }

    /**
     * @return the monitor
     */
    public IRegArimaProcessor<T> getMonitor() {
        return processor;
    }

    /**
     * @param monitor the monitor to set
     */
    public void setProcessor(IRegArimaProcessor<T> monitor) {
        this.processor = monitor;
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

    public OutliersDetectionModule(AbstractSingleOutlierDetector sod) {
        this.sod = sod == null ? new FastOutlierDetector(null) : sod;
    }

    public boolean process(RegArimaModel<T> initialModel) {
        clear();
        int n = initialModel.getY().length();
        sod.prepare(n);
        sod.setBounds(0, n);
        regarima = initialModel;
        if (curcv == 0) {
            curcv = calcCv();
        }
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

    private void calc() {
        double max;
        exit = false;
        do {
            if (!sod.process(regarima)) {
                break;
            }
            round++;
            max = sod.getMaxTStat();
            if (Math.abs(max) < curcv) {
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
                ConcentratedLikelihoodEstimation ce = ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(regarima);
                updateLikelihood(ce.getLikelihood());
            }
            if (exit || outliers.size() == MAXOUTLIERS) {
                break;
            }
        } while (round < MAXROUND);

        while (!verifyModel()) {
//                updateLikelihood(regarima_.computeLikelihood());
            estimateModel(false);
        }
    }

    private boolean estimateModel(boolean full) {
        RegArimaEstimation<T> est = full ? processor.process(regarima) : processor.optimize(regarima);
        regarima = est.getModel();
        updateLikelihood(est.getConcentratedLikelihood().getLikelihood());
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
        curcv = 0;
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

        if (Math.abs(tstats[nx0 + imin]) >= curcv) {
            return true;
        }
        int[] toremove = outliers.get(imin);
        sod.allow(toremove[0], toremove[1]);
        removeOutlier(imin);
        if (removeHook != null) {
            removeHook.accept(toremove);
        }
        if (lastremoved != null) {
            if (toremove.equals(lastremoved)) {
                exit = true;
            }
        }
        lastremoved = toremove;
        return false;
    }

    private void addOutlier(int pos, int type) {
        int[] o=new int[]{pos, type};
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
        regarima=regarima.toBuilder().removeX(opos).build();
        outliers.remove(idx);
    }

    /**
     *
     * @param o
     */
    public void addOutlierFactory(IOutlierFactory o) {
        sod.addOutlierFactory(o);
    }

    /**
     *
     */
    public void clearOutlierFactories() {
        sod.clearOutlierFactories();
    }

    /**
     *
     */
    public void setDefault() {
        clear();
        clearOutlierFactories();
        addOutlierFactory(AdditiveOutlier.FACTORY);
        addOutlierFactory(LevelShift.FACTORY_ZEROENDED);
        curcv = 0;
    }
    /**
     *
     */
    public void setAll() {
        setDefault();
        addOutlierFactory(new TransitoryChange.Factory(.7));
    }

    /**
     *
     * @param period
     */
    public void setAll(int period) {
        setAll();
        addOutlierFactory(new PeriodicOutlier.Factory(period, true));
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
     * @param fac
     * @return
     */
    public IOutlierFactory getFactory(int fac) {
        return sod.factory(fac);
    }

    public void setCriticalValue(double value) {
        cv = value;
    }

    public double getCritivalValue() {
        return cv;
    }

    public double getPc() {
        return pc;
    }

    public void setPc(double pc) {
        this.pc = pc;
    }

    private double calcCv() {
        double cv = this.cv;
        if (cv == 0) {
            cv = CriticalValueComputer.simpleComputer().applyAsDouble(regarima.getObservationsCount());
        }
        for (int i = 0; i < -selectivity; ++i) {
            cv *= (1 - pc);
        }
        return Math.max(cv, MINCV);
    }

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

    public void setSelectivity(int level) {
        if (selectivity != level) {
            selectivity = level;
            curcv = 0;
        }
    }

    public int getSelectivity() {
        return selectivity;
    }

}
