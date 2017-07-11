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
package ec.tstoolkit.modelling.arima.demetra;

import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.arima.AbstractSingleOutlierDetector;
import ec.tstoolkit.modelling.arima.ExactSingleOutlierDetector;
import ec.tstoolkit.modelling.arima.IOutliersDetectionModule;
import ec.tstoolkit.modelling.arima.ModelDescription;
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
import ec.tstoolkit.timeseries.regression.OutlierDefinition;
import ec.tstoolkit.timeseries.regression.SeasonalOutlierFactory;
import ec.tstoolkit.timeseries.regression.TransitoryChangeFactory;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.ArrayList;
import java.util.List;

/**
 * Simplified version of the solution used in Tramo. We don't use
 * Hannan-Rissanen (which is often misleading)
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class OutliersDetectionModule extends DemetraModule implements IOutliersDetectionModule {

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
     * @return the method
     */
    public Method getMethod() {
        return method;
    }

    /**
     * @param method the method to set
     */
    public void setMethod(Method method) {
        this.method = method;
    }

    public static enum Method {

        Tramo,
        X13,
        Demetra1
    }

    public static enum Estimation {

        Approximate,
        ExactIterative,
        ExactFixed
    }

    private static final double EPS = 1e-5;
    private static int MAXROUND = 50;
    private static int MAXOUTLIERS = 24;
    private RegArimaModel<SarimaModel> regarima_;
    private final ArrayList<IOutlierVariable> outliers_ = new ArrayList<>();
    private final AbstractSingleOutlierDetector sod;
    private double[] tstats_;
    private int nhp_;
    private int round_;
    // festim = true if the model has to be re-estimated
    private boolean exit_;
    private IOutlierVariable lastremoved_;

    private TsPeriodSelector span_;
    private Estimation estimation = Estimation.ExactIterative;
    private Method method = Method.Tramo;
    private int selectivity_;
    private double cv_, curcv_;
    private double pc_ = 0.12;
    public static final double MINCV = 2.0;

    public OutliersDetectionModule() {
        sod = new ExactSingleOutlierDetector();
    }

    public OutliersDetectionModule(AbstractSingleOutlierDetector sod) {
        this.sod = sod;
    }

    @Override
    public ProcessingResult process(ModellingContext context) {
        clear();
        List<OutlierDefinition> initial = OutlierDefinition.of(context.description.getOutliers());
        if (curcv_ == 0) {
            curcv_ = calcCv(context);
        }
        addVaInfo(context, curcv_);

        TsDomain edomain = context.description.getEstimationDomain();
        sod.prepare(edomain, span_ == null ? null : edomain.select(span_));
        // exclude missing and previous outliers
        sod.exclude(context.description.getMissingValues());
        sod.exclude(context.description.getOutliersPosition(true));
        sod.exclude(context.description.getOutliersPosition(false));
        outliers_.addAll(context.description.getOutliers());

        regarima_ = context.description.buildRegArima();
        nhp_ = regarima_.getArima().getParametersCount();
        if (!estimateModel(true)) {
            return ProcessingResult.Failed;
        }
        try {
            switch (method) {
                case Tramo:
                    calcTramo(context);
                    break;
                case X13:
                    calcX13(context);
                    break;
                case Demetra1:
                    calcDemetra1(context);
                    break;
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

    private void calcTramo(ModellingContext context) {
        double max;
        exit_ = false;
        do {
            if (!sod.process(regarima_)) {
                break;
            }
            round_++;
            max = sod.getMaxTStat();
            if (Math.abs(max) < curcv_) {
                break;
            }
            IOutlierVariable o = sod.getMaxOutlier();
            boolean bok = true;
            for (int i = 0; i < outliers_.size(); ++i) {
                if (o.getPosition().equals(outliers_.get(i).getPosition())) {
                    bok = false;
                    break;
                }
            }
            if (bok) {
                addOutlier(o);
                addOutlierInfo(context, o, max);
                estimateModel(false);

                while (!verifyModel(context)) {
                    if (exit_) {
                        break;
                    }
//                    estimateModel(false);
                    updateLikelihood(regarima_.computeLikelihood());
                }
                if (exit_ || outliers_.size() == MAXOUTLIERS) {
                    break;
                }
            } else {
                break;
            }
        } while (round_ < MAXROUND);

        while (!verifyModel(context)) {
//                updateLikelihood(regarima_.computeLikelihood());
            estimateModel(false);
        }
    }

    private void calcX13(ModellingContext context) {
        double max;
        do {
            if (!sod.process(regarima_)) {
                break;
            }
            round_++;
            max = sod.getMaxTStat();
            if (Math.abs(max) < curcv_) {
                break;
            }
            IOutlierVariable o = sod.getMaxOutlier();
            addOutlier(o);
            addOutlierInfo(context, o, max);
            estimateModel(false);

            if (outliers_.size() == MAXOUTLIERS) {
                break;
            }
        } while (round_ < MAXROUND);

        while (!verifyModel(context)) {
            estimateModel(false);
        }
    }

    private void calcDemetra1(ModellingContext context) {
        double max;
        do {
            if (!sod.process(regarima_)) {
                break;
            }
            round_++;
            max = sod.getMaxTStat();
            if (Math.abs(max) < curcv_) {
                break;
            }
            IOutlierVariable o = sod.getMaxOutlier();
            addOutlier(o);
            addOutlierInfo(context, o, max);
            if (outliers_.size() == MAXOUTLIERS) {
                break;
            }
        } while (round_ < MAXROUND);
        estimateModel(true);
        while (!verifyModel(context)) {
            estimateModel(false);
        }
    }

    private boolean estimateModel(boolean full) {
        GlsSarimaMonitor monitor = this.monitor();
        monitor.setPrecision(1e-4);

//        System.out.println(outliers());
        RegArimaEstimation<SarimaModel> est = full ? monitor.process(regarima_) : monitor.optimize(regarima_);
        regarima_ = est.model;
        updateLikelihood(est.likelihood);
        return true;
    }

    private void updateLikelihood(ConcentratedLikelihood likelihood) {
        tstats_ = likelihood.getTStats(true, nhp_);
    }

    private void clear() {
        nhp_ = 0;
        outliers_.clear();
        round_ = 0;
        lastremoved_ = null;
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
        IOutlierVariable toremove = outliers_.get(imin);
        sod.allow(toremove);
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
        o.data(sod.getDomain().getStart(), XO);
        regarima_.addX(XO);
        sod.exclude(o);
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
        return sod.getOutlierFactoriesCount();
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

    private double calcCv(ModellingContext context) {
        double cv = cv_;
        if (cv == 0) {
            cv = ICriticalValueComputer.defaultComputer().compute(context.description.getY().length);
        }
        for (int i = 0; i < -selectivity_; ++i) {
            cv *= (1 - pc_);
        }
        return Math.max(cv, MINCV);
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

//    private String outliers(){
//        StringBuilder builder=new StringBuilder();
//        for (int i=0; i<this.outliers_.size(); ++i){
//            builder.append(outliers_.get(i)).append('\t');
//        }
//        return builder.toString();
//    }
    private static final String OUTLIERS = "Outliers detection", OUT_ADD = "Outlier added", OUT_REMOVE = "Outlier removed", VA = "Critical value";

}
