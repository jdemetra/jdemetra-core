/*
* Copyright 2020 National Bank of Belgium
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
package jdplus.regarima;

import jdplus.regarima.internal.ConcentratedLikelihoodComputer;
import jdplus.arima.estimation.IArimaMapping;
import jdplus.arima.IArimaModel;
import nbbrd.design.BuilderPattern;
import nbbrd.design.Development;
import jdplus.likelihood.LogLikelihoodFunction;
import jdplus.math.functions.levmar.LevenbergMarquardtMinimizer;
import jdplus.math.functions.ssq.SsqFunctionMinimizer;
import demetra.data.DoubleSeq;
import jdplus.regarima.internal.RegArmaEstimation;
import jdplus.regarima.internal.RegArmaProcessor;
import jdplus.sarima.estimation.SarimaMapping;

/**
 * Generic module for estimation of RegArima models
 * @author Jean Palate
 * @param <M>
 */
@Development(status = Development.Status.Beta)
public class GlsArimaProcessor<M extends IArimaModel> implements IRegArimaProcessor<M> {

    @BuilderPattern(GlsArimaProcessor.class)
    public static class Builder<M extends IArimaModel> {

        private IRegArimaInitializer<M> initializer;
        private IRegArimaFinalizer<M> finalizer;
        private double eps = 1e-9;
        private SsqFunctionMinimizer.Builder min;
        private boolean ml = true, mt = false, fast = true;

        public Builder<M> initializer(IRegArimaInitializer<M> initializer) {
            this.initializer = initializer;
            return this;
        }

        public Builder<M> finalizer(IRegArimaFinalizer<M> finalizer) {
            this.finalizer = finalizer;
            return this;
        }

        public Builder<M> minimizer(SsqFunctionMinimizer.Builder min) {
            this.min = min;
            return this;
        }

        public Builder<M> precision(double eps) {
            this.eps = eps;
            return this;
        }

        public Builder<M> useMaximumLikelihood(boolean ml) {
            this.ml = ml;
            return this;
        }

        public Builder<M> useParallelProcessing(boolean mt) {
            this.mt = mt;
            return this;
        }

        public Builder<M> computeExactFinalDerivatives(boolean exact) {
            this.fast = !exact;
            return this;
        }

        public GlsArimaProcessor<M> build() {
            return new GlsArimaProcessor(initializer, finalizer, min, eps, ml, mt, fast);
        }

    }

    public static <N extends IArimaModel> Builder<N> builder(Class<N> nclass) {
        return new Builder<>();
    }

    private final IRegArimaInitializer<M> initializer;
    private final IRegArimaFinalizer<M> finalizer;
    private final SsqFunctionMinimizer.Builder min;
    private final boolean ml, mt, fast;

    /**
     *
     */
    private GlsArimaProcessor(final IRegArimaInitializer<M> initializer, final IRegArimaFinalizer<M> finalizer, 
            final SsqFunctionMinimizer.Builder min, final double eps, final boolean ml, final boolean mt, final boolean fast) {
        this.initializer = initializer;
        this.finalizer = finalizer;
        if (min == null) {
            this.min = LevenbergMarquardtMinimizer.builder();
        } else {
            this.min = min;
        }
        this.min.functionPrecision(eps);
        this.ml = ml;
        this.mt = mt;
        this.fast = fast;
    }

    /**
     *
     * @param regs
     * @return
     */
    @Override
    public RegArimaEstimation<M> process(RegArimaModel<M> regs, IArimaMapping<M> mapping) {
        RegArimaModel<M> initial = initialize(regs, mapping);
        RegArimaEstimation<M> estimation = optimize(initial, mapping);
        if (estimation == null) {
            return null;
        }
        return finalize(estimation, mapping);
    }

    public RegArimaModel<M> initialize(RegArimaModel<M> regs, IArimaMapping<M> mapping) {
        RegArimaModel<M> start = null;
        if (initializer != null) {
            start = initializer.initialize(regs, mapping);
        }
        if (start == null) {
            return RegArimaModel.of(regs, mapping.getDefault());
        } else {
            return start;
        }
    }

    public RegArimaEstimation<M> finalize(RegArimaEstimation<M> estimation, IArimaMapping<M> mapping) {
        if (finalizer != null) {
            return finalizer.finalize(estimation, mapping);
        } else {
            return estimation;
        }
    }

    @Override
    public RegArimaEstimation<M> optimize(RegArimaModel<M> regs, IArimaMapping<M> mapping) {
        M arima = regs.arima();
        M arma = (M) arima.stationaryTransformation().getStationaryModel();
        IArimaMapping<M> stmapping = mapping.stationaryMapping();
        RegArmaModel<M> dmodel = regs.differencedModel();
        RegArmaProcessor processor = new RegArmaProcessor(ml, mt, fast);
        int ndf = dmodel.getY().length() - dmodel.getX().getColumnsCount();// - mapping.getDim();
        RegArmaEstimation<M> rslt = processor.compute(dmodel, stmapping.parametersOf(arma), stmapping, min.build(), ndf);
        M nmodel = mapping.map(DoubleSeq.of(rslt.getParameters()));
        RegArimaModel<M> nregs = regs.toBuilder().arima(nmodel).build();

        return RegArimaEstimation.<M>builder()
                .model(nregs)
                .concentratedLikelihood(ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(nregs))
                .max(new LogLikelihoodFunction.Point(RegArimaEstimation.concentratedLogLikelihoodFunction(mapping, regs), rslt.getParameters(), rslt.getScore(), rslt.getInformation()))
                .build();
    }

}
