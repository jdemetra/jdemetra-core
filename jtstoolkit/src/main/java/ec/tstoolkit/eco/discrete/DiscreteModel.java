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

package ec.tstoolkit.eco.discrete;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.eco.Ols;
import ec.tstoolkit.eco.RegModel;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.maths.realfunctions.IFunctionMinimizer;
import ec.tstoolkit.maths.realfunctions.riso.LbfgsMinimizer;

/**
 *
 * @author Jean Palate
 */
public class DiscreteModel {

    private int[] m_y;
    private Matrix m_x;
    private final ICumulativeDistributionFunction m_fn;
    private IFunctionMinimizer minimizer = new LbfgsMinimizer();

    public DiscreteModel(ICumulativeDistributionFunction fn) {
        m_fn = fn;
    }

    public DiscreteModelEvaluation process(int[] y, Matrix X) {
        m_y = y;
        m_x = X;

        // initialization 
        double[] c = initialize();
        // create the function 
        llFn fn = new llFn(this);
        // optimization...
        if (getMinimizer().minimize(fn, new DiscreteModelEvaluation(this, new DataBlock(c)))) {
            return (DiscreteModelEvaluation) getMinimizer().getResult();
        } else {
            return null;
        }
    }

    public double[] probabilities(DataBlock b) {
        double[] z = new double[m_x.getRowsCount()];
        DataBlockIterator rows = m_x.rows();
        DataBlock row = rows.getData();
        do {
            double d = row.dot(b);
            z[rows.getPosition()] = m_fn.cdf(d);
        } while (rows.next());
        return z;
    }

    /**
     * Returns the likelihood function likelihood = sum (p(i),y(i)=1) + sum
     * (1-p(i),y(i)=0) where p(i)=cdf(x(i)*b)
     *
     * @param b The coefficients
     * @return
     */
    public double loglikelihood(DataBlock b) {
        DataBlockIterator rows = m_x.rows();
        double ll = 0;
        DataBlock row = rows.getData();
        do {
            double d = row.dot(b);
            double p = m_fn.cdf(d);
            if (Double.isNaN(p)) {
                if (d < 0) {
                    p = 0;
                } else {
                    p = 1;
                }
            }
            int y = m_y[rows.getPosition()];
            if (y != 0) {
                if (p == 0) {
                    ll += -999;
                } else if (p != 1) {
                    ll += Math.log(p);
                }
            } else {
                if (p == 1) {
                    ll += -999;
                } else if (p != 0) {
                    ll += Math.log(1 - p);
                }
            }
        } while (rows.next());
        return ll;
    }

    public ICumulativeDistributionFunction getCumulativeDistributionFunction() {
        return m_fn;
    }

    /**
     * Returns the derivative of likelihood function dlikelihood = sum
     * (dp(i),y(i)=1) + sum (-dp(i),y(i)=0) where dp(i)=dcdf(x*b)* x(i)
     *
     * @param b The coefficients
     * @return
     */
    public double dloglikelihood(DataBlock b, int i) {
        DataBlockIterator rows = m_x.rows();
        DataBlock row = rows.getData();
        double dll = 0;
        do {
            double d = row.dot(b);
            double p = m_fn.cdf(d);
            double dp = m_fn.dcdf(d);
            int y = m_y[rows.getPosition()];
            if (y != 0) {
                dll += dp * row.get(i) / p;
            } else {
                dll -= dp * row.get(i) / (1 - p);
            }
        } while (rows.next());
        return dll;
    }

    /**
     * Returns the gradient of likelihood function
     *
     * @param b The coefficients
     * @return
     */
    public double[] loglikelihoodGradient(DataBlock b) {
        double[] g = new double[b.getLength()];
        DataBlock G = new DataBlock(g);
        DataBlockIterator rows = m_x.rows();
        DataBlock row = rows.getData();
        do {
            double d = row.dot(b);
            double p = m_fn.cdf(d);
            double dp = m_fn.dcdf(d);
            int y = m_y[rows.getPosition()];
            if (y != 0) {
                if (p != 0) {
                    G.addAY(dp / p, row);
                }
            } else {
                if (p != 1) {
                    G.addAY(-dp / (1 - p), row);
                }
            }
        } while (rows.next());
        return g;
    }

    public Matrix logLikelihoodHessian(DataBlock b) {
        Matrix h = new Matrix(b.getLength(), b.getLength());
        DataBlockIterator rows = m_x.rows();
        DataBlock X = rows.getData();
        do {
            Matrix XX = SymmetricMatrix.CCt(X);
            double x = X.dot(b);
            double f = m_fn.cdf(x);
            double cf = 1 - f;
            double df = m_fn.dcdf(x);
            double d2f = m_fn.d2cdf(x);
            int y = m_y[rows.getPosition()];
            if (y != 0) {
                h.addAY((f * d2f - df * df) / (f * f), XX);
            } else {
                h.addAY(-(cf * d2f + df * df) / (cf * cf), XX);
            }
        } while (rows.next());
        return h;

    }

    public int[] getY() {
        return m_y;
    }

    public Matrix getX() {
        return m_x;
    }

    protected double[] initialize() {
        double[] y = new double[m_y.length];
        for (int i = 0; i < y.length; ++i) {
            y[i] = m_y[i];
        }
        Ols ols = new Ols();
        RegModel regmodel = new RegModel();
        regmodel.setY(new DataBlock(y));
        DataBlockIterator cols = m_x.columns();
        DataBlock col = cols.getData();
        do {
            regmodel.addX(col.deepClone());
        } while (cols.next());
        ols.process(regmodel);
        return ols.getLikelihood().getB();
    }

    /**
     * @return the minimizer
     */
    public IFunctionMinimizer getMinimizer() {
        return minimizer;
    }

    /**
     * @param minimizer the minimizer to set
     */
    public void setMinimizer(IFunctionMinimizer minimizer) {
        this.minimizer = minimizer;
    }
}
