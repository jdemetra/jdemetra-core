/*
 * Copyright 2023 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.sa.regarima;

import demetra.DemetraException;
import demetra.modelling.regular.ModellingSpec;
import nbbrd.design.Development;
import jdplus.regsarima.regular.IRegressionModule;
import jdplus.regsarima.regular.ModelDescription;
import jdplus.regsarima.regular.RegSarimaModelling;
import jdplus.regsarima.regular.RegSarimaProcessor;
import demetra.processing.ProcessingLog;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.ModellingContext;
import jdplus.regsarima.regular.ProcessingResult;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaUtility;
import jdplus.regsarima.regular.ILogLevelModule;
import jdplus.regsarima.regular.IModelBuilder;
import jdplus.regsarima.regular.IOutliersDetectionModule;
import jdplus.regsarima.regular.RegSarimaModel;
import jdplus.regsarima.regular.RegressionVariablesTest;
import jdplus.sarima.SarimaModel;
import nbbrd.design.BuilderPattern;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class FastKernel implements RegSarimaProcessor {

    private static final String DEMETRA = "demetra";

    @lombok.Value
    @lombok.Builder
    public static class AmiOptions {

        boolean checkMu;
        double cval;
        double va;
        double precision, intermediatePrecision;

        public static Builder builder() {
            return new Builder()
                    .cval(2)
                    .intermediatePrecision(1e-5)
                    .precision(1e-7);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private static final AmiOptions DEFAULT = AmiOptions.builder().build();

    @BuilderPattern(FastKernel.class)
    public static class Builder {

        private IModelBuilder modelBuilder;
        private ILogLevelModule transformation;
        private IRegressionModule calendarTest, easterTest;
        private IOutliersDetectionModule outliers;
        private RegressionVariablesTest regressionTest0, regressionTest1;
        private AmiOptions options = DEFAULT;

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

        public FastKernel build() {
            FastKernel processor = new FastKernel(this);
            return processor;
        }

    }

    private final IModelBuilder modelBuilder;
    private final ILogLevelModule transformation;
    private final IRegressionModule calendarTest, easterTest;
    private final IOutliersDetectionModule outliers;
    private final RegressionVariablesTest regressionTest0, regressionTest1;
    private final AmiOptions options;
    private final ModelEstimator finalEstimator;

    private double curva = 0;

    private FastKernel(Builder builder) {
        this.modelBuilder = builder.modelBuilder;
        this.transformation = builder.transformation;
        this.calendarTest = builder.calendarTest;
        this.easterTest = builder.easterTest;
        this.outliers = builder.outliers;
        this.options = builder.options;
        this.regressionTest0 = builder.regressionTest0;
        this.regressionTest1 = builder.regressionTest1;
        finalEstimator = ModelEstimator.builder()
                .precision(options.precision)
                .build();
    }

    public static FastKernel of(ModellingSpec spec, ModellingContext context) {
        if (! spec.isEnabled())
            return null;
        SpecDecoder helper = new SpecDecoder(spec, context);
        return helper.buildProcessor();
    }

    @Override
    public RegSarimaModel process(TsData originalTs, ProcessingLog log) {
        if (log == null) {
            log = ProcessingLog.dummy();
        }
        clear();
        log.push(DEMETRA);
        ModelDescription desc = modelBuilder.build(originalTs, log);
        if (desc == null) {
            throw new DemetraException("Initialization failed");
        }
        if (outliers != null) {
            curva = options.getVa();
            if (curva == 0) {
                curva = DemetraUtility.calcCv(desc.getEstimationDomain().getLength());
            }
        }
        RegSarimaModelling modelling = RegSarimaModelling.of(desc, log);
        RegSarimaModel rslt = calc(modelling, log);
        log.pop();

        return rslt;
    }

    private void clear() {
    }

    private RegSarimaModel calc(RegSarimaModelling modelling, ProcessingLog log) {

        if (transformation != null) {
            transformation.process(modelling);
        } else if (modelling.getDescription().isLogTransformation()) {
            if (modelling.getDescription().getSeries().getValues().anyMatch(x -> x <= 0)) {
                modelling.getLog().warning("logs changed to levels");
                modelling.getDescription().setLogTransformation(false);
                modelling.clearEstimation();
            }
        }

        regAIC(modelling);
        checkMu(modelling, options.cval);
        if (outliers != null && ProcessingResult.Changed == outliers.process(modelling, curva)) {
            if (modelling.needEstimation()) {
                modelling.estimate(options.precision);
            }
            regressionTest0.process(modelling);
        }

        finalEstimator.estimate(modelling);

        return modelling.build();
    }

    private ProcessingResult regAIC(RegSarimaModelling modelling) {
        ProcessingResult rslt = ProcessingResult.Unchanged;
        if (calendarTest != null && calendarTest.test(modelling) == ProcessingResult.Changed) {
            rslt = ProcessingResult.Changed;
        }
        if (easterTest != null && easterTest.test(modelling) == ProcessingResult.Changed) {
            rslt = ProcessingResult.Changed;
        }
//        if (userTest != null && userTest.process(context) == ProcessingResult.Changed) {
//            rslt = ProcessingResult.Changed;
//        }
        return rslt;
    }
  

    private ProcessingResult checkMu(RegSarimaModelling modelling, double cv) {
        if (!options.checkMu) {
            return ProcessingResult.Unchanged;
        }
        ModelDescription desc = modelling.getDescription();
        RegArimaEstimation<SarimaModel> est = modelling.getEstimation();
        boolean mean = desc.isMean();
        if (!mean) {
            desc = ModelDescription.copyOf(desc);
            desc.setMean(true);
            est = null;
        }
        if (est == null) {
            est = desc.estimate(RegArimaUtility.processor(true, options.getIntermediatePrecision()));
        }
        double t = est.getConcentratedLikelihood().tstat(0, 0, false);
        boolean nmean = Math.abs(t) > cv;
        if (nmean == mean) {
            return ProcessingResult.Unchanged;
        } else {
            modelling.getDescription().setMean(nmean);
            modelling.clearEstimation();
            return ProcessingResult.Changed;
        }
    }

}
