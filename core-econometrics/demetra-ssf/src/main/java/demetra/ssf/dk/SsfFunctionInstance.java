/*
 * Copyright 2016 National Bank of Belgium
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
package demetra.ssf.dk;

import demetra.data.DataBlock;
import demetra.likelihood.ILikelihood;
import demetra.maths.functions.IFunction;
import demetra.maths.functions.IFunctionPoint;
import demetra.maths.functions.ssq.ISsqFunction;
import demetra.maths.functions.ssq.ISsqFunctionPoint;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.IConcentratedLikelihoodComputer;
import demetra.ssf.univariate.SsfRegressionModel;
import demetra.data.DoubleSequence;

/**
 *
 * @author Jean Palate
 * @param <S>
 */
public class SsfFunctionInstance<S, F extends ISsf> implements
        ISsqFunctionPoint, IFunctionPoint {

    /**
     *
     */
    private final F currentSsf;
    private final S current;

    /**
     *
     */
    private final DkConcentratedLikelihood ll;
    private final DataBlock p;
    private DataBlock E;
    private final SsfFunction<S, F> fn;

    /**
     *
     * @param fn
     * @param p
     */
    public SsfFunctionInstance(SsfFunction<S, F> fn, DoubleSequence p) {
        this.fn = fn;
        this.p = DataBlock.of(p);
        current=fn.getMapping().map(p);
        currentSsf = fn.getBuilder().buildSsf(current);
        boolean fastcomputer=fn.isFast() && !fn.isMissing() && currentSsf.isTimeInvariant();
        IConcentratedLikelihoodComputer<DkConcentratedLikelihood> computer= DkToolkit.concentratedLikelihoodComputer(true, fastcomputer);
        if (fn.getX() == null)
            ll=computer.compute(currentSsf, fn.getData());
        else
            ll=computer.compute(new SsfRegressionModel(currentSsf, fn.getData(), fn.getX(), fn.getDiffuseX()));
    }

    public F getSsf() {
        return currentSsf;
    }

    public S getCore() {
        return current;
    }

    @Override
    public DoubleSequence getE() {
        if (E == null) {
            DoubleSequence res = ll.e();
            if (res == null) {
                return null;
            } else {
                E = DataBlock.select(res, x->Double.isFinite(x));
                if (fn.isMaximumLikelihood()) {
                    double factor = Math.sqrt(ll.factor());
                    E.mul(factor);
                }
            }
        }
        return E;
    }

    /**
     *
     * @return
     */
    public ILikelihood getLikelihood() {
        return ll;
    }

    @Override
    public DoubleSequence getParameters() {
        return p;
    }

    @Override
    public double getSsqE() {
        if (ll == null) {
            return Double.NaN;
        }
        return fn.isMaximumLikelihood() ? ll.ssq() * ll.factor() : ll.ssq();
    }

    @Override
    public double getValue() {
        if (ll == null) {
            return Double.NaN;
        }
        if (fn.isLog()) {
            return fn.isMaximumLikelihood() ? -ll.logLikelihood() : Math.log(ll.ssq());
        } else {
            return fn.isMaximumLikelihood() ? ll.ssq() * ll.factor() : ll
                    .ssq();
        }
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
