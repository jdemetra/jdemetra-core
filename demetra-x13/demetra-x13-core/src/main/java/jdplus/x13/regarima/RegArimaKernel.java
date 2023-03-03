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
package jdplus.x13.regarima;

import demetra.arima.SarimaOrders;
import demetra.math.Complex;
import jdplus.regsarima.regular.RegSarimaProcessor;
import demetra.processing.ProcessingLog;
import demetra.regarima.RegArimaException;
import demetra.regarima.RegArimaSpec;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.ModellingContext;
import jdplus.regarima.RegArimaEstimation;
import demetra.timeseries.regression.ModellingUtility;
import jdplus.regsarima.regular.IAutoModellingModule;
import jdplus.regsarima.regular.ILogLevelModule;
import jdplus.regsarima.regular.IModelBuilder;
import jdplus.regsarima.regular.IOutliersDetectionModule;
import jdplus.regsarima.regular.IRegressionModule;
import jdplus.regsarima.regular.ModelDescription;
import jdplus.regsarima.regular.RegSarimaModel;
import jdplus.regsarima.regular.ProcessingResult;
import jdplus.regsarima.regular.RegSarimaModelling;
import jdplus.regsarima.regular.RegressionVariablesTest;
import jdplus.sarima.SarimaModel;
import nbbrd.design.BuilderPattern;
import nbbrd.design.Development;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class RegArimaKernel implements RegSarimaProcessor {

    @lombok.Value
    @lombok.Builder(builderClassName = "AmiBuilder")
    public static class AmiOptions {

        public static final double DEF_EPS = 1e-7, DEF_IEPS = 1e-7;

        public static AmiBuilder builder() {
            AmiBuilder builder = new AmiBuilder();
            builder.checkMu = true;
            builder.precision = DEF_EPS;
            builder.intermediatePrecision = DEF_IEPS;
            builder.va=0;
            builder.reduceVa = .14286;
            builder.ljungBoxLimit = .95;
            builder.urLimit = .95;
            builder.acceptAirline = false;
            builder.mixedModel = true;
            return builder;
        }

//        public static class Builder {
//
//            boolean checkMu = true;
//            double precision = 1e-7;
//            double va = 0;
//            double reduceVa = .14286;
//            double ljungBoxLimit = .95;
//            double urLimit = .95;
//            boolean acceptAirline = false;
//            boolean mixedModel = true;
//        }
        boolean checkMu;
        double precision, intermediatePrecision;
        double va;
        double reduceVa;
        double ljungBoxLimit;
        double urLimit;
        boolean acceptAirline;
        boolean mixedModel;
    }

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(RegArimaKernel.class)
    public static class Builder {

        private IModelBuilder modelBuilder = new DefaultModelBuilder();
        private ILogLevelModule transformation;
        private IRegressionModule calendarTest, easterTest;
        private IAutoModellingModule autoModel;
        private IOutliersDetectionModule outliers;
        private RegressionVariablesTest regressionTest0, regressionTest1;
        private AmiOptions options = AmiOptions.builder().build();

        public Builder modelBuilder(@NonNull IModelBuilder builder) {
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

        public Builder autoModelling(IAutoModellingModule ami) {
            this.autoModel = ami;
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

        public Builder outliers(IOutliersDetectionModule outliers) {
            this.outliers = outliers;
            return this;
        }

        public Builder initialRegressionTest(RegressionVariablesTest test0) {
            this.regressionTest0 = test0;
            return this;
        }

        public Builder finalRegressionTest(RegressionVariablesTest test1) {
            this.regressionTest1 = test1;
            return this;
        }

        public RegArimaKernel build() {
            RegArimaKernel processor = new RegArimaKernel(this);
            return processor;
        }

    }

    public static RegArimaKernel of(RegArimaSpec spec, ModellingContext context) {
        if (!spec.getBasic().isPreprocessing())
            return null;
        X13SpecDecoder helper = new X13SpecDecoder(spec, context);
        return helper.buildProcessor();
    }

    private static final double FCT = 1 / (1 - .0125), FCT2 = 1, MALIM = .001;
    private static final double MINCV = 2.8;
    private static final int MAXD = 2, MAXBD = 1;

    private final IModelBuilder modelBuilder;
    private final ILogLevelModule transformation;
    private final IRegressionModule calendarTest, easterTest;
    private final IOutliersDetectionModule outliers;
    private final AmiOptions options;
    private final IAutoModellingModule autoModel;
    private final RegressionVariablesTest regressionTest0, regressionTest1;
    private final FinalEstimator finalEstimator;

    private double va0 = 0, curva = 0;
    private double pcr;
    private RegSarimaModelling reference;
    private boolean needOutliers;
    private boolean needAutoModelling;
    private int loop, round;
    private double plbox0, rvr0, rtval0;

    private RegArimaKernel(Builder builder) {
        this.modelBuilder = builder.modelBuilder;
        this.transformation = builder.transformation;
        this.calendarTest = builder.calendarTest;
        this.easterTest = builder.easterTest;
        this.outliers = builder.outliers;
        this.options = builder.options;
        this.autoModel = builder.autoModel;
        this.regressionTest0 = builder.regressionTest0;
        this.regressionTest1 = builder.regressionTest1;
        finalEstimator = FinalEstimator.builder()
                .ami(autoModel != null)
                .precision(options.precision)
                .build();
    }

    private void clear() {
        loop = round = 0;
        plbox0 = rvr0 = rtval0 = 0;
        reference = null;
        curva = 0;
        pcr = options.ljungBoxLimit;
        needAutoModelling = autoModel != null;
    }

    @Override
    public RegSarimaModel process(TsData originalTs, ProcessingLog log) {
        if (log == null) {
            log = ProcessingLog.dummy();
        }
        clear();
        ModelDescription desc = modelBuilder.build(originalTs, log);
        if (desc == null) {
            throw new RegArimaException("Initialization failed");
        }
        RegSarimaModelling context = RegSarimaModelling.of(desc, log);
        // initialize some internal variables
        if (outliers != null) {
            va0 = options.getVa();
            if (va0 == 0) {
                va0 = X13Utility.calcCv(desc.getSeries().getDomain().getLength());
            }
            curva = va0;
            needOutliers = true;
        }

        RegSarimaModel rslt = calc(context);
//        if (rslt != null) {
//            rslt.info_ = context.information;
//            rslt.addProcessingInformation(context.processingLog);
//        }
        return rslt;
    }

    private RegSarimaModel calc(RegSarimaModelling context) {
        try {

            if (transformation != null) {
                transformation.process(context);
            } else if (context.getDescription().isLogTransformation()) {
                if (context.getDescription().getSeries().getValues().anyMatch(x -> x <= 0)) {
                    context.getLog().warning("logs changed to levels");
                    context.getDescription().setLogTransformation(false);
                    context.clearEstimation();
                }
            }
            regAIC(context);
            checkMu(context, MeanController.CVAL0);
            if (needOutliers && ProcessingResult.Changed == outliers.process(context, curva)) {
                if (context.needEstimation()) {
                    context.estimate(options.precision);
                }
                regressionTest0.process(context);
            }
            if (isAutoModelling()) {
                if (context.needEstimation()) {
                    context.estimate(options.precision);
                }
                reference = RegSarimaModelling.copyOf(context);
                ModelController controller = new ModelController(.95);
                boolean ok = controller.accept(context);
                plbox0 = 1 - controller.getLjungBoxTest().getPvalue();
                rvr0 = controller.getRvr();
                rtval0 = controller.getRTval();
                if (!options.acceptAirline || !ok) {

                    round = 1;
                    loop = 1;
                    do {
                        boolean defModel = false;
                        if (needAutoModelling) {
                            ProcessingResult amrslt = autoModel.process(context);
                            defModel = amrslt == ProcessingResult.Unchanged;
                            if (!defModel) {
                                if (context.getDescription().removeVariable(var -> ModellingUtility.isOutlier(var, true))) {
                                    context.clearEstimation();
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
                        if (outliers != null && loop <= 2) {
                            if (!pass2(defModel, context)) {
                                continue;
                            }
                        } else {
                            controller.accept(context);
                            rtval0 = controller.getRTval();
                            rvr0 = controller.getRvr();
                            plbox0 = 1 - controller.getLjungBoxTest().getPvalue();
                        }
                        if (regressionTest1 != null) {
                            ProcessingResult changed = regressionTest1.process(context);
                            if (changed == ProcessingResult.Changed) {
                                if (loop < 3) {
                                    loop = 3;
                                }
                                if (context.needEstimation()) {
                                    context.estimate(options.precision);
                                }
                                controller.accept(context);
                                rtval0 = controller.getRTval();
                                rvr0 = controller.getRvr();
                                plbox0 = 1 - controller.getLjungBoxTest().getPvalue();
                            }
                        }
                        // final tests
                        boolean checked;
                        do {
                            checked = true;
                            if (!checkUnitRoots(context)) {
                                checked = false;
                            }
                            if (!checkMA(context)) {
                                checked = false;
                            }
                        } while (!checked);

                        if (isAutoModelling() && !context.getDescription().isMean()) {
                            if (options.checkMu && rtval0 > MeanController.CVALFINAL) {
                                context.getDescription().setMean(true);
                                context.clearEstimation();
                            }
                        }

                        if (finalEstimator.estimate(context)) {
                            break;
                        }
                        if (loop <= 2 && outliers != null) {
                            curva = Math.max(MINCV, curva * (1 - options.reduceVa));
                            needOutliers = true;
                            needAutoModelling = true;
                        }
                    } while (round++ < 5);
                }
            } else {
                finalEstimator.estimate(context);
            }

            return context.build();

        } catch (Exception err) {
            return null;
        } finally {
            clear();
        }
    }

    private boolean isAutoModelling() {
        return autoModel != null;
    }

    private ProcessingResult checkMu(RegSarimaModelling context, double cv) {
        if (!options.checkMu) {
            return ProcessingResult.Unchanged;
        }
        MeanController meanTest = new MeanController(cv);
        return meanTest.test(context);
    }

    private boolean pass2(boolean defModel, RegSarimaModelling context) {
        int ichk = 0;
        ModelDescription desc = context.getDescription();
        int naut = (int) desc.variables().filter(v -> ModellingUtility.isOutlier(v, true)).count();

        ModelDescription desc0 = reference.getDescription();
        RegArimaEstimation<SarimaModel> estimation0 = reference.getEstimation();

        int naut0 = (int) desc0.variables().filter(v -> ModellingUtility.isOutlier(v, true)).count();
        SarimaOrders spec0 = desc0.specification();
        SarimaOrders spec = desc.specification();
        SarimaModel arima = desc.arima();
        boolean mu0 = desc0.isMean(),
                mu = desc.isMean();
        ModelController controller = new ModelController(.95);
        controller.accept(context);
        double rtval = controller.getRTval();
        double rvr = controller.getRvr();
        double plbox = 1 - controller.getLjungBoxTest().getPvalue();
        if (naut0 <= naut && (!spec0.equals(spec) || mu0 != mu)) {
            if (plbox < .95 && plbox < .75 && rvr0 < rvr) {
                ichk = 1;
            } else if (loop == 1 && plbox >= .95 && plbox0 < .95) {
                ichk = 2;
            } else if (plbox < .95 && plbox0 < 0.75 && plbox0 < plbox
                    && rvr0 < FCT * rvr) {
                ichk = 3;
            } else if (plbox >= .95 && plbox0 < .95
                    && rvr0 < FCT2 * rvr) {
                ichk = 4;
            } else if (spec.getD() == 0 && spec.getBd() == 1 && spec.getP() == 1
                    && spec.getBp() == 0 && spec.getQ() == 1 && spec.getBq() == 1
                    && arima.phi(1) < -.82) {
                ichk = 5;
            } else if (spec.getD() == 1 && spec.getBd() == 0 && spec.getP() == 0
                    && spec.getBp() == 1 && spec.getQ() == 1 && spec.getBq() == 1
                    && arima.bphi(1) < -.65) {
                ichk = 6;
            }
        }
        if (ichk > 0) {
            context.set(desc0, estimation0);
            plbox = plbox0;
            defModel = true;
        } else {
            rtval0 = rtval;
            rvr0 = rvr;
            plbox0 = plbox;
            reference = RegSarimaModelling.copyOf(context);
        }

        if (loop == 1) {
            pcr += .025;
        } else {
            pcr += .015;
        }
        ++loop;
        if (plbox <= pcr) {
            return true;
        }
        boolean ncv = false;
        if (loop == 2 && outliers != null) {
            if (curva > MINCV) {
                curva = Math.max(MINCV, curva * (1 - options.reduceVa));
                ncv = true;
            }
            needOutliers = true;
        }
        needAutoModelling = !defModel;

        if (loop > 2 || !ncv) {
            lastSolution(context);
            if (loop == 2) {
                loop = 3;
            }
        }
        return false;
    }

    // use the default model
    private void lastSolution(RegSarimaModelling context) {
        ModelDescription description = context.getDescription();
        SarimaOrders nspec = description.specification();
        switch (nspec.getPeriod()) {
            case 2 ->
                nspec.setP(1);
            case 3 ->
                nspec.setP(2);
            default ->
                nspec.setP(3);
        }
        if (nspec.getBd() > 0 || nspec.getPeriod() == 1) {
            nspec.setBp(0);
        }
        if (options.mixedModel) {
            nspec.setQ(1);
        } else {
            nspec.setQ(0);
        }

        if (nspec.getPeriod() > 1) {
            nspec.setBq(1);
        }
        context.setSpecification(nspec);
        context.estimate(options.precision);
        if (outliers != null) {
            curva = va0;
            needOutliers = true;
            // we should remove the outliers because the model could be different
            //description.removeVariable(var->var.isOutlier(false));
        }
        needAutoModelling = false;
    }

    private ProcessingResult regAIC(RegSarimaModelling context) {
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

    /**
     * Return true if the model is NOT modified
     *
     * @param context
     * @return
     */
    private boolean checkUnitRoots(RegSarimaModelling context) {
        ModelDescription desc = context.getDescription();
        //quasi-unit roots of ar are changed in true unit roots
        SarimaModel m = desc.arima();
        SarimaOrders nspec = m.orders();

        boolean ok = true;
        if (nspec.getP() > 0 && nspec.getD() < MAXD) {
            if (0 != searchur(m.getRegularAR().mirror().roots())) {
                nspec.setP(nspec.getP() - 1);
                nspec.setD(nspec.getD() + 1);
                ok = false;
            }
        }
        if (nspec.getBp() > 0 && nspec.getBd() < MAXBD) {
            if (0 != searchur(m.getSeasonalAR().mirror().roots())) {
                nspec.setBp(nspec.getBp() - 1);
                nspec.setBd(nspec.getBd() + 1);
                ok = false;
            }
        }
        if (!ok) {
            desc.setSpecification(nspec);
            redoEstimation(context);
        }
        return ok;
    }

    private void redoEstimation(RegSarimaModelling context) {
        context.estimate(options.precision);
        // check mean
        ModelDescription desc = context.getDescription();
        if (desc.isMean()) {
            checkMu(context, MeanController.CVAL1);
        }
        if (desc.variables().filter(v -> ModellingUtility.isOutlier(v, true)).findAny().isPresent()) {
            desc.removeVariable(v -> ModellingUtility.isOutlier(v, true));
            context.clearEstimation();
        }
        if (context.needEstimation()) {
            context.estimate(options.precision);
        }
        if (outliers != null) {
            outliers.process(context, curva);
        }
        if (context.needEstimation()) {
            context.estimate(options.precision);
        }
        ModelController controller = new ModelController(.95);
        controller.accept(context);
        rtval0 = controller.getRTval();
        rvr0 = controller.getRvr();
        plbox0 = 1 - controller.getLjungBoxTest().getPvalue();
        reference = RegSarimaModelling.copyOf(context);

    }

    private int searchur(final Complex[] r) {
        if (r == null) {
            return 0;
        }
        int n = 0;
        for (int i = 0; i < r.length; ++i) {
            double cdim = Math.abs(r[i].getIm());
            double vcur = r[i].abs();
            if (vcur > options.urLimit && cdim <= 0.05 && r[i].getRe() > 0) {
                ++n;
            }
        }
        return n;
    }

    /**
     * Return true if the model is NOT modified
     *
     * @param context
     * @return
     */
    private boolean checkMA(RegSarimaModelling context) {
        ModelDescription description = context.getDescription();
        SarimaModel m = description.arima();
        SarimaOrders nspec = m.orders();
        if (nspec.getQ() == 0 || nspec.getD() == 0) {
            return true;
        }
        double ma = m.getRegularMA().evaluateAt(1);
        if (Math.abs(ma) < MALIM) {
            nspec.setQ(nspec.getQ() - 1);
            nspec.setD(nspec.getD() - 1);
            description.setSpecification(nspec);
            description.setMean(true);
            redoEstimation(context);
            return false;
        }
        return true;
    }
}
