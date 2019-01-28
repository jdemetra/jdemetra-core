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
package demetra.regarima;

import demetra.arima.estimation.IArimaMapping;
import demetra.arima.IArimaModel;
import demetra.data.DoubleSequence;
import demetra.design.BuilderPattern;
import demetra.design.Development;
import demetra.likelihood.LogLikelihoodFunction;
import demetra.maths.functions.IParametricMapping;
import demetra.maths.functions.levmar.LevenbergMarquardtMinimizer;
import demetra.maths.functions.ssq.ISsqFunctionMinimizer;
import demetra.regarima.internal.ConcentratedLikelihoodComputer;
import demetra.regarima.internal.RegArmaEstimation;
import demetra.regarima.internal.RegArmaProcessor;
import java.util.function.Function;

/**
 *
 * @author Jean Palate
 * @param <M>
 */
@Development(status = Development.Status.Alpha)
public class GlsArimaProcessor<M extends IArimaModel> implements IRegArimaProcessor<M> {

    @BuilderPattern(GlsArimaProcessor.class)
    public static class Builder<M extends IArimaModel> {

        private IArimaMapping<M> mapping;
        private IRegArimaInitializer<M> initializer;
        private IRegArimaFinalizer<M> finalizer;
        private double eps = 1e-9;
        private ISsqFunctionMinimizer min;
        private boolean ml = true, mt = false, fast = true;

        public Builder<M> mapping(IArimaMapping<M> mapping) {
            this.mapping = mapping;
            return this;
        }

        public Builder<M> initializer(IRegArimaInitializer<M> initializer) {
            this.initializer = initializer;
            return this;
        }

        public Builder<M> finalizer(IRegArimaFinalizer<M> finalizer) {
            this.finalizer = finalizer;
            return this;
        }

        public Builder<M> minimizer(ISsqFunctionMinimizer min) {
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
            if (mapping == null)
                throw new IllegalArgumentException();
            return new GlsArimaProcessor(mapping, initializer, finalizer, min, eps, ml, mt, fast);
        }

    }

    public static <N extends IArimaModel> Builder<N> builder(Class<N> nclass) {
        return new Builder<>();
    }

    private final IArimaMapping<M> mapping;
    private final IRegArimaInitializer<M> initializer;
    private final IRegArimaFinalizer<M> finalizer;
    private final ISsqFunctionMinimizer min;
    private final boolean ml, mt, fast;

    /**
     *
     */
    private GlsArimaProcessor(IArimaMapping<M> mapping,
            final IRegArimaInitializer<M> initializer, final IRegArimaFinalizer<M> finalizer, final ISsqFunctionMinimizer min,
            final double eps, final boolean ml, final boolean mt, final boolean fast) {
        this.mapping = mapping;
        this.initializer = initializer;
        this.finalizer = finalizer;
        if (min == null) {
            this.min = new LevenbergMarquardtMinimizer();
        } else {
            this.min = min;
        }
        this.min.setFunctionPrecision(eps);
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
    public RegArimaEstimation<M> process(RegArimaModel<M> regs) {
        RegArimaEstimation<M> estimation = optimize(initialize(regs));
        if (estimation == null) {
            return null;
        }
        return finalize(estimation);
    }

    public RegArimaModel<M> initialize(RegArimaModel<M> regs) {
        RegArimaModel<M> start = null;
        if (initializer != null) {
            start = initializer.initialize(regs);
        }
        if (start == null) {
            return RegArimaModel.of(regs, mapping.getDefault());
        } else {
            return start;
        }
    }

    public RegArimaEstimation<M> finalize(RegArimaEstimation<M> estimation) {
        if (finalizer != null) {
            return finalizer.finalize(estimation);
        } else {
            return estimation;
        }
    }

    @Override
    public RegArimaEstimation<M> optimize(RegArimaModel<M> regs) {
        M arima = regs.arima();
        M arma = (M) arima.stationaryTransformation().getStationaryModel();
        IArimaMapping<M> stmapping = mapping.stationaryMapping();
        RegArmaModel<M> dmodel = regs.differencedModel();
        RegArmaProcessor processor = new RegArmaProcessor(ml, mt, fast);
        int ndf = dmodel.getY().length() - dmodel.getX().getColumnsCount();// - mapping.getDim();
        RegArmaEstimation<M> rslt = processor.compute(dmodel, stmapping.parametersOf(arma), stmapping, min, ndf);
        M nmodel = mapping.map(DoubleSequence.ofInternal(rslt.getParameters()));
        RegArimaModel<M> nregs = regs.toBuilder().arima(nmodel).build();

        return new RegArimaEstimation(nregs, ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(nregs),
                new LogLikelihoodFunction.Point(RegArimaEstimation.concentratedLogLikelihoodFunction(mapping, regs), rslt.getParameters(), rslt.getGradient(), rslt.getHessian()));
    }

}
