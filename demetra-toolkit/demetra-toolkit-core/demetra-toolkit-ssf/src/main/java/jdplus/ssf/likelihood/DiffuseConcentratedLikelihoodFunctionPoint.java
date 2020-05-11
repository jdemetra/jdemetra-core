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

import jdplus.ssf.dk.DkToolkit;
import jdplus.likelihood.DiffuseConcentratedLikelihood;
import jdplus.data.DataBlock;
import jdplus.math.functions.IFunction;
import jdplus.math.functions.ssq.ISsqFunction;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.univariate.IConcentratedLikelihoodComputer;
import jdplus.ssf.univariate.SsfRegressionModel;
import jdplus.math.functions.IFunctionDerivatives;
import jdplus.math.functions.NumericalDerivatives;
import jdplus.math.functions.ssq.ISsqFunctionDerivatives;
import jdplus.math.functions.ssq.SsqNumericalDerivatives;
import demetra.data.DoubleSeq;
import jdplus.likelihood.LikelihoodFunctionPoint;
import jdplus.ssf.SsfException;

/**
 *
 * @author Jean Palate
 * @param <S>
 * @param <F>
 */
public class DiffuseConcentratedLikelihoodFunctionPoint<S, F extends ISsf> implements
        LikelihoodFunctionPoint<DiffuseConcentratedLikelihood> {

    /**
     *
     */
    private final F currentSsf;
    private final S current;

    /**
     *
     */
    private final DiffuseConcentratedLikelihood ll;
    private final DoubleSeq p;
    private final DoubleSeq E;
    private final DiffuseConcentratedLikelihoodFunction<S, F> fn;

    /**
     *
     * @param fn
     * @param p
     */
    public DiffuseConcentratedLikelihoodFunctionPoint(DiffuseConcentratedLikelihoodFunction<S, F> fn, DoubleSeq p) {
        this.fn = fn;
        this.p = DataBlock.of(p);
        current = fn.getMapping().map(p);
        currentSsf = fn.getBuilder().buildSsf(current);
        boolean fastcomputer = fn.isFast() && !fn.isMissing() && currentSsf.isTimeInvariant();
        IConcentratedLikelihoodComputer<DiffuseConcentratedLikelihood> computer = DkToolkit.concentratedLikelihoodComputer(true, fastcomputer, fn.isScalingFactor());
        DiffuseConcentratedLikelihood dcl;
        DoubleSeq e;
        try {
            if (fn.getX() == null) {
                dcl = computer.compute(currentSsf, fn.getData());
            } else {
                dcl = computer.compute(new SsfRegressionModel(currentSsf, fn.getData(), fn.getX(), fn.getDiffuseX()));
            }
            if (fn.isScalingFactor()) {
                DoubleSeq res = dcl.e();
                DataBlock r = DataBlock.select(res, x -> Double.isFinite(x));
                if (fn.isMaximumLikelihood()) {
                    double factor = Math.sqrt(dcl.factor());
                    r.mul(factor);
                }
                e = r;
            } else {
                e = null;
            }
        } catch (SsfException err) {
            dcl = null;
            e = null;
        }
        ll = dcl;
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
    public DiffuseConcentratedLikelihood getLikelihood() {
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
