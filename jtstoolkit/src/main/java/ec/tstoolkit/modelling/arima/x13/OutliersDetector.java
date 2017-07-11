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
package ec.tstoolkit.modelling.arima.x13;

import ec.tstoolkit.arima.estimation.AnsleyFilter;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.dstats.Normal;
import ec.tstoolkit.dstats.ProbabilityType;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.maths.matrices.Householder;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.maths.realfunctions.ProxyMinimizer;
import ec.tstoolkit.maths.realfunctions.levmar.LevenbergMarquardtMethod;
import ec.tstoolkit.modelling.IRobustStandardDeviationComputer;
import ec.tstoolkit.modelling.arima.AbstractSingleOutlierDetector;
import ec.tstoolkit.modelling.arima.ExactSingleOutlierDetector;
import ec.tstoolkit.modelling.arima.IOutliersDetectionModule;
import ec.tstoolkit.modelling.arima.IResidualsComputer;
import ec.tstoolkit.modelling.arima.ModelDescription;
import ec.tstoolkit.modelling.arima.ModelEstimation;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.PreprocessingDictionary;
import ec.tstoolkit.modelling.arima.ProcessingResult;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.estimation.GlsSarimaMonitor;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.regression.AdditiveOutlierFactory;
import ec.tstoolkit.timeseries.regression.IOutlierFactory;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import ec.tstoolkit.timeseries.regression.LevelShiftFactory;
import ec.tstoolkit.timeseries.regression.SeasonalOutlierFactory;
import ec.tstoolkit.timeseries.regression.TransitoryChangeFactory;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.ArrayList;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class OutliersDetector implements IOutliersDetectionModule {

    public static class CriticalValueComputer implements ICriticalValueComputer {

        private final double eps;

        public CriticalValueComputer() {
            eps = 0.05;
        }

        public CriticalValueComputer(final double eps) {
            this.eps = eps;
        }

        private double calcVAL(int nvals) {
            if (nvals == 1) {
                return 1.96; // normal distribution
            }
            double n = nvals;
            double pmod = 2 - Math.sqrt(1 + eps);
            double acv = Math.sqrt(2 * Math.log(n));
            double bcv = acv - (Math.log(Math.log(n)) + Math.log(4 * Math.PI))
                    / (2 * acv);
            double xcv = -Math.log(-.5 * Math.log(pmod));
            return xcv / acv + bcv;
        }

        @Override
        public double compute(int nvals) {
            Normal normal = new Normal();
            if (nvals == 1) {
                return normal.getProbabilityInverse(eps / 2,
                        ProbabilityType.Upper);
            }
            double n = nvals;
            double[] y = new double[3];
            int[] x = new int[]{2, 100, 200};
            Matrix X = new Matrix(3, 3);

            for (int i = 0; i < 3; ++i) {
                X.set(i, 0, 1);
                X.set(i, 2, Math.sqrt(2 * Math.log(x[i])));
                X.set(i, 1, (Math.log(Math.log(x[i])) + Math.log(4 * Math.PI))
                        / (2 * X.get(i, 2)));
            }

            y[0] = normal.getProbabilityInverse((1 + Math.sqrt(1 - eps)) / 2,
                    ProbabilityType.Lower);
            for (int i = 1; i < 3; ++i) {
                y[i] = calcVAL(x[i]);
            }
            // solve X b = y
            Householder qr = new Householder(false);
            qr.decompose(X);
            double[] b = qr.solve(y);

            double acv = Math.sqrt(2 * Math.log(n));
            double bcv = (Math.log(Math.log(n)) + Math.log(4 * Math.PI))
                    / (2 * acv);
            return b[0] + b[1] * bcv + b[2] * acv;

        }
    }

    private static final int MAX_OUTLIERS = 30, MAX_ITER = 30;
    private final GlsSarimaMonitor monitor;

    public double getEpsilon() {
        return monitor.getPrecision();
    }

    public void setEpsilon(double val) {
        monitor.setPrecision(val);
    }

    public void setSpan(TsPeriodSelector span) {
        span_ = span;
    }

    public TsPeriodSelector getSpan() {
        return span_;
    }

    @Override
    public ProcessingResult process(ModellingContext context) {
        try {
            clear();
            if (curcv_ == 0) {
                curcv_ = calcCv(context);
            }
            llcorr_ = context.description.getLikelihoodCorrection();
            mapping_ = X13Preprocessor.createDefaultMapping(context.description);
            if (context.estimation == null) {
                regarima_ = context.description.buildRegArima();
                if (!estimateModel()) {
                    return ProcessingResult.Failed;
                }
            } else {
                estimation_ = context.estimation;
                regarima_ = context.estimation.getRegArima();
            }
            TsDomain edomain = context.description.getEstimationDomain();
            sod_.prepare(edomain, span_ == null ? null : edomain.select(span_));
            // exclude missing and previous outliers
            sod_.exclude(context.description.getMissingValues());
            sod_.exclude(context.description.getOutliersPosition(true));
            sod_.exclude(context.description.getOutliersPosition(false));
            sod_.exclude(context.description.getFixedOutliersPosition());
            outliers_.addAll(context.description.getOutliers());

            boolean changed = execute();
            context.description.setOutliers(outliers_);
            context.estimation = estimation_;
            addInfo(context.description, context.information);
            if (changed) {
                return ProcessingResult.Changed;
            } else {
                return ProcessingResult.Unchanged;
            }
        } catch (RuntimeException err) {
            return ProcessingResult.Failed;
        }
    }
    private AbstractSingleOutlierDetector<SarimaModel> sod_;
    private RegArimaModel<SarimaModel> regarima_;
    private double llcorr_;
    private IParametricMapping<SarimaModel> mapping_;
    private ModelEstimation estimation_;
    private ArrayList<IOutlierVariable> outliers_ = new ArrayList<>();
    private int maxiter_ = MAX_ITER;
    private int m_round;
    private int selectivity_;
    private double cv_, curcv_;
    private double pc_ = 0.14286;
    private TsPeriodSelector span_;
    public static final double MINCV = 2.8;

    /**
     *
     */
    public OutliersDetector() {
        sod_ = new ExactSingleOutlierDetector(IRobustStandardDeviationComputer.mad(false), IResidualsComputer.mlComputer(), new AnsleyFilter());
        monitor = new GlsSarimaMonitor();
        monitor.setMinimizer(new ProxyMinimizer(new LevenbergMarquardtMethod()));
        monitor.useLogLikelihood(false);
    }

    /**
     *
     * @param sod
     */
    public OutliersDetector(AbstractSingleOutlierDetector<SarimaModel> sod) {
        sod_ = sod;
        monitor = new GlsSarimaMonitor();
        monitor.setMinimizer(new ProxyMinimizer(new LevenbergMarquardtMethod()));
        monitor.useLogLikelihood(false);
    }

    private void addOutlier(IOutlierVariable o) {
        outliers_.add(o);
        double[] xo = new double[regarima_.getObsCount()];
        DataBlock XO = new DataBlock(xo);
        o.data(sod_.getDomain().getStart(), XO);
        regarima_.addX(XO);
        sod_.exclude(o);
    }

    /**
     *
     * @param o
     */
    public void addOutlierFactory(IOutlierFactory o) {
        sod_.addOutlierFactory(o);
    }

    /**
     *
     */
    public void clear() {
        outliers_.clear();
        regarima_ = null;
        estimation_ = null;
        llcorr_ = 0;
        mapping_ = null;
    }

    /**
     *
     */
    public void clearOutlierFactories() {
        sod_.clearOutlierFactories();
    }

    private boolean execute() {
        boolean changed = false;
        double max;
        m_round = 0;

        do {
            if (!sod_.process(regarima_)) {
                break;
            }
            max = sod_.getMaxTStat();
            if (Math.abs(max) > curcv_) {
                m_round++;
                IOutlierVariable o = sod_.getMaxOutlier();
                addOutlier(o);
                changed = true;
                if (!estimateModel()) {
                    outliers_.remove(o);
                    estimateModel();
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
        } while (m_round < maxiter_ && outliers_.size() < MAX_OUTLIERS);

        while (verifymodel(curcv_) == 0) {
            if (!estimateModel()) {
                break;
            }
            changed = true;
        }

        return changed;
    }

    public void setCriticalValue(double value) {
        cv_ = value;
    }

    public double getCritivalValue() {
        return cv_;
    }

    public double getPc() {
        return pc_;
    }

    public void setPc(double pc) {
        pc_ = pc;
    }

    public int getMaxIter() {
        return maxiter_;
    }

    public void setMaxIter(int maxiter) {
        maxiter_ = maxiter;
    }

    /**
     *
     * @return
     */
    public RegArimaModel<SarimaModel> getModel() {
        return regarima_;
    }

    /**
     *
     * @return
     */
    public int getOutlierFactoriesCount() {
        return sod_.getOutlierFactoriesCount();
    }

    /**
     *
     * @return
     */
    public int getOutliersCount() {
        return outliers_.size();
    }

    private boolean estimateModel() {
        estimation_ = new ModelEstimation(regarima_, llcorr_);
        monitor.setMapping(mapping_);
        if (!estimation_.compute(monitor, mapping_.getDim())) {
            return false;
        }
        regarima_ = estimation_.getRegArima();
        return true;
    }

    /**
     *
     * @return
     */
    public boolean isInitialized() {
        return regarima_ != null;
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
        int opos = regarima_.getXCount() - outliers_.size() + idx;
        regarima_.removeX(opos);
        outliers_.remove(idx);
    }

    /**
     *
     */
    public void setAll() {
        clear();
        clearOutlierFactories();
        addOutlierFactory(new AdditiveOutlierFactory());
        LevelShiftFactory lfac = new LevelShiftFactory();
        lfac.setZeroEnded(true);
        addOutlierFactory(lfac);
        TransitoryChangeFactory tfac = new TransitoryChangeFactory();
        tfac.setMonthlyCoefficient(true);
        addOutlierFactory(tfac);
        SeasonalOutlierFactory sfac = new SeasonalOutlierFactory();
        sfac.setZeroEnded(true);
        addOutlierFactory(sfac);
    }

    /**
     *
     */
    public void setDefault() {
        clear();
        clearOutlierFactories();
        addOutlierFactory(new AdditiveOutlierFactory());
        LevelShiftFactory lfac = new LevelShiftFactory();
        lfac.setZeroEnded(true);
        addOutlierFactory(lfac);
        TransitoryChangeFactory tfac = new TransitoryChangeFactory();
        tfac.setMonthlyCoefficient(true);
        addOutlierFactory(tfac);
//        SeasonalOutlierFactory sfac = new SeasonalOutlierFactory();
//        sfac.setZeroEnded(true);
//        addOutlierFactory(sfac);
        curcv_ = 0;
    }

    private int verifymodel(double cv) {
        if (regarima_ == null) {
            return 1;
        }
        if (outliers_.isEmpty()) {
            return 1; // no outlier detected
        }
        int imin = 0;
        double[] t = estimation_.getLikelihood().getTStats(false, 0);
        int nx0 = regarima_.getVarsCount() - outliers_.size();

        for (int i = 1; i < outliers_.size(); ++i) {
            if (Math.abs(t[i + nx0]) < Math.abs(t[imin + nx0])) {
                imin = i;
            }
        }

        if (Math.abs(t[nx0 + imin]) >= cv) {
            return 1;
        }
        removeOutlier(imin);
        return 0;
    }

    private void addInfo(ModelDescription desc, InformationSet information) {
        InformationSet subset = information.subSet(PreprocessingDictionary.OUTLIERS);
        subset.set("count", desc.getOutliers().size());
    }

    @Override
    public boolean reduceSelectivity() {
        if (curcv_ == 0) {
            return false;
        }
        --selectivity_;
        if (curcv_ == MINCV) {
            return false;
        }
        curcv_ = Math.max(MINCV, curcv_ * (1 - pc_));
        return true;
    }

    @Override
    public void setSelectivity(int level) {
        if (selectivity_ != level) {
            selectivity_ = level;
            curcv_ = 0;
        }
    }

    @Override
    public int getSelectivity() {
        return selectivity_;
    }

    private double calcCv(ModellingContext context) {
        double cv = cv_;
        if (cv == 0) {
            cv = new CriticalValueComputer().compute(context.description.getEstimationDomain().getLength());
        }
        for (int i = 0; i < -selectivity_; ++i) {
            cv *= (1 - pc_);
        }
        return Math.max(cv, MINCV);
    }

}
