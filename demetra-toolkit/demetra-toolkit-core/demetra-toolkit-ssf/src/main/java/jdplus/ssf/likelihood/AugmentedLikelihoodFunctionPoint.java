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
import jdplus.ssf.univariate.ISsf;
import jdplus.math.functions.IFunctionDerivatives;
import jdplus.math.functions.NumericalDerivatives;
import jdplus.math.functions.ssq.ISsqFunctionDerivatives;
import jdplus.math.functions.ssq.SsqNumericalDerivatives;
import demetra.data.DoubleSeq;
import jdplus.likelihood.LikelihoodFunctionPoint;
import jdplus.ssf.SsfException;
import jdplus.ssf.akf.AkfToolkit;

/**
 *
 * @author Jean Palate
 * @param <S>
 * @param <F>
 */
public class AugmentedLikelihoodFunctionPoint<S, F extends ISsf> implements
        LikelihoodFunctionPoint<DiffuseLikelihood> {

    /**
     *
     */
    private final F currentSsf;
    private final S current;

    /**
     *
     */
    private final DiffuseLikelihood ll;
    private final DoubleSeq p;
    private final DoubleSeq E;
    private final AugmentedLikelihoodFunction<S, F> fn;

    /**
     *
     * @param fn
     * @param p
     */
    public AugmentedLikelihoodFunctionPoint(AugmentedLikelihoodFunction<S, F> fn, DoubleSeq p) {
        this.fn = fn;
        this.p = p;
        current = fn.getMapping().map(p);
        currentSsf = fn.getBuilder().buildSsf(current);
        boolean fastcomputer = fn.isFast() && !fn.isMissing() && currentSsf.isTimeInvariant();
        DiffuseLikelihood dl = null;
        DoubleSeq e = null;
        try {
            if (fastcomputer) {
                dl = AkfToolkit.fastLikelihoodComputer(fn.isScalingFactor(), fn.isResiduals()).compute(currentSsf, fn.getData());
            } else {
                dl = AkfToolkit.likelihoodComputer(fn.isCollapsing(), fn.isScalingFactor(), fn.isResiduals()).compute(currentSsf, fn.getData());
            }
            if (fn.isScalingFactor()) {
                DoubleSeq res = dl.e();
                DataBlock r = DataBlock.select(res, x -> Double.isFinite(x));
                if (fn.isMaximumLikelihood()) {
                    double factor = Math.sqrt(dl.factor());
                    r.mul(factor);
                }
                e = r;
            }
        } catch (SsfException err) {
            dl = null;
        }
        ll = dl;
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
    public DiffuseLikelihood getLikelihood() {
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

    @Override
    public ISsqFunctionDerivatives ssqDerivatives() {
        return new SsqNumericalDerivatives(this, fn.isSymmetric(), fn.isMultiThreaded());
    }
}
