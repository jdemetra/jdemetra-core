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

import demetra.tramo.internal.TramoUtility;
import demetra.design.Development;
import demetra.information.InformationSet;
import demetra.modelling.TransformationType;
import demetra.modelling.regression.AdditiveOutlier;
import demetra.modelling.regression.LevelShift;
import demetra.modelling.regression.ModellingContext;
import demetra.modelling.regression.PeriodicOutlier;
import demetra.modelling.regression.TransitoryChange;
import demetra.regarima.regular.IPreprocessor;
import demetra.regarima.regular.IRegressionModule;
import demetra.regarima.regular.SeasonalityDetector;
import demetra.regarima.regular.ModelDescription;
import demetra.regarima.regular.RegArimaModelling;
import demetra.regarima.regular.PreprocessingModel;
import demetra.sarima.SarimaSpecification;
import demetra.timeseries.TsData;
import demetra.regarima.regular.ProcessingResult;
import demetra.sarima.SarimaModel;
import demetra.timeseries.calendars.DayClustering;
import demetra.tramo.internal.ArmaModule;
import demetra.tramo.internal.DifferencingModule;
import demetra.tramo.internal.OutliersDetectionModule;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class TramoProcessor implements IPreprocessor {

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

    private PreprocessingModel refAirline, refAuto;
    private ModelStatistics refStats;

    private int pass = 0, round = 0;
    private double curva = 0, pcr = 0;
    private boolean pass3, seasonal;
    private int ost;

    private TramoProcessor(TramoSpec spec, ModellingContext context) {
        this.spec = new TramoSpec(spec);
        this.modellingContext = context;
        this.options = readAmiOptions(spec);
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
                        .fPValue(tdspec.getProbabibilityForFTest())
                        .build();
            } else {
                return AutomaticWaldRegressionTest.builder()
                        .easter(TramoModelBuilder.easter(spec))
                        .leapYear(TramoModelBuilder.leapYear(tdspec))
                        .tradingDays(TramoModelBuilder.td(spec, DayClustering.TD7, modellingContext))
                        .workingDays(TramoModelBuilder.td(spec, DayClustering.TD2, modellingContext))
                        .testMean(spec.isUsingAutoModel())
                        .fPValue(tdspec.getProbabibilityForFTest())
                        .PConstraint(tdspec.getProbabibilityForFTest())
                        .build();
            }
        } else {
            return DefaultRegressionTest.builder()
                    .easter(TramoModelBuilder.easter(spec))
                    .leapYear(TramoModelBuilder.leapYear(tdspec))
                    .tradingDays(TramoModelBuilder.tradingDays(spec, modellingContext))
                    .testMean(spec.isUsingAutoModel())
                    .build();
        }
    }

    private OutliersDetectionModule outliersModule() {
        OutlierSpec outliers = spec.getOutliers();
        OutliersDetectionModule.Builder obuilder = OutliersDetectionModule.builder();
        String[] types = outliers.getTypes();
        for (int i = 0; i < types.length; ++i) {
            switch (types[i]) {
                case AdditiveOutlier.CODE:
                    obuilder.ao(true);
                    break;
                case LevelShift.CODE:
                    obuilder.ls(true);
                    break;
                case TransitoryChange.CODE:
                    obuilder.tc(true);
                    break;
                case PeriodicOutlier.CODE:
                    obuilder.so(true);
                    break;
            }
        }
        return obuilder.span(outliers.getSpan())
                .tcrate(outliers.getDeltaTC())
                .maximumLikelihood(outliers.isMaximumLikelihood())
                .build();
    }

    private DifferencingModule differencingModule() {
        AutoModelSpec amiSpec = spec.getAutoModel();
        return DifferencingModule.builder()
                .cancel(amiSpec.getCancel())
                .ub1(amiSpec.getUb1())
                .ub2(amiSpec.getUb2())
                .seasonal(seasonal)
                .build();
    }

    private ArmaModule armaModule() {
        return ArmaModule.builder()
                .seasonal(seasonal)
                .build();
    }

    @Override
    public PreprocessingModel process(TsData originalTs, RegArimaModelling context) {
//        clear();
        if (context == null) {
            context = new RegArimaModelling();
        }
        ModelDescription desc = build(originalTs, context.getLog());
        if (desc == null) {
            throw new TramoException("Initialization failed");
        }
        context.setDescription(desc);
        PreprocessingModel rslt = ami(context);
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

    private PreprocessingModel ami(RegArimaModelling context) {

        // Test the seasonality
        testSeasonality(context);

        // Test for loglevel transformation
        testTransformation(context);

        regressionModule().test(context);

        if (isFullySpecified()) {
            context.estimate(options.precision);
            return context.build();
        }

        initProcessing(context.getDescription().regarima().getActualObservationsCount());

        int iter = 0;
        do {
            ++iter;
        } while (iter < 10 && !iterate(context));

        return context.build();
    }

    private void initProcessing(int n) {
        round = 0;
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
     * @param context
     * @return True if the model doesn't need further iteration
     */
    private boolean iterate(RegArimaModelling context) {
        if (context.needEstimation()) {
            context.estimate(options.getIntermediatePrecision());
        }

        ModelDescription desc = context.getDescription();
        boolean changed = false;
        SarimaSpecification curspec = desc.getSpecification();
        boolean curmu = desc.isMean();
        if (needDifferencing(desc)) {
            execDifferencing(context);
        }
        if (needAutoModelling(desc)) {
            execAutoModelling(context);
            desc = context.getDescription();
            changed = (!desc.getSpecification().equals(curspec))
                    || desc.isEstimatedMean() != curmu;
        }
        if (needOutliers(desc)) {

            if (context.getDescription().removeVariable(var -> var.isOutlier(false))) {
                context.setEstimation(null);
            }
            ProcessingResult rslt = execOutliers(context);
            changed = changed || rslt == ProcessingResult.Changed;
        }
//        context.estimate(options.precision);

        if (!estimateModel(context)) {
            ++round;
            ++pass;
            return false;
        }

        if (round == 0) {
            ++round;
            ++pass;
            refAirline = context.build();
            refAuto = refAirline;
            refStats = ModelStatistics.of(refAuto);
            double lb = refAirline.getEstimation().getTests().ljungBox().getPValue();
            return options.acceptAirline && (1 - lb) < options.ljungBoxLimit;
        }
        if (pass <= 3 && !pass3 && isAutoModelling()) {
            if (!pass2(!changed, context)) {
                return false;
            }
        }

        if (!testRegression(context, spec.getAutoModel().getTsig())) {
            pass = 4;
            return false;
        }
//
//        ModelEstimator estimator = new ModelEstimator();
//        estimator.setOutliersDetectionModule(outliers);
//        estimator.setPrecision(finalizer.getEpsilon());
//
//        if (context.automodelling) {
//            seasonalityController.setEstimator(estimator);
//            if (seasonalityController.process(context) == ProcessingResult.Changed) {
//                if (!pass3_) {
//                    pass3_ = true;
//                    needAutoModelling_ = true;
////                        context.description.setOutliers(null);
//                    needOutliers_ = outliers != null;
//                    pass_ = 1;
//                    return false;
//                }
//            }
//        }
//        control(context, estimator);
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
        if (!isAutoModelling()) {
            return false;
        }
        if (pass > 2) {
            return false;
        }

        switch (round) {
            case 0:
                return false;
            case 1:
                return true;
            default:
                SarimaSpecification curspec = desc.getSpecification();
                if (curspec.getD() == 2 && curspec.getBd() == 1) {
                    return false;
                }
                return !ModelDescription.sameVariables(refAirline.getDescription(), desc);
        }
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
        if (!isAutoModelling()) {
            return false;
        }
        if (pass > 2)
            return false;
        if (round == 0) {
            return false;
        }
        if (round == 2 && desc.variables().anyMatch(var -> var.isOutlier(false))) {
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
        SarimaSpecification curspec = desc.getSpecification();
        if (curspec.getParametersCount() == 0 && pass >= 3) {
            curspec.setQ(1);
            desc.setSpecification(curspec);
            context.setEstimation(null);
        }
        return rslt;
    }

    private ProcessingResult execOutliers(RegArimaModelling context) {
        return outliersModule().process(context, curva);
    }

    private void restore(RegArimaModelling context) {
        context.setDescription(new ModelDescription(refAuto.getDescription()));
        context.setEstimation(refAuto.getEstimation());
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
//            if (outliers != null) {
//                refsens_ = outliers.getSelectivity();
//            }
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

        if (pass > 2) {
            lastSolution(context);
        }
        return false;
    }

    // use the default model, clear outliers
    private void lastSolution(RegArimaModelling context) {
        ModelDescription desc = context.getDescription();
        SarimaSpecification nspec = desc.getSpecification();
        nspec.setP(3);
        if (nspec.getBd() > 0) {
            nspec.setBp(0);
        }
        nspec.setQ(1);
        if (seasonal) {
            nspec.setBq(1);
        }
        desc.setSpecification(nspec);
        desc.removeVariable(var -> var.isOutlier(false));
        context.setEstimation(null);
//        addArmaHistory(context);
        round = 1;
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

    private void testSeasonality(RegArimaModelling context) {
        if (!isAutoModelling()) {
            return;
        }

        ModelDescription model = context.getDescription();
        int ifreq = model.getAnnualFrequency();
        if (ifreq > 1) {
            TramoSeasonalityDetector seas = new TramoSeasonalityDetector();
            SeasonalityDetector.Seasonality s = seas.hasSeasonality(model.getTransformedSeries());
            ost = s.getAsInt();
            if (ost < 2) {
                SarimaSpecification nspec = new SarimaSpecification(ifreq);
                nspec.airline(false);
                model.setSpecification(nspec);
                context.setEstimation(null);
                seasonal = false;
            } else {
                seasonal = true;
            }
        } else {
            seasonal = false;
        }
    }

    private void testTransformation(RegArimaModelling context) {
        TransformSpec tspec = spec.getTransform();
        EstimateSpec espec = spec.getEstimate();
        if (tspec.getFunction() == TransformationType.Auto) {
            LogLevelModule module = LogLevelModule.builder()
                    .logPreference(Math.log(tspec.getFct()))
                    .estimationPrecision(espec.getTol())
                    .seasonal(seasonal)
                    .build();
            module.process(context);
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
                .meanThreshold(tmean)
                .build();
        return regtest.test(context) == ProcessingResult.Unchanged;
    }

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
}
