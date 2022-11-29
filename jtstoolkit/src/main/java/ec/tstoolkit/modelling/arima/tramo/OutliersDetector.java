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
package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.eco.Ols;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.maths.realfunctions.IFunctionMinimizer;
import ec.tstoolkit.maths.realfunctions.ProxyMinimizer;
import ec.tstoolkit.maths.realfunctions.levmar.LevenbergMarquardtMethod;
import ec.tstoolkit.modelling.arima.IOutliersDetectionModule;
import ec.tstoolkit.modelling.arima.ModelDescription;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.PreprocessingDictionary;
import ec.tstoolkit.modelling.arima.ProcessingResult;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.sarima.SarmaSpecification;
import ec.tstoolkit.sarima.estimation.GlsSarimaMonitor;
import ec.tstoolkit.sarima.estimation.HannanRissanen;
import ec.tstoolkit.sarima.estimation.SarimaMapping;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.regression.AdditiveOutlierFactory;
import ec.tstoolkit.timeseries.regression.IOutlierFactory;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import ec.tstoolkit.timeseries.regression.LevelShiftFactory;
import ec.tstoolkit.timeseries.regression.OutlierDefinition;
import ec.tstoolkit.timeseries.regression.SeasonalOutlierFactory;
import ec.tstoolkit.timeseries.regression.TransitoryChangeFactory;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class OutliersDetector implements IOutliersDetectionModule {

    private static final double EPS = 1e-5;
    private static final int MAXROUND = 50, MAXOUTLIERS = 30;
    private RegArimaModel<SarimaModel> regarima_;
    private final ArrayList<IOutlierVariable> outliers_ = new ArrayList<>();
    private final SingleOutlierDetector sod_ = new SingleOutlierDetector();
    private double[] coeff_, tstats_;
    private int nhp_;
    private int round_;
    // festim = true if the model has to be re-estimated
    private boolean rflag_, backw_, exit_, festim_;
    private IOutlierVariable lastremoved_;
    private DataBlock res_;
    private TsPeriodSelector span_;
    //
    private boolean mvx_, cmvx_;
    private int selectivity_;
    private double cv_, curcv_;
    private double pc_ = 0.12;
    public static final double MINCV = 2.0;

    @Override
    public ProcessingResult process(ModellingContext context) {
        clear();
        List<OutlierDefinition> initial = OutlierDefinition.of(context.description.getOutliers());
        if (curcv_ == 0) {
            curcv_ = calcCv(context);
        }
        addVaInfo(context, curcv_);
        int test = comatip(context.description);
        if (test < 0) {
            return ProcessingResult.Unprocessed;
        } else if (test > 0) {
            cmvx_ = true;
        } else {
            cmvx_ = false;
        }
        TsDomain edomain = context.description.getEstimationDomain();
        sod_.prepare(edomain, span_ == null ? null : edomain.select(span_));
        // exclude missing and previous outliers
        sod_.exclude(context.description.getMissingValues());
        sod_.exclude(context.description.getOutliersPosition(true));
        sod_.exclude(context.description.getOutliersPosition(false));
        sod_.exclude(context.description.getFixedOutliersPosition());

        outliers_.addAll(context.description.getOutliers());

        regarima_ = context.description.buildRegArima();
        nhp_ = regarima_.getArima().getParametersCount();
        double max = 0;
        try {
            do {
                if (!estimateModel()) {
                    return ProcessingResult.Failed;
                }
                boolean search = true;
                if (backw_) {
                    search = verifyModel(context);
                    if (exit_) {
                        break;
                    }
                }
                if (search) {
                    if (!sod_.process(regarima_.getArima(), res_)) {
                        break;
                    }
                    round_++;
                    max = sod_.getMaxTStat();
                    if (Math.abs(max) < curcv_) {
                        break;
                    }
                    IOutlierVariable o = sod_.getMaxOutlier();
                    boolean bok = true;
                    for (int i = 0; i < outliers_.size(); ++i) {
                        if (o.getPosition().equals(outliers_.get(i).getPosition())) {
                            bok = false;
                            break;
                        }
                    }
                    if (bok) {
                        //estim = true;
                        addOutlier(o, sod_.getMaxCoefficient());
                        addOutlierInfo(context, o, max);
                        if (outliers_.size() == MAXOUTLIERS) {
                            break;
                        }
                    } else {
                        break;// no outliers to remove...
                    }
                }
            } while (round_ < MAXROUND);

            // we should remove non signigicant outlier (witouht re-estimation)
            if (exit_ || round_ == MAXROUND || outliers_.size() == MAXOUTLIERS) {
                updateLikelihood(regarima_.computeLikelihood());
            }
            festim_ = false;

            while (!verifyModel(context)) {
                updateLikelihood(regarima_.computeLikelihood());
            }
            if (!ec.tstoolkit.utilities.Comparator.equals(initial, OutlierDefinition.of(outliers_))) {
                context.description.setOutliers(outliers_);
                addInfo(context.description, context.information);
                context.estimation = null;
                return ProcessingResult.Changed;
            } else {
                return ProcessingResult.Unchanged;
            }
        } catch (RuntimeException err) {
            return ProcessingResult.Failed;
        }
    }

    private GlsSarimaMonitor makeMonitor() {
        //IFunctionMinimizer minimizer = new ProxyMinimizer(new TramoMarquardt());
        IFunctionMinimizer minimizer = new ProxyMinimizer(new LevenbergMarquardtMethod());
        GlsSarimaMonitor monitor = new GlsSarimaMonitor();
        monitor.setMinimizer(minimizer);
        monitor.setPrecision(EPS);
        return monitor;
    }

    private boolean estimateModel() {
        // step 1 Initial values by OLS
        SarimaModel sarima = regarima_.getArima().clone();
        SarimaSpecification spec = sarima.getSpecification();
        if (rflag_) {
            if (regarima_.getDModel().getVarsCount() > 0) {
                Ols ols = new Ols();
                if (!ols.process(regarima_.getDModel())) {
                    return false;
                }
                res_ = ols.getResiduals();
            } else {
                res_ = regarima_.getDModel().getY().deepClone();
            }

        } else if (coeff_ != null) {
            res_ = regarima_.getDModel().calcRes(new DataBlock(coeff_));
        } else {
            res_ = regarima_.getDModel().getY();
        }
        boolean stable = true;
        rflag_ = false;

        if (festim_) {
            SarmaSpecification dspec = spec.doStationary();
            if (spec.getParametersCount() != 0) {
                HannanRissanen hr = new HannanRissanen();
                if (hr.process(res_, dspec)) {
                    SarimaModel stmodel = hr.getModel();
                    stable = !SarimaMapping.stabilize(stmodel);
                    if (stable || cmvx_ || round_ == 0) {
                        sarima.setParameters(stmodel.getParameters());
                        regarima_.setArima(sarima);
                    } else {
                        rflag_ = true;
                        stable = true;
                    }
                }
            }
            if ((cmvx_ || !stable) && festim_) {
                if (!optimizeModel()) {
                    return false;
                } else {
                    return true;
                }
            }
        }
        if (regarima_.getVarsCount() > 0) {
            updateLikelihood(regarima_.computeLikelihood());
        }

        return true;
    }

    private boolean optimizeModel() {
        GlsSarimaMonitor monitor = makeMonitor();
        RegArimaEstimation<SarimaModel> estimation = monitor.optimize(regarima_);
        if (!monitor.hasConverged()) {
            int iter = 0;
            monitor.useMaximumLikelihood(false);
            double tol = EPS;
            do {
                tol *= 10;
                monitor.setPrecision(tol);
                estimation = monitor.optimize(regarima_);
                if (monitor.hasConverged()) {
                    break;
                }
            } while (iter++ < 3);
            if (iter == 3) {
                return false;
            }
        }
        regarima_ = estimation.model;
        updateLikelihood(estimation.likelihood);
        return true;
    }

    private void updateLikelihood(ConcentratedLikelihood likelihood) {
        coeff_ = likelihood.getB();
        tstats_ = likelihood.getTStats(true, nhp_);
        res_ = regarima_.getDModel().calcRes(new DataBlock(coeff_));
    }

    private void clear() {
        rflag_ = true;
        nhp_ = 0;
        outliers_.clear();
        festim_ = true;
        backw_ = false;
        exit_ = false;
        round_ = 0;
        lastremoved_ = null;
        res_ = null;
        coeff_ = null;
        tstats_ = null;
        curcv_ = 0;
        // festim = true if the model has to be re-estimated
    }

    /**
     * Backward procedure (without re-estimation of the model)
     *
     * @param exit
     * @return True means that the model was not modified
     */
    private boolean verifyModel(ModellingContext context) {
        festim_ = true;
        if (outliers_.isEmpty()) {
            return true;
        }
        /*double[] t = m_model.computeLikelihood().getTStats(true,
         m_model.getArma().getParametersCount());*/
        int nx0 = regarima_.getVarsCount() - outliers_.size();
        int imin = 0;
        for (int i = 1; i < outliers_.size(); ++i) {
            if (Math.abs(tstats_[i + nx0]) < Math.abs(tstats_[imin + nx0])) {
                imin = i;
            }
        }

        if (Math.abs(tstats_[nx0 + imin]) >= curcv_) {
            return true;
        }
        backw_ = false;
        festim_ = false;
        IOutlierVariable toremove = outliers_.get(imin);
        sod_.allow(toremove);
        removeOutlier(imin);
        removeOutlierInfo(context, toremove);
        if (lastremoved_ != null) {
            if (toremove.getPosition().equals(lastremoved_.getPosition())
                    && toremove.getOutlierType() == lastremoved_.getOutlierType()) {
                exit_ = true;
            }
        }
        lastremoved_ = toremove;
        return false;
    }

    private void addOutlier(IOutlierVariable o) {
        outliers_.add(o);
        double[] xo = new double[regarima_.getObsCount()];
        DataBlock XO = new DataBlock(xo);
        o.data(sod_.getDomain().getStart(), XO);
        regarima_.addX(XO);
        sod_.exclude(o);
    }

    private void addOutlier(IOutlierVariable o, double c) {
        addOutlier(o);
        double[] tmp;
        if (coeff_ == null) {
            coeff_ = new double[]{c};
        } else {
            tmp = new double[coeff_.length + 1];
            for (int i = 0; i < coeff_.length; ++i) {
                tmp[i] = coeff_[i];
            }
            tmp[coeff_.length] = c;
            coeff_ = tmp;
        }
        backw_ = true;
    }

    /**
     *
     * @param model
     * @return
     */
    private void removeOutlier(int idx) {
        //
        int opos = regarima_.getXCount() - outliers_.size() + idx;
        regarima_.removeX(opos);
        outliers_.remove(idx);
        double[] tmp;
        if (coeff_.length == 1) {
            coeff_ = null;
        } else {
            if (regarima_.isMeanCorrection()) {
                ++opos;
            }
            tmp = new double[coeff_.length - 1];
            for (int i = 0; i < opos; ++i) {
                tmp[i] = coeff_[i];
            }
            for (int i = opos + 1; i < coeff_.length; ++i) {
                tmp[i - 1] = coeff_[i];
            }
            coeff_ = tmp;
        }
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
    public void clearOutlierFactories() {
        sod_.clearOutlierFactories();
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
        addOutlierFactory(new TransitoryChangeFactory());
        SeasonalOutlierFactory sfac = new SeasonalOutlierFactory();
        sfac.setZeroEnded(true);
        addOutlierFactory(sfac);
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
     */
    public void setDefault() {
        clear();
        clearOutlierFactories();
        addOutlierFactory(new AdditiveOutlierFactory());
        addOutlierFactory(new LevelShiftFactory());
        addOutlierFactory(new TransitoryChangeFactory());
        curcv_ = 0;
    }

    /**
     *
     * @return
     */
    public boolean isEML() {
        return mvx_;
    }

    public boolean hasUsedEML() {
        return cmvx_;
    }

    /**
     *
     * @param value
     */
    public void useEML(boolean value) {
        mvx_ = value;
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

    private void addInfo(ModelDescription desc, InformationSet information) {
        InformationSet subset = information.subSet(PreprocessingDictionary.OUTLIERS);
        subset.set("count", desc.getOutliers().size());
    }

    private int comatip(ModelDescription desc) {
        // int rslt = ml ? 1 : 0;
        int n = desc.getY().length;
        if (desc.getMissingValues() != null) {
            n -= desc.getMissingValues().length;
        }
        // first, check if od is possible
        SarimaSpecification spec = desc.getSpecification().clone();
        int nparm = Math.max(spec.getD() + spec.getP() + spec.getFrequency()
                * (spec.getBD() + spec.getBP()), spec.getQ()
                + spec.getFrequency() * spec.getBQ())
                + (desc.isEstimatedMean() ? 1 : 0)
                + (15 * n) / 100 + spec.getFrequency();
        if (n - nparm <= 0) {
            return -1;
        }
        if (mvx_) {
            return 1;
        }
        int ndf1 = TramoProcessor.autlar(n, spec);
        int ndf2 = 0;
        if (spec.getP() + spec.getBP() > 0 && spec.getQ() + spec.getBQ() > 0) {
            n -= spec.getP() + spec.getFrequency() * spec.getBP();
            spec.setP(0);
            spec.setBP(0);
            ndf2 = TramoProcessor.autlar(n, spec);
        }
        if (ndf1 < 0 || ndf2 < 0) {
            return 1;
        } else {
            return 0;
        }
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

    /**
     *
     * @param n
     * @return
     */
    @Deprecated
    public static double calcDefaultCriticalValue(int n) {
        double cv = 0;
//        if (n < 50) {
//            cv = 3;
//        } else if (n < 450) {
//            cv = 3 + 0.0025 * (n - 50);
//        } else {
//            cv = 4;
//        }
        if (n <= 50) {
            cv = 3.3;
        } else if (n < 450) {
            cv = 3.3 + 0.0025 * (n - 50);
        } else {
            cv = 4.3;
        }
        return cv;
    }

    private double calcCv(ModellingContext context) {
        double cv = cv_;
        if (cv == 0) {
            cv = ICriticalValueComputer.defaultComputer().compute(context.description.getY().length);
            //cv=calcDefaultCriticalValue(context.description.getY().length);
        }
        for (int i = 0; i < -selectivity_; ++i) {
            cv *= (1 - pc_);
        }
        return Math.max(cv, MINCV);
    }

    public void setSpan(TsPeriodSelector span) {
        span_ = span;
    }

    public TsPeriodSelector getSpan() {
        return span_;
    }

    private void addOutlierInfo(ModellingContext context, IOutlierVariable var, double t) {
//        if (context.processingLog != null) {
//            StringBuilder builder = new StringBuilder();
//            builder.append(OUT_ADD).append(": ").append(var).append(" (T=").append((t)).append(')');
//                context.processingLog.add(ProcessingInformation.info(OUTLIERS,
//                    OutliersDetector.class.getName(), builder.toString(), null));
//        }
    }

    private void removeOutlierInfo(ModellingContext context, IOutlierVariable var) {
//        if (context.processingLog != null) {
//            StringBuilder builder = new StringBuilder();
//            builder.append(OUT_REMOVE).append(": ").append(var);
//                context.processingLog.add(ProcessingInformation.info(OUTLIERS,
//                    OutliersDetector.class.getName(), builder.toString(), null));
//        }
    }

    private void addVaInfo(ModellingContext context, double va) {
//        if (context.processingLog != null) {
//            StringBuilder builder = new StringBuilder();
//            builder.append(VA).append(": ").append(va);
//                context.processingLog.add(ProcessingInformation.info(OUTLIERS,
//                    OutliersDetector.class.getName(), builder.toString(), null));
//        }
    }

    private static final String OUTLIERS = "Outliers detection", OUT_ADD = "Outlier added", OUT_REMOVE = "Outlier removed", VA = "Critical value";

}
