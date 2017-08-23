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
package demetra.arima.regarima.internals;

import demetra.arima.regarima.internals.ArmaEvaluation;
import demetra.arima.IArimaModel;
import demetra.arima.regarima.ConcentratedLikelihoodComputer;
import demetra.data.DoubleSequence;
import demetra.design.Development;
import demetra.design.IBuilder;
import demetra.likelihood.DefaultLikelihoodEvaluation;
import demetra.likelihood.ILikelihood;
import demetra.maths.functions.IFunction;
import demetra.maths.functions.IParametersDomain;
import demetra.maths.functions.IParametricMapping;
import demetra.maths.functions.ssq.ISsqFunction;
import demetra.maths.matrices.Matrix;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

/**
 * @author Jean Palate
 * @param <S>
 */
@Development(status = Development.Status.Alpha)
public class ArmaFunction<S extends IArimaModel> implements ISsqFunction, IFunction {

    public static class Builder<S extends IArimaModel> implements IBuilder<ArmaFunction<S>> {

        public static final ToDoubleFunction<ILikelihood> LL = DefaultLikelihoodEvaluation.ml();
        public static final Function<ILikelihood, DoubleSequence> ERRORS = DefaultLikelihoodEvaluation.v();
        public static final ToDoubleFunction<ILikelihood> SSQLL = DefaultLikelihoodEvaluation.deviance();

        // algorithms
        private ToDoubleFunction<ILikelihood> ll = LL;
        private ToDoubleFunction<ILikelihood> ssqll = SSQLL;
        private Function<ILikelihood, DoubleSequence> errors = ERRORS;
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

        public Builder parallelProcessinf(boolean parallel) {
            this.mt = parallel;
            return this;
        }

        public Builder evaluation(ToDoubleFunction<ILikelihood> ll) {
            this.ll = ll;
            return this;
        }

     public Builder ssqEvaluation(ToDoubleFunction<ILikelihood> ssqll) {
            this.ssqll = ssqll;
            return this;
        }

     public Builder evaluation(Function<ILikelihood, DoubleSequence> errors) {
            this.errors = errors;
            return this;
        }

        public Builder computer(ConcentratedLikelihoodComputer computer) {
            this.cll = computer;
            return this;
        }

        @Override
        public ArmaFunction<S> build() {
            return new ArmaFunction<>(dy, x, nmissing, mapping, cll, ll, ssqll, errors, mt);
        }

    }

    public static <S extends IArimaModel> Builder<S> builder(DoubleSequence dy) {
        return new Builder<>(dy);
    }

    // model
    final DoubleSequence dy;
    final Matrix x;
    final int nmissing;
    // mapping
    final IParametricMapping<S> mapping;
    // algorithms
    final ConcentratedLikelihoodComputer cll;
    final ToDoubleFunction<ILikelihood> ll, ssqll;
    final Function<ILikelihood, DoubleSequence> errors;
    final boolean mt;

    private ArmaFunction(final DoubleSequence dy,
            final Matrix x,
            final int nm,
            final IParametricMapping<S> mapping,
            final ConcentratedLikelihoodComputer cll,
            final ToDoubleFunction<ILikelihood> ll,
            final ToDoubleFunction<ILikelihood> ssqll,
            final Function<ILikelihood, DoubleSequence> errors,
            final boolean mt) {
        this.dy = dy;
        this.x = x;
        this.nmissing = nm;
        this.mapping = mapping;
        this.cll = cll;
        this.ll = ll;
        this.ssqll=ssqll;
        this.errors = errors;
        this.mt=mt;
    }

    @Override
    public ArmaEvaluation<S> evaluate(DoubleSequence parameters) {
         return new ArmaEvaluation<>(this, parameters);
    }

    @Override
    public IParametersDomain getDomain() {
        return mapping;
    }

    @Override
    public ArmaEvaluation<S> ssqEvaluate(DoubleSequence parameters) {
        return new ArmaEvaluation<>(this, parameters);
    }

}
