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
import demetra.maths.Complex;
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
import demetra.regarima.regular.ModelEstimation;
import demetra.regarima.regular.RegressionVariablesTest;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class X12Preprocessor implements IPreprocessor {

    @lombok.Value
    @lombok.Builder
    public static class AmiOptions {

        @lombok.Builder.Default
        boolean checkMu = true;
        @lombok.Builder.Default
        double precision = 1e-7;
        @lombok.Builder.Default
        double va = 0;
        @lombok.Builder.Default
        double reduceVa = .14286;
        @lombok.Builder.Default
        double ljungBoxLimit = .95;
        @lombok.Builder.Default
        double urLimit = .95;
        @lombok.Builder.Default
        boolean acceptAirline = false;
        @lombok.Builder.Default
        boolean mixedModel = true;
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
        private RegressionVariablesTest regressionTest0, regressionTest1;
        private AmiOptions options = new AmiOptions(true, 1e-7, 0, .14286, .95, .95, false, true);

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

        public Builder initialRegressionTest(RegressionVariablesTest test0) {
            this.regressionTest0 = test0;
            return this;
        }

        public Builder finalRegressionTest(RegressionVariablesTest test1) {
            this.regressionTest1 = test1;
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

    private static final double FCT = 1 / (1 - .0125), FCT2 = 1, MALIM = .001;
    private static final double MINCV = 2.8;
    private static final int MAXD = 2, MAXBD = 1;

    private final IModelBuilder modelBuilder;
    private final ILogLevelModule transformation;
    private final IRegressionModule calendarTest, easterTest;
    private final IOutliersDetectionModule outliers;
    private final AmiOptions options;
    private final IDifferencingModule differencing;
    private final IArmaModule arma;
    private final RegressionVariablesTest regressionTest0, regressionTest1;
    private FinalEstimator finalEstimator;

    private double va0 = 0, curva = 0;
    private double pcr;
    private PreprocessingModel reference;
    private boolean needOutliers;
    private boolean needAutoModelling;
    private int loop, round;
    private double plbox0, rvr0, rtval0;

    private X12Preprocessor(Builder builder) {
        this.modelBuilder = builder.modelBuilder;
        this.transformation = builder.transformation;
        this.calendarTest = builder.calendarTest;
        this.easterTest = builder.easterTest;
        this.outliers = builder.outliers;
        this.options = builder.options;
        this.differencing = builder.differencing;
        this.arma = builder.arma;
        this.regressionTest0 = builder.regressionTest0;
        this.regressionTest1 = builder.regressionTest1;
        if (differencing != null || arma != null) {
            finalEstimator = new FinalEstimator(options.precision);
        } else {
            finalEstimator = null;
        }
    }

    private void clear() {
        loop = round = 0;
        plbox0 = rvr0 = rtval0 = 0;
        reference = null;
        curva = 0;
        pcr = options.ljungBoxLimit;
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
            va0 = options.getVa();
            if (va0 == 0) {
                va0 = X12Utility.calcCv(desc.getSeries().getDomain().getLength());
            }
            curva = va0;
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
                regressionTest0.process(context);
            }
            if (isAutoModelling()) {
                if (context.needEstimation()) {
                    context.estimate(options.precision);
                }
                reference = context.build();
                ModelController controller = new ModelController(.95);
                boolean ok = controller.accept(context);
                plbox0 = 1 - controller.getLjungBoxTest().getPValue();
                rvr0 = controller.getRvr();
                rtval0 = controller.getRTval();
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
                        if (outliers != null && loop <= 2) {
                            if (!pass2(defModel, context)) {
                                continue;
                            }
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
                            }
                        }
                        // final tests
                        checkUnitRoots(context);
                        checkMA(context);
                        if (isAutoModelling() && !context.getDescription().isMean() && Math.abs(rtval0) > 2.5) {
                            if (options.checkMu) {
                                context.getDescription().setMean(true);
                                context.setEstimation(null);
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
                context.estimate(options.precision);
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

    private boolean pass2(boolean defModel, RegArimaModelling context) {
        int ichk = 0;
        ModelDescription desc = context.getDescription();
        int naut = (int) desc.variables().filter(v -> v.isOutlier(false)).count();

        ModelDescription desc0 = reference.getDescription();
        ModelEstimation estimation0 = reference.getEstimation();

        int naut0 = (int) desc0.variables().filter(v -> v.isOutlier(false)).count();
        SarimaSpecification spec0 = desc0.getSpecification(),
                spec = desc.getSpecification();
        SarimaModel arima = desc.arima();
        boolean mu0 = desc0.isEstimatedMean(),
                mu = desc.isEstimatedMean();
        ModelController controller = new ModelController(.95);
        controller.accept(context);
        double rtval = controller.getRTval();
        double rvr = controller.getRvr();
        double plbox = 1 - controller.getLjungBoxTest().getPValue();
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
            context.setDescription(desc0);
            context.setEstimation(estimation0);
            plbox = plbox0;
            rvr = rvr0;
            defModel = true;
        } else {
            rtval0 = rtval;
            rvr0 = rvr;
            plbox0 = plbox;
            reference = context.build();
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
    private void lastSolution(RegArimaModelling context) {
        ModelDescription description = context.getDescription();
        SarimaSpecification nspec = description.getSpecification();
        nspec.setP(3);
        if (nspec.getBd() > 0) {
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
        description.setSpecification(nspec);
        if (outliers != null) {
            curva = va0;
            needOutliers = true;
            // we should remove the outliers because the model could be different
            //description.removeVariable(var->var.isOutlier(false));
        }
        context.estimate(options.precision);
        needAutoModelling = false;
    }

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

    private void checkUnitRoots(RegArimaModelling context) {
        ModelDescription desc = context.getDescription();
        //quasi-unit roots of ar are changed in true unit roots
        SarimaModel m = desc.arima();
        SarimaSpecification nspec = m.specification();

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
    }

    private void redoEstimation(RegArimaModelling context) {
        context.estimate(options.precision);
        // check mean
        ModelDescription desc = context.getDescription();
        if (desc.isEstimatedMean()) {
            checkMu(context, false);
        }
        if (desc.variables().filter(v -> v.isOutlier(false)).findAny().isPresent()) {
            desc.removeVariable(v -> v.isOutlier(false));
            context.setEstimation(null);
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
        plbox0 = 1 - controller.getLjungBoxTest().getPValue();
        reference = context.build();

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

    private void checkMA(RegArimaModelling context) {
        ModelDescription description = context.getDescription();
        SarimaModel m = description.arima();
        SarimaSpecification nspec = m.specification();
        if (nspec.getQ() == 0 || nspec.getD() == 0) {
            return;
        }
        double ma = m.getRegularMA().evaluateAt(1);
        if (Math.abs(ma) < MALIM) {
            nspec.setQ(nspec.getQ() - 1);
            nspec.setD(nspec.getD() - 1);
            description.setSpecification(nspec);
            description.setMean(true);
            redoEstimation(context);
        }
    }
}