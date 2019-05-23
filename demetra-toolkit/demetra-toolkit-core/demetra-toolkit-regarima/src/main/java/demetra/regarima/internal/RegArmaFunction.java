/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.regarima.internal;

import demetra.regarima.RegArmaModel;
import demetra.arima.IArimaModel;
import demetra.design.BuilderPattern;
import demetra.likelihood.ConcentratedLikelihoodWithMissing;
import demetra.likelihood.DefaultLikelihoodEvaluation;
import demetra.maths.functions.IFunction;
import demetra.maths.functions.IFunctionPoint;
import demetra.maths.functions.IParametersDomain;
import demetra.maths.functions.IParametricMapping;
import java.util.function.ToDoubleFunction;
import demetra.data.DoubleSeq;
import demetra.likelihood.Likelihood;
import jdplus.maths.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 * @param <S>
 */
class RegArmaFunction<S extends IArimaModel> implements IFunction {

    @BuilderPattern(RegArmaFunction.class)
    public static class Builder<S extends IArimaModel> {

        // algorithms
        private boolean ml = true;
        private ConcentratedLikelihoodComputer cll = ConcentratedLikelihoodComputer.DEFAULT_COMPUTER;
        private boolean mt = false;
        // model
        private final DoubleSeq dy;
        private FastMatrix x;
        private int nmissing;
        // mapping
        private IParametricMapping<S> mapping;

        private Builder(final DoubleSeq dy) {
            this.dy = dy;
        }

        public Builder variables(FastMatrix x) {
            this.x = x;
            return this;
        }

        public Builder nmissing(int nm) {
            this.nmissing = nm;
            return this;
        }

        public Builder parallelProcessing(boolean parallel) {
            this.mt = parallel;
            return this;
        }

        public Builder maximumLikelihood(boolean ml) {
            this.ml = ml;
            return this;
        }

        public Builder likelihoodComputer(ConcentratedLikelihoodComputer computer) {
            this.cll = computer;
            return this;
        }

        public RegArmaFunction<S> build() {
            return new RegArmaFunction<>(dy, x, nmissing, mapping, cll,
                    ml ? DefaultLikelihoodEvaluation.logSsq() : DefaultLikelihoodEvaluation.ml(), mt);
        }
    }

    // model
    final DoubleSeq dy;
    final FastMatrix x;
    final int nmissing;
    // mapping
    final IParametricMapping<S> mapping;
    // algorithms
    final ConcentratedLikelihoodComputer cll;
    final ToDoubleFunction<Likelihood> ll;
    final boolean mt;

    private RegArmaFunction(final DoubleSeq dy,
            final FastMatrix x,
            final int nm,
            final IParametricMapping<S> mapping,
            final ConcentratedLikelihoodComputer cll,
            final ToDoubleFunction<Likelihood> ll,
            final boolean mt) {
        this.dy = dy;
        this.x = x;
        this.nmissing = nm;
        this.mapping = mapping;
        this.cll = cll;
        this.ll = ll;
        this.mt = mt;
    }

    @Override
    public IParametersDomain getDomain() {
        return mapping;
    }

    @Override
    public IFunctionPoint evaluate(DoubleSeq parameters) {
        return new Evaluation(this, parameters);
    }

    public static class Evaluation<S extends IArimaModel> implements IFunctionPoint {

        final RegArmaFunction<S> fn;
        final DoubleSeq p;
        final S arma;
        final ConcentratedLikelihoodWithMissing ll;

        public Evaluation(RegArmaFunction<S> fn, DoubleSeq p) {
            this.fn = fn;
            this.p = p;
            this.arma = fn.mapping.map(p);
            RegArmaModel<S> regarma = new RegArmaModel<>(fn.dy, arma, fn.x, fn.nmissing);
            ll = fn.cll.compute(regarma);
        }

        public ConcentratedLikelihoodWithMissing getLikelihood() {
            return ll;
        }

        @Override
        public DoubleSeq getParameters() {
            return p;
        }

        @Override
        public double getValue() {
            return fn.ll.applyAsDouble(ll);
        }

        @Override
        public IFunction getFunction() {
            return fn;
        }

    }

}
