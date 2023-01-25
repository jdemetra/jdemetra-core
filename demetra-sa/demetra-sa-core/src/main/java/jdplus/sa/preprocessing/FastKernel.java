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
package jdplus.sa.preprocessing;

import demetra.DemetraException;
import nbbrd.design.Development;
import demetra.modelling.TransformationType;
import demetra.timeseries.regression.ModellingContext;
import jdplus.regsarima.regular.IRegressionModule;
import jdplus.regsarima.regular.SeasonalityDetector;
import jdplus.regsarima.regular.ModelDescription;
import jdplus.regsarima.regular.RegSarimaModelling;
import demetra.arima.SarimaOrders;
import demetra.modelling.regular.EasterSpec;
import demetra.modelling.regular.ModellingSpec;
import demetra.modelling.regular.OutlierSpec;
import demetra.modelling.regular.TradingDaysSpec;
import demetra.modelling.regular.TransformSpec;
import jdplus.regsarima.regular.RegSarimaProcessor;
import demetra.processing.ProcessingLog;
import demetra.timeseries.TsData;
import jdplus.regsarima.regular.ProcessingResult;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.regression.ILengthOfPeriodVariable;
import demetra.timeseries.regression.ITradingDaysVariable;
import demetra.timeseries.regression.ModellingUtility;
import jdplus.regsarima.regular.RegSarimaModel;

