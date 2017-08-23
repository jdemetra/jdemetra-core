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
package demetra.arima.regarima.internals;

import demetra.arima.IArimaModel;
import demetra.arima.regarima.ConcentratedLikelihoodComputer;
import demetra.data.DoubleSequence;
import demetra.design.IBuilder;
import demetra.likelihood.ConcentratedLikelihood;
import demetra.likelihood.DefaultLikelihoodEvaluation;
import demetra.likelihood.ILikelihood;
import demetra.maths.functions.IFunction;
import demetra.maths.functions.IFunctionPoint;
import demetra.maths.functions.IParametersDomain;
import demetra.maths.functions.IParametricMapping;
import demetra.maths.matrices.Matrix;
import java.util.function.ToDoubleFunction;

/**
 *
 * @author Jean Palate
 * @param <S>
 */
public class RegArmaFunction<S extends IArimaModel> implements IFunction {

    public static class Builder<S extends IArimaModel> implements IBuilder<RegArmaFunction<S>> {

        // algorithms
        private boolean ml = true;
        private ConcentratedLikelihoodComputer cll = ConcentratedLikelihoodComputer.DEFAULT_COMPUTER;
        private boolean mt = false;
        // model
        private final DoubleSequence dy;
        private Matrix x;
        private int nmissing;
        // mapping
        private IParametricMapping<S> mapping;

        private Builder(final DoubleSequence dy) {
            this.dy = dy;
        }

        public Builder variables(Matrix x) {
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

        @Override
        public RegArmaFunction<S> build() {
            return new RegArmaFunction<>(dy, x, nmissing, mapping, cll,
                    ml ? DefaultLikelihoodEvaluation.logSsq() : DefaultLikelihoodEvaluation.ml(), mt);
        }
    }

    // model
    final DoubleSequence dy;
    final Matrix x;
    final int nmissing;
    // mapping
    final IParametricMapping<S> mapping;
    // algorithms
    final ConcentratedLikelihoodComputer cll;
    final ToDoubleFunction<ILikelihood> ll;
    final boolean mt;

    private RegArmaFunction(final DoubleSequence dy,
            final Matrix x,
            final int nm,
            final IParametricMapping<S> mapping,
            final ConcentratedLikelihoodComputer cll,
            final ToDoubleFunction<ILikelihood> ll,
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
    public IFunctionPoint evaluate(DoubleSequence parameters) {
        return new Evaluation(this, parameters);
    }

    public static class Evaluation<S extends IArimaModel> implements IFunctionPoint {

        final RegArmaFunction<S> fn;
        final DoubleSequence p;
        final S arma;
        final ConcentratedLikelihood ll;

        public Evaluation(RegArmaFunction<S> fn, DoubleSequence p) {
            this.fn = fn;
            this.p = p;
            this.arma = fn.mapping.map(p);
            RegArmaModel<S> regarma = new RegArmaModel<>(fn.dy, arma, fn.x, fn.nmissing);
            ll = fn.cll.compute(regarma).getLikelihood();
        }

        public ConcentratedLikelihood getLikelihood() {
            return ll;
        }

        @Override
        public DoubleSequence getParameters() {
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
