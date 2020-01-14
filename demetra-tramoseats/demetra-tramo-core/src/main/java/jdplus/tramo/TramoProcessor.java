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
import demetra.design.Development;
import demetra.information.InformationSet;
import demetra.modelling.TransformationType;
import demetra.timeseries.regression.ModellingContext;
import jdplus.regarima.regular.IPreprocessor;
import jdplus.regarima.regular.IRegressionModule;
import jdplus.regarima.regular.SeasonalityDetector;
import jdplus.regarima.regular.ModelDescription;
import jdplus.regarima.regular.RegArimaModelling;
import jdplus.regarima.regular.PreprocessingModel;
import demetra.arima.SarimaSpecification;
import demetra.timeseries.TsData;
import jdplus.regarima.regular.ProcessingResult;
import jdplus.sarima.SarimaModel;
import demetra.timeseries.calendars.DayClustering;
import demetra.tramo.AutoModelSpec;
import demetra.tramo.EstimateSpec;
import demetra.tramo.OutlierSpec;
import demetra.tramo.TradingDaysSpec;
import demetra.tramo.TramoSpec;
import demetra.tramo.TransformSpec;
import jdplus.tramo.internal.ArmaModule;
import jdplus.tramo.internal.DifferencingModule;
import jdplus.tramo.internal.OutliersDetectionModule;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class TramoProcessor implements IPreprocessor {

    static class Context {

        int originalSeasonalityTest;
        boolean seasonal;
    }

    @lombok.Value
    @lombok.Builder
    public static class AmiOptions {

        boolean checkMu;
        double precision, intermediatePrecision, ur;
        double va;
        double reduceVa;
        double ljungBoxLimit;
        boolean acceptAirline;

        public static AmiOptionsBuilder builder() {
            return new AmiOptionsBuilder()
                    .intermediatePrecision(1e-5)
                    .precision(1e-7)
                    .ur(.96)
                    .reduceVa(.12)
                    .ljungBoxLimit(.95);
        }
    }

    public static TramoProcessor of(TramoSpec spec, ModellingContext context) {
        return new TramoProcessor(spec, context);
    }

    private final TramoSpec spec;
    private final ModellingContext modellingContext;
    private final AmiOptions options;
    private final Context context = new Context();

    private final SeasonalityController scontroller;
    private final List<ModelController> controllers = new ArrayList<>();

    private PreprocessingModel refAirline, refAuto;
    private ModelStatistics refStats;

    private int pass = 0, round = 0;
    private double curva = 0, pcr = 0;
    private boolean pass3;
    private boolean needOutliers;
    private boolean needAutoModelling;

    private TramoProcessor(TramoSpec spec, ModellingContext context) {        
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
                .checkMu(spec.isUsingAutoModel())
                .ljungBoxLimit(ami.getPcr())
                .acceptAirline(ami.isAcceptDefault())
                .build();
    }

    private IRegressionModule regressionModule() {
        TradingDaysSpec tdspec = spec.getRegression().getCalendar().getTradingDays();
        if (tdspec.isAutomatic()) {
            if (tdspec.getAutomaticMethod() == TradingDaysSpec.AutoMethod.FTest) {
                return AutomaticFRegressionTest.builder()
                        .easter(TramoModelBuilder.easter(spec))
                        .leapYear(TramoModelBuilder.leapYear(tdspec))
                        .tradingDays(TramoModelBuilder.td(spec, DayClustering.TD7, modellingContext))
                        .workingDays(TramoModelBuilder.td(spec, DayClustering.TD2, modellingContext))
                        .testMean(spec.isUsingAutoModel())
                        .fPValue(tdspec.getProbabilityForFTest())
                        .estimationPrecision(options.intermediatePrecision)
                        .build();
            } else {
                return AutomaticWaldRegressionTest.builder()
                        .easter(TramoModelBuilder.easter(spec))
                        .leapYear(TramoModelBuilder.leapYear(tdspec))
                        .tradingDays(TramoModelBuilder.td(spec, DayClustering.TD7, modellingContext))
                        .workingDays(TramoModelBuilder.td(spec, DayClustering.TD2, modellingContext))
                        .testMean(spec.isUsingAutoModel())
                        .fPValue(tdspec.getProbabilityForFTest())
                        .PConstraint(tdspec.getProbabilityForFTest())
                        .estimationPrecision(options.intermediatePrecision)
                        .build();
            }
        } else {
            return DefaultRegressionTest.builder()
                    .easter(TramoModelBuilder.easter(spec))
                    .leapYear(TramoModelBuilder.leapYear(tdspec))
                    .tradingDays(TramoModelBuilder.tradingDays(spec, modellingContext))
                    .testMean(spec.isUsingAutoModel())
                    .estimationPrecision(options.intermediatePrecision)
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

    private DifferencingModule differencingModule() {
        AutoModelSpec amiSpec = spec.getAutoModel();
        return DifferencingModule.builder()
                .cancel(amiSpec.getCancel())
                .ub1(amiSpec.getUb1())
                .ub2(amiSpec.getUb2())
                .seasonal(context.seasonal)
                .precision(options.intermediatePrecision)
                .initial(round==1)
                .build();
    }

    private ArmaModule armaModule() {
        return ArmaModule.builder()
                .seasonal(context.seasonal)
                .build();
    }

    @Override
    public PreprocessingModel process(TsData originalTs, RegArimaModelling modelling) {
//        clear();
        if (modelling == null) {
            modelling = new RegArimaModelling();
        }
        ModelDescription desc = build(originalTs, modelling.getLog());
        if (desc == null) {
            throw new TramoException("Initialization failed");
        }
        modelling.setDescription(desc);
        PreprocessingModel rslt = ami(modelling);
//        if (rslt != null) {
//            rslt.info_ = context.information;
//            rslt.addProcessingInformation(context.processingLog);
//        }
        return rslt;
    }

    private ModelDescription build(TsData originalTs, InformationSet log) {
        TramoModelBuilder builder = new TramoModelBuilder(spec, modellingContext);
        return builder.build(originalTs, log);
    }

    private boolean isFullySpecified() {
        // Nothing to do.
        return !(this.isAutoModelling() || this.isOutliersDetection());

    }

    private PreprocessingModel ami(RegArimaModelling modelling) {

        // Test the seasonality
        testSeasonality(modelling);

        // Test for loglevel transformation
        testTransformation(modelling);

        regressionModule().test(modelling);

        if (isFullySpecified()) {
            modelling.estimate(options.precision);
            return modelling.build();
        }

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
    private boolean iterate(RegArimaModelling modelling) {

        if (modelling.needEstimation()) {
            modelling.estimate(options.getIntermediatePrecision());
        }

        ModelDescription desc = modelling.getDescription();
        boolean changed = false;
        SarimaSpecification curspec = desc.specification();
        boolean curMean = desc.isMean();
        if (needDifferencing(desc)) {
            changed=execDifferencing(modelling) == ProcessingResult.Changed;
            SarimaSpecification nspec = desc.specification();
            if (pass == 1 && nspec.getDifferenceOrder() != curspec.getDifferenceOrder() ) {
                desc.removeVariable(var -> var.isOutlier(false));
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

            if (modelling.getDescription().removeVariable(var -> var.isOutlier(false))) {
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
            refAirline = modelling.build();
            refAuto = refAirline;
            refStats = ModelStatistics.of(refAuto);
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
        if (round == 2 && !desc.variables().anyMatch(var -> var.isOutlier(false))) {
            return false;
        }
        SarimaSpecification curspec = desc.specification();
        if (curspec.getD() == 2 && curspec.getBd() == 1) {
            return false;
        }
        return true;
//        return !ModelDescription.sameVariables(refAirline.getDescription(), desc);
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
        return round < 3;
    }

    private boolean needAutoModelling(ModelDescription desc) {
        if (!needAutoModelling) {
            return false;
        }
        if (round == 2 && !desc.variables().anyMatch(var -> var.isOutlier(false))) {
            return false;
        }
        // Should be completed
        return true;
    }

    private ProcessingResult execDifferencing(RegArimaModelling context) {
        return differencingModule().process(context);
    }

    private ProcessingResult execAutoModelling(RegArimaModelling context) {
        ProcessingResult rslt = armaModule().process(context);
        ModelDescription desc = context.getDescription();
        SarimaSpecification curspec = desc.specification();
        if (curspec.getParametersCount() == 0 && pass >= 3) {
            curspec.setQ(1);
            desc.setSpecification(curspec);
         }
        return rslt;
    }

    private ProcessingResult execOutliers(RegArimaModelling context) {
        return outliersModule().process(context, curva);
    }

    private void restore(RegArimaModelling context) {
        context.set(new ModelDescription(refAuto.getDescription()), refAuto.getEstimation());
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

    protected boolean pass2(final boolean same, RegArimaModelling context) {
        double fct = 1, fct2 = 1;
        boolean useprev = false;
        SarimaModel curmodel = context.getDescription().arima();
        SarimaSpecification curspec = curmodel.specification();

        PreprocessingModel cur = context.build();
        ModelStatistics stats = ModelStatistics.of(cur);
        double plbox = 1 - stats.getLjungBoxPvalue();
        double rvr = stats.getSe();
        if (refAuto != null) {
            double plbox0 = 1 - refStats.getLjungBoxPvalue();
            double rvr0 = refStats.getSe();
            int nout = context.getDescription().countRegressors(var -> var.isOutlier(false));
            int refout = refAuto.getDescription().countRegressors(var -> var.isOutlier(false));
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
                refAuto = cur;
                refStats = stats;
            } else {
                restore(context);
                plbox = plbox0;
            }
        } else {
            refAuto = cur;
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
    private void lastSolution(RegArimaModelling modelling) {
        ModelDescription desc = modelling.getDescription();
        SarimaSpecification nspec = desc.specification();
        nspec.setP(3);
        if (nspec.getBd() > 0) {
            nspec.setBp(0);
        }
        nspec.setQ(1);
        if (context.seasonal) {
            nspec.setBq(1);
        }
        desc.removeVariable(var -> var.isOutlier(false));
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

    private void testSeasonality(RegArimaModelling modelling) {
        ModelDescription model = modelling.getDescription();
        if (!isAutoModelling()) {
            context.seasonal = model.specification().isSeasonal();
            return;
        }

        int ifreq = model.getAnnualFrequency();
        if (ifreq > 1) {
            TramoSeasonalityDetector seas = new TramoSeasonalityDetector();
            SeasonalityDetector.Seasonality s = seas.hasSeasonality(model.getTransformedSeries());
            context.originalSeasonalityTest = s.getAsInt();
            if (context.originalSeasonalityTest < 2) {
                SarimaSpecification nspec = SarimaSpecification.m011(ifreq);
                model.setSpecification(nspec);
                context.seasonal = false;
            } else {
                context.seasonal = true;
            }
        } else {
            context.seasonal = false;
        }
    }

    private void testTransformation(RegArimaModelling modelling) {
        TransformSpec tspec = spec.getTransform();
        EstimateSpec espec = spec.getEstimate();
        if (tspec.getFunction() == TransformationType.Auto) {
            LogLevelModule module = LogLevelModule.builder()
                    .logPreference(Math.log(tspec.getFct()))
                    .estimationPrecision(options.intermediatePrecision)
                    .seasonal(context.seasonal)
                    .build();
            module.process(modelling);
        }
    }

    private boolean estimateModel(RegArimaModelling context) {

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

    private boolean testRegression(RegArimaModelling context, double tmean) {
        FastRegressionTest regtest = FastRegressionTest.builder()
                .testMean(isAutoModelling())
                .meanThreshold(tmean)
                .build();
        return regtest.test(context) == ProcessingResult.Unchanged;
    }

}
