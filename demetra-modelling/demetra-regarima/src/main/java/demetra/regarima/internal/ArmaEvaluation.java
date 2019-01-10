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
package demetra.regarima.internal;

import demetra.regarima.RegArmaModel;
import demetra.arima.IArimaModel;
import demetra.data.DoubleSequence;
import demetra.design.Development;
import demetra.likelihood.ConcentratedLikelihood;
import demetra.maths.functions.IFunction;
import demetra.maths.functions.IFunctionPoint;
import demetra.maths.functions.ssq.ISsqFunction;
import demetra.maths.functions.ssq.ISsqFunctionPoint;

/**
 * @author Jean Palate
 * @param <S> Specific arima model type
 */
@Development(status = Development.Status.Alpha)
class ArmaEvaluation<S extends IArimaModel> implements ISsqFunctionPoint,
        IFunctionPoint {

    final ArmaFunction<S> fn;
    final DoubleSequence p;
    final S arma;
    final ConcentratedLikelihood ll;

    public ArmaEvaluation(ArmaFunction<S> fn, DoubleSequence p) {
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
    public double getValue() {
        return fn.ll.applyAsDouble(ll);
    }

    @Override
    public ISsqFunction getSsqFunction() {
        return fn;
    }

    @Override
    public IFunction getFunction() {
        return fn;
    }
}
