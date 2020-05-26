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
package jdplus.ssf.likelihood;

import jdplus.data.DataBlock;
import jdplus.math.functions.IFunction;
import jdplus.math.functions.ssq.ISsqFunction;
import jdplus.math.functions.IFunctionDerivatives;
import jdplus.math.functions.NumericalDerivatives;
import jdplus.math.functions.ssq.ISsqFunctionDerivatives;
import jdplus.math.functions.ssq.SsqNumericalDerivatives;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.univariate.ISsf;
import demetra.data.DoubleSeq;
import jdplus.likelihood.LikelihoodFunctionPoint;
import jdplus.ssf.SsfException;

/**
 *
 * @author Jean Palate
 * @param <S>
 */
public class MarginalLikelihoodFunctionPoint<S, F extends ISsf> implements
        LikelihoodFunctionPoint<MarginalLikelihood> {

    /**
     *
     */
    private final F currentSsf;
    private final S current;

    /**
     *
     */
    private final MarginalLikelihood ll;
    private final DoubleSeq p;
    private final DoubleSeq E;
    private final MarginalLikelihoodFunction<S, F> fn;

    /**
     *
     * @param fn
     * @param p
     */
    public MarginalLikelihoodFunctionPoint(MarginalLikelihoodFunction<S, F> fn, DoubleSeq p) {
        this.fn = fn;
        this.p = DataBlock.of(p);
        current = fn.getMapping().map(p);
        currentSsf = fn.getBuilder().buildSsf(current);
        MarginalLikelihood ml;
        DoubleSeq e;
        try {
            ml = DkToolkit.marginalLikelihood(currentSsf, fn.getData(), fn.isScalingFactor(), fn.isResiduals());
            e = ml.e();
            if (fn.isScalingFactor()) {
                DataBlock r = DataBlock.select(e, x -> Double.isFinite(x));
                if (fn.isMaximumLikelihood()) {
                    double factor = Math.sqrt(ml.factor());
                    r.mul(factor);
                }
                e = r;
            }
        } catch (SsfException err) {
            ml = null;
            e = null;
        }
        ll = ml;
        E = e;
    }

    public F getSsf() {
        return currentSsf;
    }

    public S getCore() {
        return current;
    }

    @Override
    public DoubleSeq getE() {
        return E;
    }

    /**
     *
     * @return
     */
    @Override
    public MarginalLikelihood getLikelihood() {
        return ll;
    }

    @Override
    public DoubleSeq getParameters() {
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

    @Override
    public IFunctionDerivatives derivatives() {
        return new NumericalDerivatives(this, fn.isSymmetric(), fn.isMultiThreaded());
    }

    ;

    @Override
    public ISsqFunctionDerivatives ssqDerivatives() {
        return new SsqNumericalDerivatives(this, fn.isSymmetric(), fn.isMultiThreaded());
    }
;
}
