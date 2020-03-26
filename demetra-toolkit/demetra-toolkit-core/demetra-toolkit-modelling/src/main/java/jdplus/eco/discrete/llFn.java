/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.eco.discrete;

import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.math.functions.DefaultDomain;
import jdplus.math.functions.IFunction;
import jdplus.math.functions.IFunctionDerivatives;
import jdplus.math.functions.IFunctionPoint;
import jdplus.math.functions.IParametersDomain;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.SymmetricMatrix;

/**
 *
 * @author Jean Palate
 */
class llFn implements IFunction {

    llFn(DiscreteModel model) {
        this.model = model;
    }

    DiscreteModel model;

    double[] probabilities(DoubleSeq b) {
        // compute Xb
        int n = model.X.getRowsCount();
        double[] p = new double[n];
        DataBlock Xb = DataBlock.of(p);
        DoubleSeqCursor bcur = b.cursor();
        DataBlockIterator cols = model.X.columnsIterator();
        Xb.setAY(bcur.getAndNext(), cols.next());
        while (cols.hasNext()) {
            Xb.addAY(bcur.getAndNext(), cols.next());
        }
        for (int pos = 0; pos < n; ++pos) {
            double d = p[pos];
            double z = model.cdf.f(d);
            if (Double.isNaN(z)) {
                if (d < 0) {
                    z = 0;
                } else {
                    z = 1;
                }
            }
            p[pos] = z;
        }
        return p;
    }

    /**
     * Returns the likelihood function likelihood = sum (p(i),y(i)=1) + sum
     * (1-p(i),y(i)=0) where p(i)=cdf(x(i)*b)
     *
     * @param b The coefficients
     * @return
     */
    double logLikelihood(DoubleSeq b) {
        double[] p = probabilities(b);
        double ll = 0;
        int[] y = model.getY();
        for (int pos = 0; pos < y.length; ++pos) {
            double pcur = p[pos];
            int ycur = y[pos];
            if (ycur != 0) {
                if (pcur == 0) {
                    return Double.NaN;
                } else if (pcur != 1) {
                    ll += Math.log(pcur);
                }
            } else {
                if (pcur == 1) {
                    return Double.NaN;
                } else if (pcur != 0) {
                    ll += Math.log(1 - pcur);
                }
            }
        }
        return ll;
    }

    /**
     * Returns the gradient of likelihood function
     *
     * @param b The coefficients
     * @return
     */
    public double[] loglikelihoodGradient(DoubleSeq b) {
        double[] g = new double[b.length()];
        DataBlock G = DataBlock.of(g);
        DataBlockIterator rows = model.X.rowsIterator();
        int pos = 0;
        ICumulativeDistributionFunction cdf = model.getCdf();
        while (rows.hasNext()) {
            DataBlock row = rows.next();
            double d = row.dot(b);
            double p = cdf.f(d);
            double dp = cdf.df(d);
            int ycur = model.y[pos++];
            if (ycur != 0) {
                if (p != 0) {
                    G.addAY(dp / p, row);
                }
            } else {
                if (p != 1) {
                    G.addAY(-dp / (1 - p), row);
                }
            }
        }
        return g;
    }

    public void logLikelihoodHessian(DoubleSeq b, Matrix h) {
        DataBlockIterator rows = model.X.rowsIterator();
        int pos = 0;
        ICumulativeDistributionFunction cdf = model.getCdf();
        while (rows.hasNext()) {
            DataBlock X = rows.next();
            Matrix XX = SymmetricMatrix.xxt(X);
            double x = X.dot(b);
            double f = cdf.f(x);
            double cf = 1 - f;
            double df = cdf.df(x);
            double d2f = cdf.d2f(x);
            int y = model.y[pos++];
            if (y != 0) {
                h.addAY((f * d2f - df * df) / (f * f), XX);
            } else {
                h.addAY(-(cf * d2f + df * df) / (cf * cf), XX);
            }
        }
    }

    @Override
    public IParametersDomain getDomain() {
        return new DefaultDomain(model.getX().getColumnsCount(), 1e-8);
    }

    @Override
    public IFunctionPoint evaluate(DoubleSeq parameters) {
        return new Point(parameters);
    }

    class Point implements IFunctionPoint {

        private final DoubleSeq parameters;

        Point(DoubleSeq p) {
            parameters = p;
        }

        @Override
        public llFn getFunction() {
            return llFn.this;
        }

        @Override
        public IFunctionDerivatives derivatives() {
            return new dllFn(parameters);
        }

        @Override
        public DoubleSeq getParameters() {
            return parameters;
        }

        @Override
        public double getValue() {
            return -logLikelihood(parameters);
        }

    }

    class dllFn implements IFunctionDerivatives {

        private final DoubleSeq parameters;

        dllFn(DoubleSeq p) {
            parameters = p;
        }

        @Override
        public IFunction getFunction() {
            return llFn.this;
        }

        @Override
        public DoubleSeq gradient() {
            double[] g = loglikelihoodGradient(parameters);
            return DoubleSeq.onMapping(g.length, i -> -g[i]);
        }

        @Override
        public void hessian(Matrix hessian) {
            logLikelihoodHessian(parameters, hessian);
            hessian.chs();
        }

    }
}
