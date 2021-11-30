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
package jdplus.regarima.estimation;

import jdplus.regarima.RegArmaModel;
import jdplus.arima.IArimaModel;
import nbbrd.design.BuilderPattern;
import jdplus.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.likelihood.DefaultLikelihoodEvaluation;
import jdplus.math.functions.IFunction;
import jdplus.math.functions.IFunctionPoint;
import jdplus.math.functions.IParametersDomain;
import jdplus.math.functions.IParametricMapping;
import java.util.function.ToDoubleFunction;
import demetra.data.DoubleSeq;
import jdplus.arima.estimation.IArimaMapping;
import jdplus.likelihood.Likelihood;
import jdplus.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 * @param <S>
 */
public class RegArmaFunction<S extends IArimaModel> implements IFunction {

    @BuilderPattern(RegArmaFunction.class)
    public static class Builder<S extends IArimaModel> {

        // algorithms
        private ConcentratedLikelihoodComputer cll = ConcentratedLikelihoodComputer.DEFAULT_FULL_COMPUTER;
        private ToDoubleFunction<Likelihood> eval = DefaultLikelihoodEvaluation.deviance();
        private boolean mt = false;
        // model
        private final DoubleSeq dy;
        private FastMatrix x;
        private int nmissing;
        // mapping
        private IArimaMapping<S> mapping;

        private Builder(final DoubleSeq dy) {
            this.dy = dy;
        }

        public Builder variables(FastMatrix x) {
            this.x = x;
            return this;
        }

        public Builder missingCount(int nm) {
            this.nmissing = nm;
            return this;
        }

        public Builder parallelProcessing(boolean parallel) {
            this.mt = parallel;
            return this;
        }

        public Builder likelihoodComputer(ConcentratedLikelihoodComputer computer) {
            this.cll = computer;
            return this;
        }

        public Builder mapping(IArimaMapping<S> mapping) {
            this.mapping = mapping;
            return this;
        }

        public Builder likelihoodEvaluation(ToDoubleFunction<Likelihood> eval) {
            this.eval = eval;
            return this;
        }

        public RegArmaFunction<S> build() {
            return new RegArmaFunction<>(dy, x, nmissing, mapping, cll, eval, mt);
        }
    }

    public static <S extends IArimaModel> Builder<S> builder(DoubleSeq y) {
        return new Builder<>(y);
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
            final IArimaMapping<S> mapping,
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
    public Evaluation<S> evaluate(DoubleSeq parameters) {
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
            RegArmaModel<S> regarma = new RegArmaModel<>(fn.dy, arma, fn.nmissing, fn.x);
            ll = fn.cll.compute(regarma);
        }

//        public ConcentratedLikelihoodWithMissing getLikelihood() {
//            return ll;
//        }

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
