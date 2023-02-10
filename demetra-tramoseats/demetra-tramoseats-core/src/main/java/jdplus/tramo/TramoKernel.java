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
package jdplus.tramo;

import jdplus.tramo.internal.TramoUtility;
import nbbrd.design.Development;
import demetra.modelling.TransformationType;
import demetra.timeseries.regression.ModellingContext;
import jdplus.regsarima.regular.IRegressionModule;
import jdplus.regsarima.regular.SeasonalityDetector;
import jdplus.regsarima.regular.ModelDescription;
import jdplus.regsarima.regular.RegSarimaModelling;
import demetra.arima.SarimaOrders;
import jdplus.regsarima.regular.RegSarimaProcessor;
import demetra.processing.ProcessingLog;
import demetra.timeseries.TsData;
import jdplus.regsarima.regular.ProcessingResult;
import jdplus.sarima.SarimaModel;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.regression.ILengthOfPeriodVariable;
import demetra.timeseries.regression.ITradingDaysVariable;
import demetra.tramo.AutoModelSpec;
import demetra.tramo.EasterSpec;
import demetra.tramo.OutlierSpec;
import demetra.tramo.TradingDaysSpec;
import demetra.tramo.TramoSpec;
import demetra.tramo.TransformSpec;
import jdplus.tramo.internal.ArmaModule;
import jdplus.tramo.internal.DifferencingModule;
import jdplus.tramo.internal.OutliersDetectionModule;
import java.util.ArrayList;
import java.util.List;
import demetra.timeseries.regression.ModellingUtility;
import demetra.tramo.RegressionTestType;
import jdplus.regsarima.regular.RegSarimaModel;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class TramoKernel implements RegSarimaProcessor {

    private static final String TRAMO = "tramo";

    @lombok.Value
    @lombok.Builder
    public static class AmiOptions {

        boolean checkMu;
        double precision, intermediatePrecision, ur;
        double va;
        double reduceVa;
        double ljungBoxLimit;
        boolean acceptAirline;

        public static Builder builder() {
            return new Builder()
                    .intermediatePrecision(1e-5)
                    .precision(1e-7)
                    .ur(.96)
                    .reduceVa(.12)
                    .ljungBoxLimit(.95);
        }
    }

    public static TramoKernel of(TramoSpec spec, ModellingContext context) {
        return new TramoKernel(spec, context);
    }

    private final TramoSpec spec;
    private final ModellingContext modellingContext;
    private final AmiOptions options;
    private final TramoContext context = new TramoContext();

    private final SeasonalityController scontroller;
    private final List<ModelController> controllers = new ArrayList<>();

    private RegSarimaModelling refAirline, refAuto;
    private ModelStatistics refStats;

    private int pass = 0, round = 0;
    private double curva = 0, pcr = 0;
    private boolean pass3;
    private boolean needOutliers;
    private boolean needAutoModelling;

    private TramoKernel(TramoSpec spec, ModellingContext context) {
        this.spec = spec;
        this.modellingContext = context;
        this.options = readAmiOptions(spec);
        TradingDaysSpec td = spec.getRegression().getCalendar().getTradingDays();

        if (spec.isUsingAutoModel()) {
            scontroller = new SeasonalityController();
            controllers.add(new RegularUnderDifferencingTest());
            controllers.add(new SeasonalUnderDifferencingTest());
            if (spec.getAutoModel().isAmiCompare()) {
                controllers.add(new ModelBenchmarking());
            }
            if (td.isAutomatic()) {
                controllers.add(new TradingDaysController(
                        TramoModelBuilder.td(spec, DayClustering.TD2, modellingContext), td.getProbabilityForFTest()));
            }
            controllers.add(new SeasonalUnderDifferencingTest2());
            controllers.add(new RegularUnderDifferencingTest2());
        } else {
            scontroller = null;
            if (td.isAutomatic()) {
                controllers.add(new TradingDaysController(
                        TramoModelBuilder.td(spec, DayClustering.TD2, modellingContext), td.getProbabilityForFTest()));
            }
        }
    }

    private static AmiOptions readAmiOptions(TramoSpec spec) {
        AutoModelSpec ami = spec.getAutoModel();
        return AmiOptions.builder()
                .precision(spec.getEstimate().getTol())
                .ur(spec.getEstimate().getUbp())
                .va(spec.getOutliers().getCriticalValue())
                .reduceVa(ami.getPc())
                .checkMu(spec.isUsingAutoModel() || spec.getRegression().getMean().isTest())
                .ljungBoxLimit(ami.getPcr())
                .acceptAirline(ami.isAcceptDefault())
                .build();
    }

    private ITradingDaysVariable[] alltd() {
        return new ITradingDaysVariable[]{
            TramoModelBuilder.td(spec, DayClustering.TD2, modellingContext),
            TramoModelBuilder.td(spec, DayClustering.TD2c, modellingContext),
            TramoModelBuilder.td(spec, DayClustering.TD3, modellingContext),
            TramoModelBuilder.td(spec, DayClustering.TD3c, modellingContext),
            TramoModelBuilder.td(spec, DayClustering.TD4, modellingContext),
            TramoModelBuilder.td(spec, DayClustering.TD7, modellingContext)
        };
    }

    private ITradingDaysVariable[] nestedtd() {
        return new ITradingDaysVariable[]{
            TramoModelBuilder.td(spec, DayClustering.TD2, modellingContext),
            TramoModelBuilder.td(spec, DayClustering.TD3, modellingContext),
            TramoModelBuilder.td(spec, DayClustering.TD4, modellingContext),
            TramoModelBuilder.td(spec, DayClustering.TD7, modellingContext)
        };
    }

    private IRegressionModule regressionModule(boolean preadjusted) {
        TradingDaysSpec tdspec = spec.getRegression().getCalendar().getTradingDays();
        EasterSpec espec = spec.getRegression().getCalendar().getEaster();
        ILengthOfPeriodVariable lp = preadjusted ? null : TramoModelBuilder.leapYear(tdspec);
        if (tdspec.isAutomatic()) {
            switch (tdspec.getAutomaticMethod()) {
                case FTEST:
                    return AutomaticFRegressionTest.builder()
                            .easter(espec.isTest() ? TramoModelBuilder.easter(spec) : null)
                            .leapYear(lp)
                            .adjust(tdspec.isAutoAdjust())
                            .tradingDays(TramoModelBuilder.td(spec, DayClustering.TD7, modellingContext))
                            .workingDays(TramoModelBuilder.td(spec, DayClustering.TD2, modellingContext))
                            .testMean(options.isCheckMu())
                            .fPValue(tdspec.getProbabilityForFTest())
                            .estimationPrecision(options.intermediatePrecision)
                            .build();
                case AIC: {
                    return AutomaticRegressionTest.builder()
                            .easter(espec.isTest() ? TramoModelBuilder.easter(spec) : null)
                            .leapYear(lp)
                            .tradingDays(alltd())
                            .testMean(options.isCheckMu())
                            .estimationPrecision(options.intermediatePrecision)
                            .adjust(tdspec.isAutoAdjust())
                            .aic()
                            .build();
                }
                case BIC: {
                    return AutomaticRegressionTest.builder()
                            .easter(espec.isTest() ? TramoModelBuilder.easter(spec) : null)
                            .leapYear(lp)
                            .adjust(tdspec.isAutoAdjust())
                            .tradingDays(alltd())
                            .testMean(options.isCheckMu())
                            .estimationPrecision(options.intermediatePrecision)
                            .bic()
                            .build();
                }
                default:
                    return AutomaticWaldRegressionTest.builder()
                            .easter(espec.isTest() ? TramoModelBuilder.easter(spec) : null)
                            .leapYear(lp)
                            .tradingDays(nestedtd())
                            .testMean(options.isCheckMu())
                            .estimationPrecision(options.intermediatePrecision)
                            .pconstraint(0.1)
                            .pmodel(tdspec.getProbabilityForFTest())
                            .adjust(tdspec.isAutoAdjust())
                            .build();
            }
        } else {
            return DefaultRegressionTest.builder()
                    .easter(espec.isTest() ? TramoModelBuilder.easter(spec) : null)
                    .leapYear(tdspec.isTest() ? lp : null)
                    .tradingDays(tdspec.isTest() ? TramoModelBuilder.tradingDays(spec, modellingContext) : null)
                    .useJoinTest(tdspec.getRegressionTestType() == RegressionTestType.Joint_F)
                    .testMean(options.isCheckMu())
                    .estimationPrecision(options.intermediatePrecision)
                    .adjust(tdspec.isAutoAdjust())
                    .build();
        }
    }

    private OutliersDetectionModule outliersModule() {
        OutlierSpec outliers = spec.getOutliers();
        return OutliersDetectionModule.builder()
                .ao(outliers.isAo())
                .ls(outliers.isLs())
                .tc(outliers.isTc())
                .so(outliers.isSo())
                .span(outliers.getSpan())
                .tcrate(outliers.getDeltaTC())
                .maximumLikelihood(outliers.isMaximumLikelihood())
                .precision(options.intermediatePrecision)
                .build();
    }

    private OutliersDetectionModule robustOutliersModule() {
        return OutliersDetectionModule.builder()
                .ao(true)
                .ls(true)
                .precision(1e-3)
                .build();
    }

    private DifferencingModule differencingModule() {
        AutoModelSpec amiSpec = spec.getAutoModel();
        return DifferencingModule.builder()
                .cancel(amiSpec.getCancel())
                .ub1(amiSpec.getUb1())
                .ub2(amiSpec.getUb2())
                .seasonal(context.seasonal)
                .precision(options.intermediatePrecision)
                .initial(round == 1)
                .build();
    }

    private ArmaModule armaModule() {
        return ArmaModule.builder()
                .seasonal(context.seasonal)
                .build();
    }

    @Override
    public RegSarimaModel process(TsData originalTs, ProcessingLog log) {
        if (log == null) {
            log = ProcessingLog.dummy();
        }
        log.push(TRAMO);
        ModelDescription desc = build(originalTs, log);
        if (desc == null) {
            throw new TramoException("Initialization failed");
        }
        RegSarimaModelling modelling = RegSarimaModelling.of(desc, log);
        RegSarimaModel rslt = ami(modelling, log);
        log.pop();

        return rslt;
    }

    private ModelDescription build(TsData originalTs, ProcessingLog log) {
        TramoModelBuilder builder = new TramoModelBuilder(spec, modellingContext);
        return builder.build(originalTs, log);
    }

    private boolean isFullySpecified() {
        // Nothing to do.
        return !(this.spec.getTransform().getFunction() == TransformationType.Auto
                || this.isAutoModelling()
                || this.isOutliersDetection()
                || this.spec.getRegression().getMean().isTest()
                || this.spec.getRegression().getCalendar().getTradingDays().isTest()
                || this.spec.getRegression().getCalendar().getTradingDays().isAutomatic()
                || this.spec.getRegression().getCalendar().getEaster().isTest());

    }

    private RegSarimaModel ami(RegSarimaModelling modelling, ProcessingLog log) {

        if (isFullySpecified()) {
            modelling.estimate(options.precision);
            return modelling.build();
        }

        // Test the seasonality
        testSeasonality(modelling);

        // Test for loglevel transformation
        testTransformation(modelling);

        regressionModule(modelling.getDescription().isAdjusted()).test(modelling);

        initProcessing(modelling.getDescription().regarima().getActualObservationsCount());

        int iter = 0;
        do {
            ++iter;
        } while (iter < 10 && !iterate(modelling));

        return modelling.build();
    }

    private void initProcessing(int n) {
        round = 0;
        needOutliers = isOutliersDetection();
        needAutoModelling = false;
        pass = 0;
        pass3 = false;
        // initialize some internal variables
        if (this.isOutliersDetection()) {
            curva = options.getVa();
            if (curva == 0) {
                curva = TramoUtility.calcCv(n);
            }
        }
        pcr = options.ljungBoxLimit;
        refAuto = null;
        refStats = null;
        refAirline = null;
        if (scontroller != null) {
            scontroller.setReferenceModel(null);
        }
        controllers.forEach(c -> c.setReferenceModel(null));
    }

    private boolean reduceVa() {
        if (curva == TramoUtility.MINCV) {
            return false;
        }
        curva = Math.max(TramoUtility.MINCV, curva * (1 - options.reduceVa));
        return true;
    }

    /**
     *
     * @param modelling
     * @return True if the model doesn't need further iteration
     */
    private boolean iterate(RegSarimaModelling modelling) {

        if (modelling.needEstimation()) {
            modelling.estimate(options.getIntermediatePrecision());
        }

        ModelDescription desc = modelling.getDescription();
        boolean changed = false;
        SarimaOrders curspec = desc.specification();
        boolean curMean = desc.isMean();
        if (needDifferencing(desc)) {
            changed = execDifferencing(modelling) == ProcessingResult.Changed;
            SarimaOrders nspec = desc.specification();
            if (pass == 1 && nspec.getDifferenceOrder() != curspec.getDifferenceOrder()) {
                desc.removeVariable(var -> ModellingUtility.isOutlier(var, true));
                modelling.clearEstimation();
            }
        }
        if (needAutoModelling(desc)) {
            execAutoModelling(modelling);
            desc = modelling.getDescription();
            changed = (!desc.specification().equals(curspec))
                    || desc.isMean() != curMean;
        }
        if (needOutliers(desc)) {

            if (modelling.getDescription().removeVariable(var -> ModellingUtility.isOutlier(var, true))) {
                modelling.clearEstimation();
            }
            ProcessingResult rslt = execOutliers(modelling);
            changed = changed || rslt == ProcessingResult.Changed;
        }
//        context.estimate(options.precision);

        if (!estimateModel(modelling)) {
            needOutliers = isOutliersDetection();
            needAutoModelling = isAutoModelling();
            ++round;
            ++pass;
            return false;
        }

        if (round == 0 && (isAutoModelling() || isOutliersDetection())) {
            needOutliers = isOutliersDetection();
            needAutoModelling = isAutoModelling();
            ++round;
            ++pass;
            refAirline = RegSarimaModelling.copyOf(modelling);
            refAuto = RegSarimaModelling.copyOf(modelling);
            refStats = ModelStatistics.of(refAuto.getDescription(), refAuto.getEstimation().getConcentratedLikelihood());
            double lb = refStats.getLjungBoxPvalue();
            return options.acceptAirline && (1 - lb) < options.ljungBoxLimit;
        }
        if (pass <= 3 && !pass3 && isAutoModelling()) {
            if (!pass2(!changed, modelling)) {
                return false;
            }
        }

        if (!testRegression(modelling, spec.getAutoModel().getTsig())) {
            pass = 4;
            needAutoModelling = false;
            needOutliers = false;
            return false;
        }

//
        ModelEstimator estimator = new ModelEstimator(options.precision, curva, outliersModule());
//
        if (spec.isUsingAutoModel()) {
            scontroller.setEstimator(estimator);
            if (scontroller.process(modelling, context) == ProcessingResult.Changed) {
                if (!pass3) {
                    pass3 = true;
                    pass = 1;
                    needAutoModelling = true;
                    needOutliers = isOutliersDetection();
                    return false;
                }
            }
        }
        for (ModelController controller : controllers) {
            controller.setEstimator(estimator);
            controller.setReferenceModel(refAuto);
            controller.process(modelling, context);
        }
        return true;
    }

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
    private boolean needDifferencing(ModelDescription desc) {
        // round=0 : NO DIFFERENCING (use airline)
        // round=1 
        // The series is corrected for outliers observed in round 0
        // We start from differencing = (0,0)
        // Always differencing
        // round=2
        // The series is corrected for outliers observed in round 1
        // We start from current differencing
        // we will not compute differencing if it is already max (2,1) or if
        // we didn't find any outlier at the end of round 0 and 1 (same problem)
        if (!needAutoModelling) {
            return false;
        }
        if (round == 2 && !desc.variables().anyMatch(var -> ModellingUtility.isOutlier(var, true))) {
            return false;
        }
        SarimaOrders curspec = desc.specification();
//        return !ModelDescription.sameVariables(refAirline.getDescription(), desc);
        return curspec.getD() < 2 || curspec.getBd() < 1;
    }

    private boolean isAutoModelling() {
        return spec.isUsingAutoModel();
    }

    private boolean isOutliersDetection() {
        return spec.getOutliers().isUsed();
    }

    private boolean needOutliers(ModelDescription desc) {

        if (!isOutliersDetection()) {
            return false;
        }
        return needOutliers;
    }

    private boolean needAutoModelling(ModelDescription desc) {
        if (!needAutoModelling) {
            return false;
        }
        if (round == 2 && !desc.variables().anyMatch(var -> ModellingUtility.isOutlier(var, true))) {
            return false;
        }
        // Should be completed
        return true;
    }

    private ProcessingResult execDifferencing(RegSarimaModelling context) {
        return differencingModule().process(context);
    }

    private ProcessingResult execAutoModelling(RegSarimaModelling context) {
        ProcessingResult rslt = armaModule().process(context);
        ModelDescription desc = context.getDescription();
        SarimaOrders curspec = desc.specification();
        if (curspec.getParametersCount() == 0 && pass >= 3) {
            curspec.setQ(1);
            desc.setSpecification(curspec);
        }
        return rslt;
    }

    private ProcessingResult execOutliers(RegSarimaModelling context) {
        return outliersModule().process(context, curva);
    }

    private void restore(RegSarimaModelling context) {
        context.set(ModelDescription.copyOf(refAuto.getDescription()), refAuto.getEstimation());
    }
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

    protected boolean pass2(final boolean same, RegSarimaModelling context) {
        double fct = 1, fct2 = 1;
        boolean useprev = false;
        SarimaModel curmodel = context.getDescription().arima();
        SarimaOrders curspec = curmodel.orders();

        ModelStatistics stats = ModelStatistics.of(context.getDescription(), context.getEstimation().getConcentratedLikelihood());
        double plbox = 1 - stats.getLjungBoxPvalue();
        double rvr = stats.getSe();
        if (refAuto != null) {
            double plbox0 = 1 - refStats.getLjungBoxPvalue();
            double rvr0 = refStats.getSe();
            int nout = context.getDescription().countRegressors(var -> ModellingUtility.isOutlier(var, true));
            int refout = refAuto.getDescription().countRegressors(var -> ModellingUtility.isOutlier(var, true));
//            addModelInfo(stats, context, false);
            if (refout <= nout && ((plbox < .95 && plbox0 < .75 && rvr0 < rvr)
                    // 1. the previous model was significantly better
                    || (pass == 1 && plbox >= .95 && plbox0 < .95)
                    // 2. no improvement
                    || (plbox < .95 && plbox0 < .75 && plbox0 < plbox && rvr0 < fct * rvr)
                    // 3.
                    || (plbox >= .95 && plbox0 < .95 && rvr0 < fct2 * rvr)
                    // 4. degradation
                    || (curspec.getD() == 0 && curspec.getBd() == 1 && curspec.getP() == 1
                    && curmodel.phi(1) <= -.82 && curspec.getQ() <= 1
                    && curspec.getBp() == 0 && curspec.getBq() == 1)
                    //quasi airline model
                    || (curspec.getD() == 1 && curspec.getBd() == 0 && curspec.getP() == 0
                    && curspec.getQ() == 1 && curspec.getBp() == 1
                    && curmodel.bphi(1) <= -.65 && curspec.getBq() <= 1))) {
                useprev = true;
            }
            if (!useprev) {
                refAuto = RegSarimaModelling.copyOf(context);
                refStats = stats;
            } else {
                restore(context);
                plbox = plbox0;
            }
        } else {
            refAuto = RegSarimaModelling.copyOf(context);
            refStats = stats;
        }

        if (pass == 1) {
            pcr += .025;
        } else if (pass >= 2) {
            pcr += .015;
        }

        if (plbox <= pcr) {
            return true;
        }

        if (pass == 1 && isOutliersDetection()) {
            reduceVa();
        }

        ++round;
        ++pass;

        if (pass <= 2) {
            needAutoModelling = !same;
            needOutliers = isOutliersDetection();
        } else {
            lastSolution(context);
            needAutoModelling = false;
        }
        return false;
    }

    // use the default model, clear outliers
    private void lastSolution(RegSarimaModelling modelling) {
        ModelDescription desc = modelling.getDescription();
        SarimaOrders nspec = desc.specification();
        nspec.setP(3);
        if (nspec.getBd() > 0) {
            nspec.setBp(0);
        }
        nspec.setQ(1);
        if (context.seasonal) {
            nspec.setBq(1);
        }
        desc.removeVariable(var -> ModellingUtility.isOutlier(var, true));
        modelling.setSpecification(nspec);
//        addArmaHistory(context);
        round = 1;
        needOutliers = isOutliersDetection();
        needAutoModelling = false;
    }
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

    private static final String SEAS = "seasonality test";

    private void testSeasonality(RegSarimaModelling modelling) {
        ModelDescription model = modelling.getDescription();
        if (!isAutoModelling()) {
            context.seasonal = model.specification().isSeasonal();
            return;
        }

        int period = model.getAnnualFrequency();
        if (period > 1) {

            TramoSeasonalityDetector seas = new TramoSeasonalityDetector();
            SeasonalityDetector.Seasonality s = seas.hasSeasonality(model.getTransformedSeries().getValues(), period);
            context.originalSeasonalityTest = s.toInt();
            if (context.originalSeasonalityTest < 2) {
                SarimaOrders nspec = SarimaOrders.m011(period);
                model.setSpecification(nspec);
                context.seasonal = false;
            } else {
                context.seasonal = true;
            }
            ProcessingLog log = modelling.getLog();
            if (log != null) {
                log.step(SEAS, context.originalSeasonalityTest);
            }
        } else {
            context.seasonal = false;
        }
    }

    private void testTransformation(RegSarimaModelling modelling) {
        TransformSpec tspec = spec.getTransform();
        if (tspec.getFunction() == TransformationType.Auto) {
            boolean toClean = false;
            if (tspec.isOutliersCorrection()) {
                OutliersDetectionModule outliers = robustOutliersModule();
                ProcessingResult rslt = outliers.process(modelling, 5);
                toClean = rslt == ProcessingResult.Changed;
            }
            LogLevelModule module = LogLevelModule.builder()
                    .logPreference(Math.log(tspec.getFct()))
                    .estimationPrecision(options.intermediatePrecision)
                    .seasonal(context.seasonal)
                    .build();
            module.process(modelling);
            ModelDescription desc = modelling.getDescription();
//            TradingDaysSpec td = spec.getRegression().getCalendar().getTradingDays();
//            if (desc.isLogTransformation()
//                    && td.isAutoAdjust()) {
//                desc.setPreadjustment(td.getLengthOfPeriodType());
//                desc.remove("lp");
//                modelling.clearEstimation();
//            }
            if (toClean) {
                if (desc.removeVariable(var -> ModellingUtility.isOutlier(var, true))) {
                    modelling.clearEstimation();
                }
            }
        } else if (modelling.getDescription().isLogTransformation()) {
            if (modelling.getDescription().getSeries().getValues().anyMatch(x -> x <= 0)) {
                modelling.getLog().warning("logs changed to levels");
                modelling.getDescription().setLogTransformation(false);
                modelling.getDescription().setPreadjustment(LengthOfPeriodType.None);
                modelling.clearEstimation();
            }
        }
    }

    private boolean estimateModel(RegSarimaModelling context) {

        FinalEstimator estimator = FinalEstimator.builder()
                .precision(options.precision)
                .unitRootThreshold(options.ur)
                .pass(pass)
                .ami(isAutoModelling())
                .outliers(isOutliersDetection())
                .build();
        int niter = 0;
        do {
            if (!estimator.estimate(context)) {
                if (pass == 1 && this.needOutliers(context.getDescription())
                        && this.needAutoModelling(context.getDescription())) {
                    reduceVa();
                }
                return false;
            }
            if (pass3 || pass != 0) {
                return true;
            }
        } while (niter++ < 5 && !testRegression(context, FastRegressionTest.CVAL));
        return true;
    }

    private boolean testRegression(RegSarimaModelling context, double tmean) {
        FastRegressionTest regtest = FastRegressionTest.builder()
                .testMean(options.checkMu)
                .meanThreshold(tmean)
                .build();
        return regtest.test(context) == ProcessingResult.Unchanged;
    }

}
