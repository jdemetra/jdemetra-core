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

import demetra.design.BuilderPattern;
import demetra.design.Development;
import demetra.modelling.regression.ModellingContext;
import demetra.regarima.regular.ILogLevelModule;
import demetra.regarima.regular.IRegressionModule;
import demetra.regarima.regular.ProcessingResult;
import demetra.regarima.regular.IModelBuilder;
import demetra.regarima.regular.IPreprocessor;
import demetra.regarima.regular.ModelDescription;
import demetra.regarima.regular.PreprocessingModel;
import demetra.regarima.regular.RegArimaModelling;
import demetra.timeseries.TsData;
import javax.annotation.Nonnull;
import demetra.regarima.ami.IGenericDifferencingModule;
import demetra.regarima.regular.IArmaModule;
import demetra.regarima.regular.IDifferencingModule;
import demetra.regarima.regular.IOutliersDetectionModule;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class X12Preprocessor implements IPreprocessor {

    @lombok.Value
    @lombok.Builder
    public static class AmiOptions {

        boolean checkMu;
        double precision;
        double va;
        double reduceVa;
        double ljungBoxLimit;
        boolean acceptAirline;
    }

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(X12Preprocessor.class)
    public static class Builder {

        private IModelBuilder modelBuilder = new DefaultModelBuilder();
        private ILogLevelModule transformation;
        private IRegressionModule calendarTest, easterTest;
        private IDifferencingModule differencing;
        private IArmaModule arma;
        private IOutliersDetectionModule outliers;
        private AmiOptions options = new AmiOptions(true, 1e-7, 0, .14286, .95, false);

        public Builder modelBuilder(@Nonnull IModelBuilder builder) {
            this.modelBuilder = builder;
            return this;
        }

        public Builder options(AmiOptions options) {
            this.options = options;
            return this;
        }

        public Builder logLevel(ILogLevelModule ll) {
            this.transformation = ll;
            return this;
        }

        public Builder differencing(IDifferencingModule diff) {
            this.differencing = diff;
            return this;
        }

        public Builder calendarTest(IRegressionModule calendarTest) {
            this.calendarTest = calendarTest;
            return this;
        }

        public Builder easterTest(IRegressionModule easterTest) {
            this.easterTest = easterTest;
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

        public X12Preprocessor build() {
            X12Preprocessor processor = new X12Preprocessor(this);
            return processor;
        }

    }

    public static X12Preprocessor of(RegArimaSpec spec, ModellingContext context) {
        X12SpecDecoder helper = new X12SpecDecoder(spec, context);
        return helper.buildProcessor();
    }

    private final IModelBuilder modelBuilder;
    private final ILogLevelModule transformation;
    private final IRegressionModule calendarTest, easterTest;
    private final IOutliersDetectionModule outliers;
    private final AmiOptions options;
    private final IDifferencingModule differencing;
    private final IArmaModule arma;

    private double curva = 0;
    private PreprocessingModel refAirline, refAuto;
    private boolean needOutliers;
    private boolean needAutoModelling;
    private int loop, round;
    private double plbox, rvr, rtval;

    private X12Preprocessor(Builder builder) {
        this.modelBuilder = builder.modelBuilder;
        this.transformation = builder.transformation;
        this.calendarTest = builder.calendarTest;
        this.easterTest = builder.easterTest;
        this.outliers = builder.outliers;
        this.options = builder.options;
        this.differencing = builder.differencing;
        this.arma = builder.arma;
    }

    private void clear() {
        loop = round = 0;
        plbox = rvr = rtval = 0;
        refAirline = null;
        curva = 0;
        needAutoModelling = differencing != null || arma != null;
    }

    @Override
    public PreprocessingModel process(TsData originalTs, RegArimaModelling context) {
        clear();
        if (context == null) {
            context = new RegArimaModelling();
        }
        ModelDescription desc = modelBuilder.build(originalTs, context.getLog());
        if (desc == null) {
            throw new X12Exception("Initialization failed");
        }
        context.setDescription(desc);

        // initialize some internal variables
        if (outliers != null) {
            curva = options.getVa();
            if (curva == 0) {
                curva = X12Utility.calcCv(desc.getSeries().getDomain().getLength());
            }
            needOutliers = true;
        }

        PreprocessingModel rslt = calc(context);
//        if (rslt != null) {
//            rslt.info_ = context.information;
//            rslt.addProcessingInformation(context.processingLog);
//        }
        return rslt;
    }

    private PreprocessingModel calc(RegArimaModelling context) {
        try {
            if (transformation != null) {
                transformation.process(context);
            }

            regAIC(context);

            checkMu(context, true);

            if (needOutliers && ProcessingResult.Changed == outliers.process(context, curva)) {
                if (context.needEstimation()) {
                    context.estimate(options.precision);
                }
                // TODO test regression effects
            }
            if (isAutoModelling()) {
                if (context.needEstimation()) {
                    context.estimate(options.precision);
                }
                refAirline = context.build();
                ModelController controller = new ModelController(.95, 1);
                boolean ok = controller.accept(context);
                plbox = 1 - controller.getLjungBoxTest().getPValue();
                rvr = controller.getRvr();
                rtval = controller.getRTval();
                if (!options.acceptAirline || !ok) {

                    round = 1;
                    loop = 1;
                    do {
                        boolean defModel = false;
                        if (needAutoModelling) {
                            ProcessingResult drslt = differencing.process(context);
                            ProcessingResult arslt = arma.process(context);
                            defModel = drslt == ProcessingResult.Unchanged
                                    && arslt == ProcessingResult.Unchanged;
                            if (!defModel) {
                                if (context.getDescription().removeVariable(var -> var.isOutlier(false))) {
                                    context.setEstimation(null);
                                    needOutliers = outliers != null;
                                }
                            }
                            if (context.needEstimation()) {
                                context.estimate(options.precision);
                            }
                            if (!defModel || loop > 1) {
                                regAIC(context);
                            }
                        }
                        if (needOutliers) {
                            outliers.process(context, curva);
                            if (context.needEstimation()) {
                                context.estimate(options.precision);
                            }
                        }
//                    if (! needOutliers && loop <= 2) {
//                        if (!pass2(defModel, context)) {
//                            continue;
//                        }
//                    }
//                    if (regressionTest1 != null) {
//                        ProcessingResult changed = regressionTest1.process(context);
//                        if (changed == ProcessingResult.Changed) {
//                            if (loop_ < 3) {
//                                loop_ = 3;
//                            }
//                            if (context.estimation == null) {
//                                estimator.estimate(context);
//                            }
//                        }
//                    }
//                    // final tests
//                    checkUnitRoots(context);
//                    checkMA(context);
//                    if (context.automodelling && !context.description.isMean() && Math.abs(rtval_) > 2.5) {
//                        if (checkMu_) {
//                            context.description.setMean(true);
//                        }
//                    }
//
//                    if (finalizer.estimate(context)) {
//                        break;
//                    }
//                    if (loop_ <= 2 && outliers != null) {
//                        outliers.reduceSelectivity();
//                        needOutliers_ = true;
//                        needAutoModelling_ = true;
//                    }
                    } while (round++ < 5);
                }
            } else {
//            estimator.estimate(context);
            }

            return context.build();

        } catch (Exception err) {
            return null;
        } finally {
            clear();
        }
    }
    

    private boolean isAutoModelling() {
        return differencing != null || arma != null;
    }

    private ProcessingResult checkMu(RegArimaModelling context, boolean initial) {
        if (!options.checkMu) {
            return ProcessingResult.Unchanged;
        }
        MeanController meanTest = new MeanController(initial
                ? MeanController.CVAL0 : MeanController.CVAL1);
        return meanTest.test(context);
    }

//    public static IParametricMapping<SarimaModel> createDefaultMapping(ModelDescription desc) {
//        SarimaComponent arima = desc.getArimaComponent();
//        if (arima.getFixedParametersCount() == 0) {
//            return new SarimaMapping(arima.getSpecification(), false);
//        } else {
//            return new SarimaFixedMapping(arima.getSpecification(), arima.getParameters(), arima.getFixedConstraints());
//        }
//    }
//    public TsPeriodSelector estimateSpan;
//    public IModelBuilder builder;
//    public ISeriesScaling scaling;
//    public ITsDataInterpolator missing;
//    public IPreprocessingModule loglevelTest;
//    public IPreprocessingModule tdTest;
//    public IPreprocessingModule easterTest;
//    public IPreprocessingModule userTest;
//    public IOutliersDetectionModule outliers;
//    public IPreprocessingModule autoModelling;
//    private IPreprocessingModule regressionTest0 = new RegressionVariablesTest(false, RegressionVariablesTest.CVAL, true);
//    public IPreprocessingModule regressionTest1 = new RegressionVariablesTest(RegressionVariablesTest.CVAL, RegressionVariablesTest.TSIG);
//    public IModelEstimator estimator;
//    public IModelEstimator finalizer = new FinalEstimator();
//    private double pcr_ = .95, cpcr_;
//    private double plbox_, rvr_, rtval_;
//    private int refsens_;
//    private int loop_, round_;
//    private boolean acceptAirline = false;
//    private PreprocessingModel reference_;
//    private static final double FCT = 1 / (1 - .0125), FCT2 = 1, MALIM = .001;
//    //private boolean needOutliers_;
//    //private boolean keepOutliers_;
//    private boolean checkMu_ = true;
//    private boolean needAutoModelling_, needOutliers_;
//    private boolean mixedModel_ = true;
//    public static final int MAXD = 2, MAXBD = 1;
//    private double ur_ = .95;
//
//    PreprocessingModel makeProcessing(ModellingContext context) {
//        // Step 1.
//        // Initial adjustments:
//        // - interpolation of the missing values
//        // - change of units [should always be done, to avoid "scale" effects]
//
//        // Step 2.
//        // Creates a "default model", which is an airline model (0 1 1)(0 1 1) with mean,
//        // except when no seasonal component is needed; the model is then
//        // (0 1 1)(0 0 0) with mean
//        // Step 3.
//        // Computes the default model with data in levels/logs
//        // The default model, without regression variables is always used.
//        // Step 4.
//        // Check for the presence of trading days/Easter/[others] and mean correction.
//        // The default model, without regression variables other than the tested ones
//        // is always used.
//        // Step 5.
//        // Complete the model with all the pre-specified regression variables
//        // and with any pre-specified arima model (or orders).
//        try {
//            clear();
//
//            builder.initialize(context);
//            // scaling
//            if (scaling != null) {
//                if (!scaling.process(context)) {
//                    return null;
//                }
//            }
//            // missing value...
//            if (missing != null) {
//                if (!context.description.updateMissing(missing)) {
//                    return null;
//                }
//            }
//            if (context.description.isFullySpecified() && outliers == null) {
//                // nothing to do
//                IParametricMapping<SarimaModel> mapping = context.description.defaultMapping();
//                ModelDescription model = context.description;
//                RegArimaModel<SarimaModel> regarima = model.buildRegArima();
//                RegArimaEstimator monitor = new RegArimaEstimator(mapping);
//                monitor.getMinimizer().setMaxIter(1);
//                monitor.optimize(regarima);
//                ModelEstimation estimation = new ModelEstimation(regarima, model.getLikelihoodCorrection());
//                estimation.computeLikelihood(mapping.getDim());
//                estimation.updateParametersCovariance(monitor.getParametersCovariance());
//                return new PreprocessingModel(model, estimation);
//            }
//
//            // step 1. Transformation
//            runTransformations(context);
//
//            regAIC(context);
//
//            checkMu(context, true);
//
//            estimator.estimate(context);
//
//            if (needOutliers_) {
//                ProcessingResult changed = outliers.process(context);
//                if (context.estimation == null) {
//                    estimator.estimate(context);
//                }
//                // Call pass0
//                // The original code of X13 doesn't call pass0 if automatic modelling is not used
//                // however, it should be done.
//                if (changed == ProcessingResult.Changed) {
//                    regressionTest0.process(context);
//                }
//            }
//            if (needAutoModelling_) {
//                if (context.estimation == null) {
//                    estimator.estimate(context);
//                }
//
//                ModelController controller = new ModelController();
//                boolean ok = controller.accept(context);
//                reference_ = context.current(false);
//                plbox_ = 1 - controller.getLjungBoxTest().getPValue();
//                rvr_ = controller.getRvr();
//                rtval_ = controller.getRTval();
//                if (!acceptAirline || !ok) {
//
//                    round_ = 1;
//                    loop_ = 1;
//                    do {
//                        boolean defModel = false;
//                        if (needAutoModelling_) {
//                            ProcessingResult result = execAutoModelling(context);
//                            defModel = result != ProcessingResult.Changed;
//                            if (!defModel) {
//                                context.description.setOutliers(null);
//                                context.estimation = null;
//                                needOutliers_ = outliers != null;
//                            }
//                            if (context.estimation == null) {
//                                estimator.estimate(context);
//                            }
//                            if (!defModel || loop_ > 1) {
//                                regAIC(context);
//                            }
//                        }
//                        if (needOutliers_) {
//                            outliers.process(context);
//                            if (context.estimation == null) {
//                                estimator.estimate(context);
//                            }
//                        }
//                        if (outliers != null && loop_ <= 2) {
//                            if (!pass2(defModel, context)) {
//                                continue;
//                            }
//                        }
//                        if (regressionTest1 != null) {
//                            ProcessingResult changed = regressionTest1.process(context);
//                            if (changed == ProcessingResult.Changed) {
//                                if (loop_ < 3) {
//                                    loop_ = 3;
//                                }
//                                if (context.estimation == null) {
//                                    estimator.estimate(context);
//                                }
//                            }
//                        }
//                        // final tests
//                        checkUnitRoots(context);
//                        checkMA(context);
//                        if (context.automodelling && !context.description.isMean() && Math.abs(rtval_) > 2.5) {
//                            if (checkMu_) {
//                                context.description.setMean(true);
//                            }
//                        }
//
//                        if (finalizer.estimate(context)) {
//                            break;
//                        }
//                        if (loop_ <= 2 && outliers != null) {
//                            outliers.reduceSelectivity();
//                            needOutliers_ = true;
//                            needAutoModelling_ = true;
//                        }
//                    } while (round_++ < 5);
//                }
//            } else {
//                estimator.estimate(context);
//            }
//
//            return context.current(true);
//
//        } catch (Exception err) {
//            return null;
//        } finally {
//            clear();
//        }
//    }
//
//    public boolean isCheckMu() {
//        return checkMu_;
//    }
//
//    public void setCheckMu(boolean check) {
//        if (check != checkMu_) {
//            checkMu_ = check;
//            regressionTest0 = new RegressionVariablesTest(false, RegressionVariablesTest.CVAL, check);
//        }
//    }
//
//    public boolean isMixed() {
//        return this.mixedModel_;
//    }
//
//    public void setMixed(boolean mixed) {
//        mixedModel_ = mixed;
//    }
//
//    public double getLjungBoxLimit() {
//        return this.pcr_;
//    }
//
//    public void setLjungBoxLimit(double val) {
//        this.pcr_ = val;
//    }
//
//    protected boolean runTransformations(ModellingContext context) {
//        // log/level...
//        if (loglevelTest != null) {
//            loglevelTest.process(context);
//        }
//        return true;
//    }
//
//    private ProcessingResult execAutoModelling(ModellingContext context) {
//        return autoModelling.process(context);
//    }
//
//    @Override
//    public PreprocessingModel process(TsData originalTs, ModellingContext context) {
//
//        if (context == null) {
//            context = new ModellingContext();
//        }
//        context.description = new ModelDescription(originalTs, estimateSpan == null ? null : originalTs.getDomain().select(estimateSpan));
//        initContext(context);
//
//        PreprocessingModel rslt = makeProcessing(context);
//        if (rslt != null) {
//            rslt.info_ = context.information;
//            rslt.addProcessingInformation(context.processingLog);
//        }
//        return rslt;
//    }
//
//    private void initContext(ModellingContext context) {
//        context.automodelling = autoModelling != null;
//        context.hasseas = context.description.getFrequency() > 1;
//    }
//
//    private void clear() {
//        loop_ = 0;
//        cpcr_ = pcr_;
//        refsens_ = 0;
//        reference_ = null;
//        plbox_ = 0;
//        rvr_ = 0;
//        rtval_ = 0;
//        needAutoModelling_ = autoModelling != null;
//        needOutliers_ = outliers != null;
//    }
//
//    private boolean pass2(boolean defModel, ModellingContext context) {
//        int ichk = 0;
//        int naut = context.description.getOutliers().size();
//        int naut0 = reference_.description.getOutliers().size();
//        SarimaSpecification spec0 = reference_.description.getSpecification(),
//                spec = context.description.getSpecification();
//        SarimaModel arima = context.estimation.getArima();
//        boolean mu0 = reference_.description.isEstimatedMean(),
//                mu = context.description.isEstimatedMean();
//        ModelController controller = new ModelController();
//        controller.accept(context);
//        double rvr = controller.getRvr();
//        double plbox = 1 - controller.getLjungBoxTest().getPValue();
//        if (naut0 <= naut && (!spec0.equals(spec) || mu0 != mu)) {
//            if (plbox < .95 && plbox_ < .75 && rvr_ < rvr) {
//                ichk = 1;
//            } else if (loop_ == 1 && plbox >= .95 && plbox_ < .95) {
//                ichk = 2;
//            } else if (plbox < .95 && plbox_ < 0.75 && plbox_ < plbox
//                    && rvr_ < FCT * rvr) {
//                ichk = 3;
//            } else if (plbox >= .95 && plbox_ < .95
//                    && rvr_ < FCT2 * rvr) {
//                ichk = 4;
//            } else if (spec.getD() == 0 && spec.getBD() == 1 && spec.getP() == 1
//                    && spec.getBP() == 0 && spec.getQ() == 1 && spec.getBQ() == 1
//                    && arima.phi(1) < -.82) {
//                ichk = 5;
//            } else if (spec.getD() == 1 && spec.getBD() == 0 && spec.getP() == 0
//                    && spec.getBP() == 1 && spec.getQ() == 1 && spec.getBQ() == 1
//                    && arima.bphi(1) < -.65) {
//                ichk = 6;
//            }
//        }
//        if (ichk > 0) {
//            context.description = reference_.description;
//            context.estimation = reference_.estimation;
//            plbox = plbox_;
//            rvr = rvr_;
//            defModel = true;
//        } else {
//            rtval_ = controller.getRTval();
//            rvr_ = rvr;
//            plbox_ = plbox;
//            reference_ = context.current(false);
//        }
//
//        if (loop_ == 1) {
//            cpcr_ += .025;
//        } else {
//            cpcr_ += .015;
//        }
//        ++loop_;
//        if (plbox <= cpcr_) {
//            return true;
//        }
//        boolean ncv = false;
//        if (loop_ == 2 && outliers != null) {
//            ncv = outliers.reduceSelectivity();
//            needOutliers_ = true;
//        }
//        needAutoModelling_ = !defModel;
//
//        if (loop_ > 2 || !ncv) {
//            lastSolution(context);
//            if (loop_ == 2) {
//                loop_ = 3;
//            }
//        }
//        return false;
//    }
//
//    // use the default model, clear outliers
//    private void lastSolution(ModellingContext context) {
//        SarimaSpecification nspec = context.description.getSpecification();
//        nspec.setP(3);
//        if (nspec.getBD() > 0 || nspec.getFrequency() == 1) {
//            nspec.setBP(0);
//        }
//        if (mixedModel_) {
//            nspec.setQ(1);
//        } else {
//            nspec.setQ(0);
//        }
//
//        if (nspec.getFrequency() > 1) {
//            nspec.setBQ(1);
//        }
//        context.description.setSpecification(nspec);
//        //context.description.setOutliers(null);
//        estimator.estimate(context);
//        needAutoModelling_ = false;
//        if (outliers != null) {
//            outliers.setSelectivity(0);
//            needOutliers_ = true;
//        }
//    }
//
    private ProcessingResult regAIC(RegArimaModelling context) {
        ProcessingResult rslt = ProcessingResult.Unchanged;
        if (calendarTest != null && calendarTest.test(context) == ProcessingResult.Changed) {
            rslt = ProcessingResult.Changed;
        }
        if (easterTest != null && easterTest.test(context) == ProcessingResult.Changed) {
            rslt = ProcessingResult.Changed;
        }
//        if (userTest != null && userTest.process(context) == ProcessingResult.Changed) {
//            rslt = ProcessingResult.Changed;
//        }
        return rslt;
    }
//
//    private ProcessingResult checkMu(ModellingContext context, boolean initial) {
//        if (!checkMu_) {
//            return ProcessingResult.Unchanged;
//        }
//        MeanController meanTest = new MeanController(initial
//                ? MeanController.CVAL0 : MeanController.CVAL1);
//        return meanTest.process(context);
//    }
//
//    private boolean reduceModel(ModellingContext context) {
//        return false;
//    }
//
//    private void checkUnitRoots(ModellingContext context) {
//
//        //quasi-unit roots of ar are changed in true unit roots
//        SarimaModel m = context.estimation.getArima();
//        SarimaSpecification nspec = m.getSpecification();
//
//        boolean ok = true;
//        if (nspec.getP() > 0 && nspec.getD() < MAXD) {
//            if (0 != searchur(m.getRegularAR().mirror().roots())) {
//                nspec.setP(nspec.getP() - 1);
//                nspec.setD(nspec.getD() + 1);
//                ok = false;
//            }
//        }
//        if (nspec.getBP() > 0 && nspec.getBD() < MAXBD) {
//            if (0 != searchur(m.getSeasonalAR().mirror().roots())) {
//                nspec.setBP(nspec.getBP() - 1);
//                nspec.setBD(nspec.getBD() + 1);
//                ok = false;
//            }
//        }
//        if (!ok) {
//            context.description.setSpecification(nspec);
//            redoEstimation(context);
//        }
//    }
//
//    private void redoEstimation(ModellingContext context) {
//        estimator.estimate(context);
//        // check mean
//        if (context.description.isEstimatedMean()) {
//            checkMu(context, false);
//        }
//        if (!context.description.getOutliers().isEmpty()) {
//            context.description.setOutliers(null);
//            context.estimation = null;
//        }
//        if (context.estimation == null) {
//            estimator.estimate(context);
//        }
//        if (outliers != null) {
//            outliers.process(context);
//        }
//        if (context.estimation == null) {
//            estimator.estimate(context);
//        }
//        ModelController controller = new ModelController();
//        controller.accept(context);
//        rtval_ = controller.getRTval();
//        rvr_ = controller.getRvr();
//        plbox_ = 1 - controller.getLjungBoxTest().getPValue();
//        reference_ = context.current(false);
//
//    }
//
//    private int searchur(final Complex[] r) {
//        if (r == null) {
//            return 0;
//        }
//        int n = 0;
//        for (int i = 0; i < r.length; ++i) {
//            double cdim = Math.abs(r[i].getIm());
//            double vcur = r[i].abs();
//            if (vcur > ur_ && cdim <= 0.05 && r[i].getRe() > 0) {
//                ++n;
//            }
//        }
//        return n;
//    }
//
//    private void checkMA(ModellingContext context) {
//        SarimaModel m = context.estimation.getArima();
//        SarimaSpecification nspec = m.getSpecification();
//        if (nspec.getQ() == 0 || nspec.getD() == 0) {
//            return;
//        }
//        double ma = m.getRegularMA().evaluateAt(1);
//        if (Math.abs(ma) < MALIM) {
//            nspec.setQ(nspec.getQ() - 1);
//            nspec.setD(nspec.getD() - 1);
//            context.description.setSpecification(nspec);
//            context.description.setMean(true);
//            redoEstimation(context);
//        }
//    }
}
