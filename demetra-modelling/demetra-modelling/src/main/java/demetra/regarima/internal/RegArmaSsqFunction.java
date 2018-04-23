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
import demetra.data.DoubleSequence;
import demetra.design.Development;
import demetra.design.IBuilder;
import demetra.likelihood.ConcentratedLikelihood;
import demetra.likelihood.DefaultLikelihoodEvaluation;
import demetra.likelihood.ILikelihood;
import demetra.maths.functions.IParametersDomain;
import demetra.maths.functions.IParametricMapping;
import demetra.maths.functions.ssq.ISsqFunction;
import demetra.maths.functions.ssq.ISsqFunctionPoint;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import demetra.maths.MatrixType;

/**
 *
 * @author Jean Palate
 * @param <S>
 */
@Development(status=Development.Status.Alpha)
class RegArmaSsqFunction<S extends IArimaModel> implements ISsqFunction {

    static class SsqBuilder<S extends IArimaModel> implements IBuilder<RegArmaSsqFunction<S>> {

        // algorithms
        private boolean ml = true;
        private ConcentratedLikelihoodComputer cll = ConcentratedLikelihoodComputer.DEFAULT_COMPUTER;
        private boolean mt = false;
        // model
        private final DoubleSequence dy;
        private MatrixType x;
        private int nmissing;
        // mapping
        private IParametricMapping<S> mapping;

        private SsqBuilder(final DoubleSequence dy) {
            this.dy = dy;
        }

        SsqBuilder variables(MatrixType x) {
            this.x = x;
            return this;
        }

        SsqBuilder missingCount(int nm) {
            this.nmissing = nm;
            return this;
        }

        SsqBuilder mapping(IParametricMapping<S> mapping) {
            this.mapping=mapping;
            return this;
        }

        SsqBuilder parallelProcessing(boolean parallel) {
            this.mt = parallel;
            return this;
        }

        SsqBuilder maximumLikelihood(boolean ml) {
            this.ml = ml;
            return this;
        }

        SsqBuilder likelihoodComputer(ConcentratedLikelihoodComputer computer) {
            this.cll = computer;
            return this;
        }

        @Override
        public RegArmaSsqFunction<S> build() {
            return new RegArmaSsqFunction<>(dy, x, nmissing, mapping, cll,
                    ml ? DefaultLikelihoodEvaluation.v() : DefaultLikelihoodEvaluation.errors(),
                    ml ? DefaultLikelihoodEvaluation.deviance() : DefaultLikelihoodEvaluation.ssq(), mt);
        }
    }

    public static <S extends IArimaModel> SsqBuilder<S> builder(DoubleSequence y){
        return new SsqBuilder<>(y);
    }
    
    // model
    final DoubleSequence dy;
    final MatrixType x;
    final int nmissing;
    // mapping
    final IParametricMapping<S> mapping;
    // algorithms
    final ConcentratedLikelihoodComputer cll;
    final ToDoubleFunction<ILikelihood> ssqll;
    final Function<ILikelihood, DoubleSequence> errors;
    final boolean mt;

    private RegArmaSsqFunction(final DoubleSequence dy,
            final MatrixType x,
            final int nm,
            final IParametricMapping<S> mapping,
            final ConcentratedLikelihoodComputer cll,
            final Function<ILikelihood, DoubleSequence> errors,
            final ToDoubleFunction<ILikelihood> ssqll,
            final boolean mt) {
        this.dy = dy;
        this.x = x;
        this.nmissing = nm;
        this.mapping = mapping;
        this.cll = cll;
        this.ssqll = ssqll;
        this.errors = errors;
        this.mt = mt;
    }

    @Override
    public IParametersDomain getDomain() {
        return mapping;
    }

    @Override
    public Evaluation<S> ssqEvaluate(DoubleSequence parameters) {
        return new Evaluation<>(this, parameters);
    }

    static class Evaluation<S extends IArimaModel> implements ISsqFunctionPoint {

        final RegArmaSsqFunction<S> fn;
        final DoubleSequence p;
        final S arma;
        final ConcentratedLikelihood ll;

        public Evaluation(RegArmaSsqFunction<S> fn, DoubleSequence p) {
            this.fn = fn;
            this.p = p;
            this.arma = fn.mapping.map(p);
            RegArmaModel<S> regarma = new RegArmaModel<>(fn.dy, arma, fn.x, fn.nmissing);
            ll = fn.cll.compute(regarma);
        }

        @Override
        public DoubleSequence getE() {
            return fn.errors.apply(ll);
        }

        public ConcentratedLikelihood getLikelihood() {
            return ll;
        }

        @Override
        public DoubleSequence getParameters() {
            return p;
        }

        @Override
        public double getSsqE() {
            return fn.ssqll.applyAsDouble(ll);
        }

        @Override
        public ISsqFunction getSsqFunction() {
            return fn;
        }

    }

}