///**
// *
// * @author Jean Palate
// */
//@Development(status = Development.Status.Preliminary)
//public class FastKernel implements RegSarimaProcessor {
//
//    private static final String DEMETRA = "demetra";
//
//    @lombok.Value
//    @lombok.Builder
//    public static class AmiOptions {
//
//        boolean checkMu;
//        double va;
//        double precision, intermediatePrecision;
//
//        public static Builder builder() {
//            return new Builder()
//                    .intermediatePrecision(1e-5)
//                    .precision(1e-7);
//        }
//    }
//
//    public static FastKernel of(ModellingSpec spec, ModellingContext context) {
//        return new FastKernel(spec, context);
//    }
//
//    private final ModellingSpec spec;
//    private final ModellingContext modellingContext;
//    private final AmiOptions options;
//
//    private double curva = 0;
//
//    private FastKernel(ModellingSpec spec, ModellingContext context) {
//        this.spec = spec;
//        this.modellingContext = context;
//        this.options = readAmiOptions(spec);
//        TradingDaysSpec td = spec.getRegression().getCalendar().getTradingDays();
//    }
//
//    private static AmiOptions readAmiOptions(ModellingSpec spec) {
//        return AmiOptions.builder()
//                .precision(spec.getEstimate().getPrecision())
//                .va(spec.getOutliers().getCriticalValue())
//                .checkMu(spec.getRegression().isCheckMu())
//                .build();
//    }
//
//    private ITradingDaysVariable[] alltd() {
//        return new ITradingDaysVariable[]{
//            ModelBuilder.td(spec, DayClustering.TD2, modellingContext),
//            ModelBuilder.td(spec, DayClustering.TD2c, modellingContext),
//            ModelBuilder.td(spec, DayClustering.TD3, modellingContext),
//            ModelBuilder.td(spec, DayClustering.TD3c, modellingContext),
//            ModelBuilder.td(spec, DayClustering.TD4, modellingContext),
//            ModelBuilder.td(spec, DayClustering.TD7, modellingContext)
//        };
//    }
//
//    private ITradingDaysVariable[] nestedtd() {
//        return new ITradingDaysVariable[]{
//            ModelBuilder.td(spec, DayClustering.TD2, modellingContext),
//            ModelBuilder.td(spec, DayClustering.TD3, modellingContext),
//            ModelBuilder.td(spec, DayClustering.TD4, modellingContext),
//            ModelBuilder.td(spec, DayClustering.TD7, modellingContext)
//        };
//    }
//
//    private IRegressionModule regressionModule(boolean preadjusted) {
//        TradingDaysSpec tdspec = spec.getRegression().getCalendar().getTradingDays();
//        EasterSpec espec = spec.getRegression().getCalendar().getEaster();
//        ILengthOfPeriodVariable lp = preadjusted ? null : ModelBuilder.leapYear(tdspec);
//        if (tdspec.isAutomatic()) {
//            switch (tdspec.getAutomaticMethod()) {
//                case AIC -> {
//                    return AutomaticICTD.builder()
//                            .easter(espec.isTest() ? ModelBuilder.easter(spec) : null)
//                            .leapYear(lp)
//                            .tradingDays(alltd())
//                            .testMean(spec.getRegression().isCheckMu())
//                            .estimationPrecision(options.intermediatePrecision)
//                            .adjust(tdspec.isAutoAdjust())
//                            .aic()
//                            .build();
//                }
//                case BIC -> {
//                    return AutomaticICTD.builder()
//                            .easter(espec.isTest() ? ModelBuilder.easter(spec) : null)
//                            .leapYear(lp)
//                            .adjust(tdspec.isAutoAdjust())
//                            .tradingDays(alltd())
//                            .testMean(spec.getRegression().isCheckMu())
//                            .estimationPrecision(options.intermediatePrecision)
//                            .bic()
//                            .build();
//                }
//                default -> {
//                    return AutomaticWaldTD.builder()
//                            .easter(espec.isTest() ? ModelBuilder.easter(spec) : null)
//                            .leapYear(lp)
//                            .tradingDays(nestedtd())
//                            .testMean(spec.getRegression().isCheckMu())
//                            .estimationPrecision(options.intermediatePrecision)
//                            .pconstraint(0.1)
//                            .pmodel(tdspec.getProbabilityForFTest())
//                            .adjust(tdspec.isAutoAdjust())
//                            .build();
//                }
//            }
//        } else {
//            return DefaultRegressionTest.builder()
//                    .easter(espec.isTest() ? ModelBuilder.easter(spec) : null)
//                    .leapYear(tdspec.isTest() ? lp : null)
//                    .tradingDays(tdspec.isTest() ? ModelBuilder.tradingDays(spec, modellingContext) : null)
//                    .useJoinTest(true)
//                    .testMean(spec.getRegression().isCheckMu())
//                    .estimationPrecision(options.intermediatePrecision)
//                    .adjust(tdspec.isAutoAdjust())
//                    .build();
//        }
//    }
//
//    private OutliersDetectionModule outliersModule() {
//        OutlierSpec outliers = spec.getOutliers();
//        return OutliersDetectionModule.builder()
//                .ao(outliers.isAo())
//                .ls(outliers.isLs())
//                .tc(outliers.isTc())
//                .so(outliers.isSo())
//                .span(outliers.getSpan())
//                .tcrate(outliers.getDeltaTC())
//                .precision(options.intermediatePrecision)
//                .build();
//    }
//
//    private OutliersDetectionModule robustOutliersModule() {
//        return OutliersDetectionModule.builder()
//                .ao(true)
//                .ls(true)
//                .precision(1e-3)
//                .build();
//    }
//
//    @Override
//    public RegSarimaModel process(TsData originalTs, ProcessingLog log) {
//        if (log == null) {
//            log = ProcessingLog.dummy();
//        }
//        log.push(DEMETRA);
//        ModelDescription desc = build(originalTs, log);
//        if (desc == null) {
//            throw new DemetraException("Initialization failed");
//        }
//        RegSarimaModelling modelling = RegSarimaModelling.of(desc, log);
//        RegSarimaModel rslt = ami(modelling, log);
//        log.pop();
//
//        return rslt;
//    }
//
//    private ModelDescription build(TsData originalTs, ProcessingLog log) {
//        ModelBuilder builder = new ModelBuilder(spec, modellingContext);
//        return builder.build(originalTs, log);
//    }
//
//    private boolean isFullySpecified() {
//        // Nothing to do.
//        return this.spec.getTransform().getFunction() != TransformationType.Auto
//                && !this.isOutliersDetection()
//                && this.spec.getRegression().isSpecified();
//    }
//
//    private RegSarimaModel ami(RegSarimaModelling modelling, ProcessingLog log) {
//
//        if (isFullySpecified()) {
//            modelling.estimate(options.precision);
//            return modelling.build();
//        }
//
//        // Test the seasonality
//        testSeasonality(modelling);
//
//        // Test for loglevel transformation
//        testTransformation(modelling);
//
//        regressionModule(modelling.getDescription().isAdjusted()).test(modelling);
//
//        initProcessing(modelling.getDescription().regarima().getActualObservationsCount());
//
//        int iter = 0;
//        do {
//            ++iter;
//        } while (iter < 10 && !iterate(modelling));
//
//        return modelling.build();
//    }
//
//    private void initProcessing(int n) {
//        // initialize some internal variables
//        if (this.isOutliersDetection()) {
//            curva = options.getVa();
//            if (curva == 0) {
//                curva = DemetraUtility.calcCv(n);
//            }
//        }
//    }
//
//    /**
//     *
//     * @param modelling
//     * @return True if the model doesn't need further iteration
//     */
//    private boolean iterate(RegSarimaModelling modelling) {
//
//        if (modelling.needEstimation()) {
//            modelling.estimate(options.getIntermediatePrecision());
//        }
//
//        ModelDescription desc = modelling.getDescription();
//        boolean changed = false;
//        SarimaOrders curspec = desc.specification();
//        boolean curMean = desc.isMean();
//        if (needOutliers(desc)) {
//
//            if (modelling.getDescription().removeVariable(var -> ModellingUtility.isOutlier(var, true))) {
//                modelling.clearEstimation();
//            }
//            ProcessingResult rslt = execOutliers(modelling);
//            changed = changed || rslt == ProcessingResult.Changed;
//        }
////        context.estimate(options.precision);
//
//        if (!estimateModel(modelling)) {
//            needOutliers = isOutliersDetection();
//            needAutoModelling = isAutoModelling();
//            ++round;
//            ++pass;
//            return false;
//        }
//
//        if (round == 0 && (isAutoModelling() || isOutliersDetection())) {
//            needOutliers = isOutliersDetection();
//            needAutoModelling = isAutoModelling();
//            ++round;
//            ++pass;
//            refAirline = RegSarimaModelling.copyOf(modelling);
//            refAuto = RegSarimaModelling.copyOf(modelling);
//            refStats = ModelStatistics.of(refAuto.getDescription(), refAuto.getEstimation().getConcentratedLikelihood());
//            double lb = refStats.getLjungBoxPvalue();
//            return options.acceptAirline && (1 - lb) < options.ljungBoxLimit;
//        }
//        if (pass <= 3 && !pass3 && isAutoModelling()) {
//            if (!pass2(!changed, modelling)) {
//                return false;
//            }
//        }
////
//        ModelEstimator estimator = new ModelEstimator(options.precision, curva, outliersModule());
////
//        if (spec.isUsingAutoModel()) {
//            scontroller.setEstimator(estimator);
//            if (scontroller.process(modelling, context) == ProcessingResult.Changed) {
//                if (!pass3) {
//                    pass3 = true;
//                    pass = 1;
//                    needAutoModelling = true;
//                    needOutliers = isOutliersDetection();
//                    return false;
//                }
//            }
//        }
//        for (ModelController controller : controllers) {
//            controller.setEstimator(estimator);
//            controller.setReferenceModel(refAuto);
//            controller.process(modelling, context);
//        }
//        return true;
//    }
//
//    //
//    //    private void control(RegArimaContext context, ModelEstimator estimator) {
//    //        boolean changed = false;
//    //        for (IModelController controller : controllers) {
//    //            try {
//    //                controller.setEstimator(estimator);
//    //                ProcessingResult rslt = controller.process(context);
//    //                if (rslt == ProcessingResult.Changed) {
//    //                    changed = true;
//    //                }
//    //            } catch (Exception err) {
//    //                // if a controller fails, go to the next one !
//    //            }
//    //        }
//    ////        if (changed) {
//    ////            pass_=4;
//    ////            estimateModel(context);
//    ////        }
//    //
//    //    }
//    //
//    private boolean isOutliersDetection() {
//        return spec.getOutliers().isUsed();
//    }
//
//    private boolean needOutliers(ModelDescription desc) {
//
//        if (!isOutliersDetection()) {
//            return false;
//        }
//        return needOutliers;
//    }
//
//    private ProcessingResult execOutliers(RegSarimaModelling context) {
//        return outliersModule().process(context, curva);
//    }
//
//    private static final String SEAS = "seasonality test";
//
//    private void testSeasonality(RegSarimaModelling modelling) {
//        ModelDescription model = modelling.getDescription();
//        if (!isAutoModelling()) {
//            context.seasonal = model.specification().isSeasonal();
//            return;
//        }
//
//        int period = model.getAnnualFrequency();
//        if (period > 1) {
//
//            TramoSeasonalityDetector seas = new TramoSeasonalityDetector();
//            SeasonalityDetector.Seasonality s = seas.hasSeasonality(model.getTransformedSeries().getValues(), period);
//            context.originalSeasonalityTest = s.toInt();
//            if (context.originalSeasonalityTest < 2) {
//                SarimaOrders nspec = SarimaOrders.m011(period);
//                model.setSpecification(nspec);
//                context.seasonal = false;
//            } else {
//                context.seasonal = true;
//            }
//            ProcessingLog log = modelling.getLog();
//            if (log != null) {
//                log.step(SEAS, context.originalSeasonalityTest);
//            }
//        } else {
//            context.seasonal = false;
//        }
//    }
//
//    private void testTransformation(RegSarimaModelling modelling) {
//        TransformSpec tspec = spec.getTransform();
//        if (tspec.getFunction() == TransformationType.Auto) {
//            boolean toClean = false;
//            if (tspec.isOutliersCorrection()) {
//                OutliersDetectionModule outliers = robustOutliersModule();
//                ProcessingResult rslt = outliers.process(modelling, 5);
//                toClean = rslt == ProcessingResult.Changed;
//            }
//            LogLevelModule module = LogLevelModule.builder()
//                    .logPreference(Math.log(tspec.getFct()))
//                    .estimationPrecision(options.intermediatePrecision)
//                    .seasonal(context.seasonal)
//                    .build();
//            module.process(modelling);
//            ModelDescription desc = modelling.getDescription();
//            if (toClean) {
//                if (desc.removeVariable(var -> ModellingUtility.isOutlier(var, true))) {
//                    modelling.clearEstimation();
//                }
//            }
//        } else if (modelling.getDescription().isLogTransformation()) {
//            if (modelling.getDescription().getSeries().getValues().anyMatch(x -> x <= 0)) {
//                modelling.getLog().warning("logs changed to levels");
//                modelling.getDescription().setLogTransformation(false);
//                modelling.getDescription().setPreadjustment(LengthOfPeriodType.None);
//                modelling.clearEstimation();
//            }
//        }
//    }
//
//    private boolean estimateModel(RegSarimaModelling context) {
//
//        FinalEstimator estimator = FinalEstimator.builder()
//                .precision(options.precision)
//                .unitRootThreshold(options.ur)
//                .pass(pass)
//                .ami(isAutoModelling())
//                .outliers(isOutliersDetection())
//                .build();
//        int niter = 0;
//        do {
//            if (!estimator.estimate(context)) {
//                if (pass == 1 && this.needOutliers(context.getDescription())
//                        && this.needAutoModelling(context.getDescription())) {
//                    reduceVa();
//                }
//                return false;
//            }
//            if (pass3 || pass != 0) {
//                return true;
//            }
//        } while (niter++ < 5 );
//        return true;
//    }
//
//}
