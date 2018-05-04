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

import demetra.data.AverageInterpolator;
import demetra.data.DoubleSequence;
import demetra.design.BuilderPattern;
import demetra.design.Development;
import demetra.modelling.regression.ModellingContext;
import demetra.regarima.IRegArimaInitializer;
import demetra.regarima.RegArimaModel;
import demetra.regarima.ami.IArmaModule;
import demetra.regarima.ami.IDifferencingModule;
import demetra.regarima.ami.ILogLevelModule;
import demetra.regarima.regular.IModelBuilder;
import demetra.regarima.ami.IOutliersDetectionModule;
import demetra.regarima.regular.IPreprocessor;
import demetra.regarima.ami.IRegressionModule;
import demetra.regarima.regular.ISeasonalityDetector;
import demetra.regarima.regular.ModelDescription;
import demetra.regarima.regular.RegArimaContext;
import demetra.regarima.regular.PreprocessingModel;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import demetra.timeseries.TimeSelector;
import demetra.timeseries.TsData;
import javax.annotation.Nonnull;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class TramoProcessor implements IPreprocessor {

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(TramoProcessor.class)
    public static class Builder {

        private IModelBuilder modelBuilder = new DefaultModelBuilder();
        private ILogLevelModule<SarimaModel> transformation;
        private ISeasonalityDetector seas = new SeasonalityDetector();
        private IRegressionModule regressionTest;
        private IDifferencingModule differencing;
        private IArmaModule arma;
        private IOutliersDetectionModule outliers;

        public Builder modelBuilder(@Nonnull IModelBuilder builder) {
            this.modelBuilder = builder;
            return this;
        }

        public Builder seasonalityDetector(@Nonnull ISeasonalityDetector seas) {
            this.seas = seas;
            return this;
        }

        public Builder logLevel(ILogLevelModule<SarimaModel> ll) {
            this.transformation = ll;
            return this;
        }

        public Builder differencing(IDifferencingModule diff) {
            this.differencing = diff;
            return this;
        }

        public Builder regressionTest(IRegressionModule regressionTest) {
            this.regressionTest = regressionTest;
            return this;
        }

        public Builder arma(IArmaModule arma) {
            this.arma = arma;
            return this;
        }

        public Builder outliers(IOutliersDetectionModule outliers) {
            this.outliers = outliers;
            return this;
        }

        public TramoProcessor build() {
            TramoProcessor processor = new TramoProcessor(this);
            return processor;
        }

    }

    public static TramoProcessor of(TramoSpec spec, ModellingContext context) {
        TramoSpecDecoder helper = new TramoSpecDecoder(spec, context);
        return helper.buildProcessor();
    }

    private final IModelBuilder builder;
    private final ISeasonalityDetector seas;
    private final ILogLevelModule<SarimaModel> transformation;
    private final IRegressionModule regressionTest;
    private final IOutliersDetectionModule outliers;

//    public IPreprocessingModule loglevelTest;
//    public IOutliersDetectionModule outliers;
//    public IPreprocessingModule differencing;
//    public IPreprocessingModule autoModelling;
//    public IPreprocessingModule regressionTest2, regressionTest3;
//    public IModelController seasonalityController = new SeasonalityController();
//    public FinalEstimator finalizer;
//    public List<IModelController> controllers = new ArrayList<>();
//    public IModelController benchmarking;
//    private final IModelBuilder defaultBuilder = new DefaultModelBuilder();
//    private boolean mu_, pass3_;
//    private boolean dfm_;
//    private double pcr_ = .95, cpcr_;
//    private boolean fal_ = false;
//    private double tsig_ = 1;
//    private PreprocessingModel reference_;
//    private ModelStatistics refstats_;
    private int pass = 0, round = 0;
    private boolean needOutliers;
    private boolean needAutoModelling;

    private TramoProcessor(Builder builder) {
        this.builder = builder.modelBuilder;
        this.transformation = builder.transformation;
        this.seas = builder.seas;
        this.regressionTest = builder.regressionTest;
        this.outliers = builder.outliers;
    }

    @Override
    public PreprocessingModel process(TsData originalTs, RegArimaContext context) {
//        clear();
        if (context == null) {
            context = new RegArimaContext();
        }
        ModelDescription desc = builder.build(originalTs, context.getLog());
        if (desc == null) {
            throw new TramoException("Initialization failed");
        }
        context.setDescription(desc);

        PreprocessingModel rslt = calc(context);
//        if (rslt != null) {
//            rslt.info_ = context.information;
//            rslt.addProcessingInformation(context.processingLog);
//        }
        return rslt;
    }

    private PreprocessingModel calc(RegArimaContext context) {

        // Test the seasonality
        if (seas != null) {
            testSeasonality(context);
        }
        if (transformation != null) {
            testTransformation(context);
        }
        if (regressionTest != null) {
            regressionTest.test(context);
        }
        initProcessing();

        // Step 1.
        // Initial adjustments:
        // - interpolation of the missing values
        // - change of units [should always be done, to avoid "scale" effects]
        // Step 2.
        // Creates a "default model", which is an airline model (0 1 1)(0 1 1) with mean,
        // except when no seasonal component is needed; the model is then
        // (0 1 1)(0 0 0) with mean
        // Step 3.
        // Computes the default model with data in levels/logs
        // The default model, without regression variables is always used.
        // Step 4.
        // Check for the presence of trading days/Easter/[others] and mean correction.
        // The default model, without regression variables other than the tested ones
        // is always used.
        // Step 5.
        // Complete the model with all the pre-specified regression variables
        // and with any pre-specified arima model (or orders).
        try {
//            context.getDescription().

//            if (builder != null) {
//                builder.initialize(context);
//            } else {
//                defaultBuilder.initialize(context);
//            }
//            if (context.description.isFullySpecified() && outliers == null) {
//                // nothing to do
//                IParametricMapping<SarimaModel> mapping = context.description.defaultMapping();
//                ModelDescription model = context.description;
//                RegArimaModel<SarimaModel> regarima = model.buildRegArima();
//                TramoModelEstimator monitor = new TramoModelEstimator(mapping);
//                monitor.getMinimizer().setMaxIter(1);
//                monitor.optimize(regarima);
//                ModelEstimation estimation = new ModelEstimation(regarima, model.getLikelihoodCorrection());
//                estimation.computeLikelihood(mapping.getDim());
//                estimation.updateParametersCovariance(monitor.getParametersCovariance());
//                context.estimation=estimation;
//                return context.current(true);
//            }
//
//            if (!initContext(context)) {
//                return null;
//            }
//
//            checkSeasonality(context);
//            // log/level...
//            if (loglevelTest != null) {
//                loglevelTest.process(context);
////                addLogLevelHistory(context);
//            }
//            // regression effects
//            if (regressionTest != null) {
//                regressionTest.process(context);
////                addRegressionHistory(context);
//            }
//
//            initProcessing();
//            // use airline as the reference
////            if (!context.description.isRegressionPrespecified() || context.outliers || context.automodelling) {
////                if (update(context)) {
////                    reference_ = context.current(true);
////                    refstats_ = new ModelStatistics(reference_);
////                    if (!context.outliers)
////                    addModelInfo(refstats_, context, true);
////                }
////            }
//
//            int iter = 0;
//            do {
//                ++iter;
//            } while (iter < 10 && !iterate(context));
//            if (!update(context)) {
//                return null; // to be sure that the model has been estimated
//            }
//            return context.current(true);
            return null;
        } catch (Exception err) {

            return null;
        } finally {
//            clear();
        }
    }

    private void initProcessing() {
        needOutliers = outliers != null;
        needAutoModelling = false;
        round = 0;
    }
//
//    /**
//     *
//     * @param context
//     * @return True if the model doesn't need further iteration
//     */
//    private boolean iterate(RegArimaContext context) {
//        // we check that we have a valid estimation...
//        if (!context.description.isRegressionPrespecified() || context.outliers || context.automodelling) {
//            update(context);
//
//            boolean changed = false;
//            SarimaSpecification curspec = context.description.getSpecification();
//            boolean curmu = context.description.isMean();
//            if (needDifferencing(context)) {
//                execDifferencing(context);
//            }
//            if (needAutoModelling(context)) {
//                execAutoModelling(context);
//                changed = (!context.description.getSpecification().equals(curspec))
//                        || context.description.isEstimatedMean()!= curmu;
////                if (outliers == null && round_ == 1) {
////                    if (testAutoModel(context)) {
////                        if (regressionTest2.process(context) != ProcessingResult.Unchanged) {
////                            addRegressionHistory(context);
////                        }
////                    }
////                } else if (round_ == 1) {
//                if (round_ == 1) {
//                    needOutliers_ = outliers != null && changed;
//                }
//            }
//            if (needOutliers(context)) {
//                boolean autoOut = execOutliers(context);
//                changed = changed || autoOut;
//            }
//            if (!estimateModel(context)) {
//                needOutliers_ = outliers != null;
//                needAutoModelling_ = differencing != null;
//                ++round_;
//                ++pass_;
//                return false;
//            }
//
//            if (round_ == 0) {
//                needOutliers_ = outliers != null;
//                needAutoModelling_ = differencing != null;
//                ++round_;
//                ++pass_;
//                reference_ = context.current(true);
//                refstats_ = new ModelStatistics(reference_);
//                addModelInfo(refstats_, context, true);
//                if (fal_ && (1 - refstats_.ljungBoxPvalue) < getPcr()) {
//                    return true;
//                } else {
//                    return false;
//                }
//            }
//            if (pass_ <= 3 && !pass3_ && autoModelling != null) {
//                if (!pass2(!changed, context)) {
//                    return false;
//                }
//            }
//
////            if (autoModelling != null && !checkMean(context)) {
////                addRegressionHistory(context);
////                return false;
////            }
//            if (regressionTest3.process(context) != ProcessingResult.Unchanged) {
////                addRegressionHistory(context);
//                pass_ = 4;
//                needAutoModelling_ = false;
//                needOutliers_ = false;
//                return false;
//            }
//
//            ModelEstimator estimator = new ModelEstimator();
//            estimator.setOutliersDetectionModule(outliers);
//            estimator.setPrecision(finalizer.getEpsilon());
//
//            if (context.automodelling) {
//                seasonalityController.setEstimator(estimator);
//                if (seasonalityController.process(context) == ProcessingResult.Changed) {
//                    if (!pass3_) {
//                        pass3_ = true;
//                        needAutoModelling_ = true;
////                        context.description.setOutliers(null);
//                        needOutliers_ = outliers != null;
//                        pass_ = 1;
//                        return false;
//                    }
//                }
//            }
//            control(context, estimator);
//            return true;
//        } else if (finalizer.estimate(context)) {
//            return true;
//        } else {
//            context.information.addError("Tramo final estimation failed");
//            throw new TramoException("Unable to estimate the model");
//        }
//    }
//
//    void setFal(boolean b) {
//        fal_ = b;
//    }
//
//    boolean isFal() {
//        return fal_;
//    }
//
//    private void control(RegArimaContext context, ModelEstimator estimator) {
//        boolean changed = false;
//        for (IModelController controller : controllers) {
//            try {
//                controller.setEstimator(estimator);
//                ProcessingResult rslt = controller.process(context);
//                if (rslt == ProcessingResult.Changed) {
//                    changed = true;
//                }
//            } catch (Exception err) {
//                // if a controller fails, go to the next one !
//            }
//        }
////        if (changed) {
////            pass_=4;
////            estimateModel(context);
////        }
//
//    }
//
//    private boolean needDifferencing(RegArimaContext context) {
//        if (!needAutoModelling_) {
//            return false;
//        }
//        if (round_ == 2 && context.description.getOutliers().isEmpty()) {
//            return false;
//        }
//        SarimaSpecification curspec = context.description.getSpecification();
//        if (round_ == 2 && curspec.getBD() == 2 && curspec.getD() == 1) {
//            return false;
//        }
//
//        return true;
//    }
//
//    private boolean execDifferencing(RegArimaContext context) {
//        if (context.estimation == null) {
//            ModelEstimation estimation = new ModelEstimation(context.description.buildRegArima(),
//                    context.description.getLikelihoodCorrection());
//            estimation.compute(getMonitor(), context.description.getArimaComponent().getFreeParametersCount());
//        }
//        SarimaSpecification prevspec = context.description.getSpecification();
//        if (round_ == 1) {
//            context.description.setSpecification(
//                    new SarimaSpecification(context.description.getFrequency()));
//        }
//        ProcessingResult drslt = differencing.process(context);
//        SarimaSpecification curspec = context.description.getSpecification();
//
//        boolean changed = drslt == ProcessingResult.Changed;
//        if (pass_ == 1) {
//            changed = prevspec.getD() != curspec.getD() || prevspec.getBD() != curspec.getBD();
//            if (changed) {
//                context.description.setOutliers(null);
//                context.estimation = null;
//            }
//        }
////        addDifferencingHistory(context);
//        return changed;
//    }
//
//    private boolean needAutoModelling(RegArimaContext context) {
//        if (!needAutoModelling_) {
//            return false;
//        }
//        if (round_ == 2 && context.description.getOutliers().isEmpty()) {
//            return false;
//        }
//        return true;
//
//    }
//
//    private boolean execAutoModelling(RegArimaContext context) {
//        SarimaSpecification prevspec = context.description.getSpecification();
//        ProcessingResult autorslt = autoModelling.process(context);
//        SarimaSpecification curspec = context.description.getSpecification();
//        if (curspec.getParametersCount() == 0) {
//            if (pass_ >= 3) {
//                context.description.setSpecification(prevspec);
////                addArmaHistory(context);
//                return false;
//            } else {
//                curspec.setQ(1);
//                context.description.setSpecification(curspec);
////                addArmaHistory(context);
//                return true;
//            }
//        } else {
////            addArmaHistory(context);
//            return !prevspec.equals(curspec);
//        }
//    }
//
//    private boolean needOutliers(RegArimaContext context) {
//        return needOutliers_;
//    }
//
//    private boolean execOutliers(RegArimaContext context) {
//        //if (!keepOutliers_) {
//        context.description.setOutliers(null);
//        context.estimation = null;
//        //}
//
//        ProcessingResult autoout = outliers.process(context);
////        addOutliersHistory(context);
//        return autoout == ProcessingResult.Changed;
//    }
//
//
//    private void restore(RegArimaContext context) {
//        context.description = reference_.description.clone();
//        context.estimation = reference_.estimation;
//        context.information.clear();
//        context.information.copy(reference_.info_);
//    }
//
//    /////////////////////////////////////////////////////////////////////////////
//    static int autlar(final int n, final SarimaSpecification spec) {
//        int d = spec.getD() + spec.getFrequency() * spec.getBD();
//        int q = spec.getQ() + spec.getFrequency() * spec.getBQ();
//        int p = spec.getP() + spec.getFrequency() * spec.getBP();
//        int nd = n - d;
//        int nar = (int) Math.log(nd * nd);
//        int m = Math.max(p, 2 * q);
//        if (m > nar) {
//            nar = m;
//        }
//        if (nar >= nd) {
//            nar = nd - nd / 4;
//        }
//        if (nar > 50) {
//            nar = 50;
//        }
//        int ncol = spec.getP() + (1 + spec.getP()) * spec.getBP() + spec.getQ()
//                + (1 + spec.getQ()) * spec.getBQ();
//        return nd - nar - Math.max(p, q) - ncol;
//    }
//
//    static boolean meantest(final int n, final double t) {
//        double vct = 2.5;
//        if (n <= 80) {
//            vct = 1.96;
//        } else if (n <= 155) {
//            vct = 1.98;
//        } else if (n <= 230) {
//            vct = 2.1;
//        } else if (n <= 320) {
//            vct = 2.3;
//        }
//        return Math.abs(t) > vct;
//    }
//
//    public static int calcLBLength(final int freq) {
//        int n;
//        if (freq == 12) {
//            n = 24;
//        } else if (freq == 1) {
//            n = 8;
//        } else {
//            n = 4 * freq;
//        }
//        return n;
//    }
//
//    static double PLjungBox(final int freq, final double[] res,
//            final int hp) {
//        int n = calcLBLength(freq);
//
//        LjungBoxTest lb = new LjungBoxTest();
//        lb.setHyperParametersCount(hp);
//        lb.setK(n);
//        lb.test(new ReadDataBlock(res));
//        return 1 - lb.getPValue();
//    }
//
//    public static double PLjungBox(final RegArimaEstimation<SarimaModel> gls) {
//        SarimaSpecification spec = gls.model.getArima().getSpecification();
//        return PLjungBox(spec.getFrequency(), gls.likelihood.getResiduals(), spec.getParametersCount());
//    }
//
//    protected boolean pass2(final boolean same, RegArimaContext context) {
//        double fct = 1, fct2 = 1;
//        boolean useprev = false;
//        SarimaModel curmodel = context.estimation.getRegArima().getArima();
//        SarimaSpecification curspec = curmodel.getSpecification();
//
//        PreprocessingModel cur = context.current(true);
//        ModelStatistics stats = new ModelStatistics(cur);
//        double plbox = 1 - stats.ljungBoxPvalue;
//        double rvr = stats.se;
//        if (reference_ != null) {
//            double plbox0 = 1 - refstats_.ljungBoxPvalue;
//            double rvr0 = refstats_.se;
//
//            addModelInfo(stats, context, false);
//            if (reference_.description.getOutliers().size()
//                    <= context.description.getOutliers().size() && ((plbox < .95 && plbox0 < .75 && rvr0 < rvr)
//                    // 1. the previous model was significantly better
//                    || (pass_ == 1 && plbox >= .95 && plbox0 < .95)
//                    // 2. no improvement
//                    || (plbox < .95 && plbox0 < .75 && plbox0 < plbox && rvr0 < fct * rvr)
//                    // 3.
//                    || (plbox >= .95 && plbox0 < .95 && rvr0 < fct2 * rvr)
//                    // 4. degradation
//                    || (curspec.getD() == 0 && curspec.getBD() == 1 && curspec.getP() == 1
//                    && curmodel.phi(1) <= -.82 && curspec.getQ() <= 1
//                    && curspec.getBP() == 0 && curspec.getBQ() == 1)
//                    //quasi airline model
//                    || (curspec.getD() == 1 && curspec.getBD() == 0 && curspec.getP() == 0
//                    && curspec.getQ() == 1 && curspec.getBP() == 1
//                    && curmodel.getParameter(0) <= -.65 && curspec.getBQ() <= 1))) {
//                useprev = true;
//            }
//            if (!useprev) {
//                reference_ = cur;
//                refstats_ = stats;
////            if (outliers != null) {
////                refsens_ = outliers.getSelectivity();
////            }
//            } else {
//                restore(context);
//                plbox = plbox0;
//            }
//        } else {
//            reference_ = cur;
//            refstats_ = stats;
//        }
//
//        if (pass_ == 1) {
//            cpcr_ += .025;
//        } else if (pass_ >= 2) {
//            cpcr_ += .015;
//        }
//
//        if (plbox <= cpcr_) {
//            return true;
//        }
//
//        if (pass_ == 1 && outliers != null) {
//            outliers.reduceSelectivity();
//            //keepOutliers_ = true;
//        }
//
//        ++round_;
//        ++pass_;
//
//        if (pass_ <= 2) {
//            needAutoModelling_ = !same;
//            needOutliers_ = outliers != null;
//        } else {
//            // use the last solution (3 1 1) (0 1 1), re-estimates all
//            lastSolution(context);
//            needAutoModelling_ = false;
//        }
//        return false;
//    }
//
//    // use the default model, clear outliers
//    private void lastSolution(RegArimaContext context) {
//        SarimaSpecification nspec = context.description.getSpecification();
//        nspec.setP(3);
//        if (nspec.getBD() > 0) {
//            nspec.setBP(0);
//        }
//        nspec.setQ(1);
//        if (context.hasseas) {
//            nspec.setBQ(1);
//        }
////        if (outliers != null) {
////            outliers.setSelectivity(0);
////        }
//        context.description.setSpecification(nspec);
//        context.description.setOutliers(null);
//        context.estimation = null;
////        addArmaHistory(context);
//        round_ = 1;
//        needOutliers_ = outliers != null;
//        needAutoModelling_ = false;
//        //setDfm(true);
//    }
//
//    private void clear() {
//        pass_ = 0;
//        pass3_ = false;
//        round_ = 0;
//        cpcr_ = pcr_;
//        reference_ = null;
//        refstats_ = null;
//        //refsens_ = 0;
//        if (outliers != null) {
//            outliers.setSelectivity(0);
//        }
//        dfm_ = false;
//        seasonalityController.setReferenceModel(null);
//        for (IModelController controller : controllers) {
//            controller.setReferenceModel(null);
//        }
//    }
//
////    private boolean checkMean(ModellingContext context) {
////        if (context.description.isMean() && context.automodelling && (pass_ != 0 || !isDfm())) {
////            int nhp = context.description.getArimaComponent().getFreeParametersCount();
////            double e = context.estimation.getLikelihood().getBSer(0, true, nhp);
////            double mu = context.estimation.getLikelihood().getB()[0];
////            if (!mu_ && Math.abs(mu / e) < getTsig()) {
////                mu_ = true;
////                context.description.setMean(false);
////                RegArimaModel<SarimaModel> nregarima = context.description.buildRegArima();
////                nregarima.setArima(context.estimation.getArima());
////                context.estimation = new ModelEstimation(nregarima, context.description.getLikelihoodCorrection());
////                context.estimation.computeLikelihood(nhp);
////                return false;
////            }
////        }
////        return true;
////    }
//    private boolean initContext(RegArimaContext context) {
//        context.automodelling = autoModelling != null;
//        context.outliers = outliers != null;
//        // scaling
//        if (scaling != null) {
//            if (!scaling.process(context)) {
//                return false;
//            }
//        }
//
//        // missing value...
//        if (missing != null) {
//            if (!context.description.updateMissing(missing)) {
//                return false;
//            }
//        }
//        return true;
//    }
//

    private void testTransformation(RegArimaContext context) {
        ModelDescription model = context.getDescription();
        RegArimaModel<SarimaModel> regarima = RegArimaModel.builder(SarimaModel.class)
                .y(DoubleSequence.ofInternal(model.transformation().data))
                .meanCorrection(true)
                .arima(model.getArimaComponent().getModel())
                .build();
        if (transformation.process(regarima) && transformation.isChoosingLog()) {
            context.getDescription().setLogTransformation(true);
            context.setEstimation(null);
        }
    }

    private void testSeasonality(RegArimaContext context) {
        ModelDescription model = context.getDescription();
        int ifreq = model.getAnnualFrequency();
        if (ifreq > 1) {
            ISeasonalityDetector.Seasonality s = seas.hasSeasonality(model.getTransformedSeries());
//            model.setSeasonality(s.getAsInt() >= 2);
            if (s.getAsInt() < 2) {
                SarimaSpecification nspec = new SarimaSpecification(ifreq);
                nspec.airline(false);
                model.setSpecification(nspec);
                context.setEstimation(null);
                context.setSeasonal(false);
            } else {
                context.setSeasonal(true);
            }
        } else {
            context.setSeasonal(false);
        }
    }
//
////    private boolean finalMeanTest(ModellingContext context) {
////
////        if (context.description.isMean() || mu_) {
////            return true;
////        }
////        double[] res = context.estimation.getLikelihood().getResiduals();
////        double s = 0, s2 = 0;
////        int n = res.length;
////        for (int i = 0; i < n; ++i) {
////            s += res[i];
////            s2 += res[i] * res[i];
////        }
////        double rtval = Math.abs(s / Math.sqrt((s2 * n - s * s) / n));
////
////        if (rtval <= 2.5) {
////            return true;
////        }
////        mu_ = true;
////        context.description.setMean(true);
////        RegArimaModel<SarimaModel> nregarima = context.description.buildRegArima();
////        nregarima.setArima(context.estimation.getArima());
////        context.estimation = new ModelEstimation(nregarima, context.description.getLikelihoodCorrection());
////        int nhp = context.description.getArimaComponent().getFreeParametersCount();
////        context.estimation.computeLikelihood(nhp);
////        pass_ = 3;
//////        addMeanHistory(context);
////        return false;
////    }
////    static void addArmaHistory(ModellingContext context) {
////        context.information.subSet("history").add("arma", context.description.getSpecification().doStationary().toString());
////    }
////
////    static void addDifferencingHistory(ModellingContext context) {
////        SarimaSpecification spec = context.description.getSpecification();
////        StringBuilder msg = new StringBuilder();
////        msg.append("d=").append(spec.getD());
////        msg.append(" bd=").append(spec.getBD());
////        if (context.description.isMean()) {
////            msg.append(" mean");
////        }
////        context.information.subSet("history").add("differencing", msg.toString());
////    }
////    static void addBenchmarkingHistory(ModellingContext context) {
////        SarimaSpecification spec = context.description.getSpecification();
////        context.information.subSet("history").add("benchmark", spec.toString());
////    }
////
////    static void addOutliersHistory(ModellingContext context) {
////        int no = context.description.getOutliers().size();
////        context.information.subSet("history").add("outliers", Integer.toString(no));
////    }
////
////    static void addMeanHistory(ModellingContext context) {
////        StringBuilder msg = new StringBuilder();
////        if (context.description.isMean()) {
////            msg.append("mean=true");
////        } else {
////            msg.append("mean=false");
////        }
////
////        context.information.subSet("history").add("final mean", msg.toString());
////    }
////
////    static void addForceHistory(ModellingContext context) {
////        context.information.subSet("history").add("force", context.description.getSpecification().toString());
////    }
////
////    static void addLogLevelHistory(ModellingContext context) {
////        boolean log = context.description.getTransformation() == DefaultTransformationType.Log;
////        context.information.subSet("history").add("loglevel", log ? "log" : "level");
////    }
////
////    static void addRegressionHistory(ModellingContext context) {
////        StringBuilder msg = new StringBuilder();
////
////        if (Variable.usedCount(context.description.getCalendars(), ITradingDaysVariable.class
////        ) > 0) {
////            msg.append(
////                    "td ");
////        }
////
////        if (Variable.usedCount(context.description.getCalendars(), ILengthOfPeriodVariable.class
////        ) > 0) {
////            msg.append(
////                    "ly ");
////        }
////
////        if (Variable.usedCount(context.description.getMovingHolidays(), EasterVariable.class
////        ) > 0) {
////            msg.append(
////                    "easter ");
////        }
////        if (context.description.isMean()) {
////            msg.append("mean");
////        }
////
////        context.information.subSet("history").add("regression", msg.toString());
////    }
//    private boolean estimateModel(RegArimaContext context) {
//
//        finalizer.setPass(pass_);
//        int niter = 0;
//        do {
//            if (!finalizer.estimate(context)) {
//                if (pass_ == 1 && context.automodelling && context.outliers) {
//                    outliers.reduceSelectivity();
//                }
//                return false;
//            }
//            if (pass3_ || pass_ != 0) {// || !context.outliers || !context.automodelling) {
//                return true;
//            }
//        } while (niter++ < 5 && regressionTest2.process(context) == ProcessingResult.Changed);
//        return true;
//    }
//
//    private boolean update(RegArimaContext context) {
//        try {
//            if (context.estimation != null) {
//                return true;
//            }
//            IParametricMapping<SarimaModel> mapping = context.description.defaultMapping();
//            ModelDescription model = context.description;
//            context.estimation = new ModelEstimation(model.buildRegArima(), model.getLikelihoodCorrection());
//            // should be changed for fixed parameters
//            int ndim = mapping.getDim();
//            TramoModelEstimator monitor = new TramoModelEstimator(mapping);
//            if (context.description.isPartiallySpecified()) {
//                context.estimation.improve(monitor, ndim);
//            } else {
//                context.estimation.compute(monitor, ndim);
//            }
//            context.estimation.updateParametersCovariance(monitor.getParametersCovariance());
//            return true;
//        } catch (Exception err) {
//            return false;
//        }
//    }
//
//    /**
//     *
//     * @param context
//     * @return True if the model finally chosen is not an airline model, false
//     * otherwise
//     */
////    private boolean testAutoModel(ModellingContext context) {
////        // the reference model is an airline model...
////        SarimaSpecification spec = context.description.getSpecification();
////        if (spec.isAirline(context.hasseas)) {
////            return false;
////        }
////
////        // compute airline model. It was done in a previous step, but it is difficult
////        // to integrate that step in the logic of the processing. So we do it again
////        // model with mean only
////        ModelDescription desc = context.description.clone();
////        desc.removeVariable(var->true);
////        desc.setMean(true);
////        desc.setAirline(context.hasseas);
////        TramoModelEstimator estimator = new TramoModelEstimator(desc.defaultMapping());
////        ModelEstimation air = new ModelEstimation(desc.buildRegArima());
////        air.compute(estimator, desc.getArimaComponent().getFreeParametersCount());
////
////        PreprocessingModel airline = new PreprocessingModel(desc, air);
////        airline.updateModel();
////        ModelStatistics sairline = new ModelStatistics(airline);
////
////        // the airline model is not accepted
////        Parameter q = airline.description.getArimaComponent().getTheta()[0];
////        if (q.getStde() != 0 && Math.abs(q.getValue() / q.getStde()) < 1.96) {
////            return true;
////        }
////        if (context.hasseas) {
////            Parameter bq = airline.description.getArimaComponent().getBTheta()[0];
////            if (bq.getStde() == 0 && Math.abs(bq.getValue() / bq.getStde()) < 1.96) {
////                return true;
////            }
////        }
////
////        // makes and estimates a model with mean only
////        desc.setSpecification(spec);
////        ModelEstimation estimation = null;
////        boolean accept = true;
////
////        int round = 0;
////        do {
////            accept = true;
////            spec = desc.getSpecification();
////            estimator = new TramoModelEstimator(desc.defaultMapping());
////            estimation = new ModelEstimation(desc.buildRegArima());
////            if (!estimation.compute(estimator, desc.getArimaComponent().getFreeParametersCount())) {
////                return false;
////            }
////            if (round++ != 1 && spec.getParametersCount() > 1 && !Variable.isUsed(context.description.getCalendars())
////                    && !Variable.isUsed(context.description.getMovingHolidays())) {
////                // check the model
////                SarimaSpecification nspec = checkModel(estimation.getArima(), estimator.getParametersCovariance().diagonal());
////                if (nspec != null && !nspec.equals(spec)) {
////                    desc.setSpecification(nspec);
////                    accept = false;
////                }
////            }
////
////        } while (!accept);
////
////        PreprocessingModel tmp = new PreprocessingModel(desc, estimation);
////        ModelStatistics cur = new ModelStatistics(tmp);
////
////        double fct2 = 1, fct0 = 1.025;
////        double pdfm = 1 - sairline.ljungBoxPvalue, pami = 1 - cur.ljungBoxPvalue;
////        double rsddfm = sairline.se, rsdami = cur.se;
////        SarimaModel arima = estimation.getArima();
////        if ((pami < .95 && pdfm < .75 && rsddfm < rsdami)
////                || (pami < .95 && pdfm < .75 && pdfm < pami && rsddfm < fct0 * rsdami)
////                || (pami >= .95 && pdfm < .95 && rsddfm < fct2 * rsdami)
////                || (spec.getD() == 0 && spec.getBD() == 1
////                && spec.getP() == 1 && arima.phi(1) <= -.82
////                && spec.getQ() <= 1 && spec.getBP() == 0 && spec.getBQ() == 1)
////                || (spec.getD() == 1 && spec.getBD() == 0 && spec.getP() == 0
////                && spec.getQ() == 1 && spec.getBP() == 1 && arima.bphi(1) <= -.65
////                && spec.getBQ() <= 1)) {
////            // we reuse airline
////            restore(context);
////            return false;
////        } else {
////            RegArimaModel<SarimaModel> regarima = context.description.buildRegArima();
////            regarima.setArima(tmp.estimation.getArima());
////            context.estimation = new ModelEstimation(regarima);
////            context.estimation.computeLikelihood(desc.getArimaComponent().getFreeParametersCount());
////            return true;
////        }
////    }
////    private SarimaSpecification checkModel(SarimaModel model, IReadDataBlock var) {
////        SarimaSpecification spec = model.getSpecification();
////        IReadDataBlock p = model.getParameters();
////        int beg = 0, len = 0;
////        int cpr = 0, cps = 0, cqr = 0, cqs = 0;
////        if (spec.getP() > 0) {
////            len = spec.getP();
////            cpr = checkParameters(p.rextract(beg, len), var.rextract(beg, len));
////            beg += len;
////        }
////        if (spec.getBP() > 0) {
////            len = spec.getBP();
////            cps = checkParameters(p.rextract(beg, len), var.rextract(beg, len));
////            beg += len;
////        }
////        if (spec.getQ() > 0) {
////            len = spec.getQ();
////            cqr = checkParameters(p.rextract(beg, len), var.rextract(beg, len));
////            beg += len;
////        }
////        if (spec.getBQ() > 0) {
////            len = spec.getBQ();
////            cqs = checkParameters(p.rextract(beg, len), var.rextract(beg, len));
////            beg += len;
////        }
////        int cont = cpr + cps + cqr + cqs;
////        if (cont > 1) {
////            return null;
////        } else {
////            SarimaSpecification nspec = spec.clone();
////            if (cpr == 1) {
////                nspec.setP(spec.getP() - 1);
////            } else if (cps == 1) {
////                nspec.setBP(spec.getBP() - 1);
////            } else if (cqr == 1) {
////                nspec.setQ(spec.getQ() - 1);
////            } else if (cqs == 1) {
////                nspec.setBQ(spec.getBQ() - 1);
////            }
////            return nspec;
////        }
////    }
////    /**
////     *
////     * @param p
////     * @param var
////     * @return The number of non significant final parameters
////     */
////    private int checkParameters(IReadDataBlock p, IReadDataBlock var) {
////        final double cval = 1.8;
////        int n = p.getLength() - 1;
////        int i = 0;
////        while (i <= n) {
////            double e = var.get(n - i);
////            if (e > 0) {
////                e = Math.sqrt(e);
////                double t = p.get(n - i) / e;
////                if (t < cval) {
////                    ++i;
////                } else {
////                    break;
////                }
////            } else {
////                break;
////            }
////        }
////        return i;
////    }
//    /**
//     * @return the dfm_
//     */
//    public boolean isDfm() {
//        return dfm_;
//    }
//
//    /**
//     * @param dfm_ the dfm_ to set
//     */
//    public void setDfm(boolean dfm_) {
//        this.dfm_ = dfm_;
//    }
//
//    /**
//     * @return the pcr_
//     */
//    public double getPcr() {
//        return pcr_;
//    }
//
//    /**
//     * @param pcr_ the pcr_ to set
//     */
//    public void setPcr(double pcr_) {
//        this.pcr_ = pcr_;
//    }
//
//    /**
//     * @return the tsig_
//     */
//    public double getTsig() {
//        return tsig_;
//    }
//
//    /**
//     * @param tsig_ the tsig_ to set
//     */
//    public void setTsig_(double tsig_) {
//        this.tsig_ = tsig_;
//    }
//
//    /**
//     * This function is different from the CHECKSEAS routine of TRAMO.
//     *
//     * @param s The tested series
//     * @return
//     */
//    public static boolean checkSeasonality(TsData s) {
//        if (s.getFrequency() == TsFrequency.Yearly) {
//            return false;
//        }
//        TsData delta = s.delta(1);
//        int ifreq = s.getFrequency().intValue();
//        int k = 3;
//        if (k * ifreq >= delta.getLength()) {
//            k = 2;
//        }
//        LjungBoxTest lb = new LjungBoxTest();
//        lb.setLag(ifreq);
//        lb.setK(k);
//        lb.test(delta);
//        lb.setSignificanceThreshold(.1);
//        return !lb.isValid() || lb.isSignificant();
//    }
//
//    public IModelEstimator getEstimator() {
//        ModelEstimator e = new ModelEstimator();
//        e.setOutliersDetectionModule(outliers);
//        e.setPrecision(finalizer.getEpsilon());
//        return e;
//    }
//
//    private void addModelInfo(ModelStatistics stats, RegArimaContext context, boolean initial) {
////        if (context.processingLog != null) {
////            SarimaModel arima = context.estimation.getRegArima().getArima();
////            context.processingLog.add(ProcessingInformation.info(MODEL_TEST,
////                    TramoProcessor.class
////                    .getName(), initial ? "airline"
////                            : ("current model: " + arima.getSpecification().toString()), arima));
////            context.processingLog.add(ProcessingInformation.info(MODEL_TEST,
////                    TramoProcessor.class
////                    .getName(), "model statistics", stats));
////        }
//    }
//
//    private static final String MODEL_TEST = "Model test";
}
