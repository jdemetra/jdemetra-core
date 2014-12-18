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
package ec.tstoolkit.arima.estimation;

import ec.tstoolkit.BaseException;
import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.eco.RegModel;
import ec.tstoolkit.maths.matrices.Householder;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.maths.matrices.UpperTriangularMatrix;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class ConcentratedLikelihoodEstimation {

    /**
     *
     */
    public static final AtomicLong fnCalls = new AtomicLong(0);

    private final IArmaFilter m_filter;
    private boolean m_scaling = true;
    private ConcentratedLikelihood m_ll;

    private double[] m_el;

    /**
     *
     */
    public ConcentratedLikelihoodEstimation() {
        m_filter = new KalmanFilter(true);
    }

    /**
     *
     * @param filter
     */
    public ConcentratedLikelihoodEstimation(IArmaFilter filter) {
        m_filter = filter.exemplar();
    }

    public void setScaling(boolean scaling) {
        m_scaling = scaling;
    }

    public boolean isScaling() {
        return m_scaling;
    }

    /**
     *
     * @param <S>
     * @param model
     * @return
     */
    public <S extends IArimaModel> boolean estimate(final RegArimaModel<S> model) {
        RegModel dmodel = model.getDModel();
        return estimate(dmodel, model.getArima().getNonStationaryARCount(), model.getMissings(), model.getArma());
    }

    /**
     *
     * @param dmodel
     * @param d
     * @param missings
     * @param arma
     * @return
     */
    public boolean estimate(RegModel dmodel, int d, int[] missings, IArimaModel arma) {
        DataBlock dy = dmodel.getY();
        int n = dy.getLength();
        int nl = m_filter.initialize(arma, n);
        try {
            return process(dmodel, nl, d, missings);
        } catch (BaseException ex) {
            return false;
        }
    }

    /**
     *
     * @return
     */
    public ConcentratedLikelihood getLikelihood() {
        return m_ll;
    }

    /**
     *
     * @return
     */
    public double[] getResiduals() {
        return m_el;
    }

    private boolean process(RegModel model, int nl, int nd, int[] missings) {
        fnCalls.incrementAndGet();
        m_ll = new ConcentratedLikelihood();
        DataBlock y = model.getY().deepClone();
        int n = y.getLength();
        double[] factors = null;
        double yfactor = 1;
        if (m_scaling) {
            double yn = y.nrm2();
            if (yn != 0) {
                yfactor = n / yn;
                y.mul(yfactor);
            }
        }
        DataBlock yl = new DataBlock(nl);
        m_filter.filter(y, yl);
        Matrix x = model.variables();
        int nx = x == null ? 0 : x.getColumnsCount();
        Matrix xl;
        if (nx > 0) {
            if (m_scaling) {
                factors = new double[nx];
                for (int i = 0; i < nx; ++i) {
                    DataBlock cur = x.column(i);
                    double xn = cur.nrm2();
                    if (xn != 0) {
                        double w = n / xn;
                        factors[i] = w;
                        cur.mul(w);
                    } else {
                        factors[i] = 1;
                    }
                }
            }
            xl = new Matrix(nl, x.getColumnsCount());
            DataBlockIterator xcols = x.columns();
            DataBlockIterator xlcols = xl.columns();
            DataBlock xcol = xcols.getData(), xlcol = xlcols.getData();

            do {
                m_filter.filter(xcol, xlcol);
            } while (xcols.next() && xlcols.next());

            Householder qr = new Householder(true);
            qr.setEpsilon(1e-12);
            qr.decompose(xl);
            if (qr.getRank() == 0) {
                double ssqerr = yl.ssq();
                double ldet = m_filter.getLogDeterminant();
                m_ll.set(ssqerr, ldet, n);
                m_ll.setRes(yl.getData());
                m_ll.setB(new double[nx], new Matrix(nx, nx), 0);
                if (m_scaling) {
                    m_ll.rescale(yfactor);
                }
                m_el = m_ll.getResiduals();
                return true;
            } else {
                DataBlock b = new DataBlock(qr.getRank());
                DataBlock res = new DataBlock(nl - qr.getRank());
                qr.leastSquares(yl, b, res);
                Matrix R = qr.getR();
                double ssqerr = res.ssq();
                int nm = missings == null ? 0 : missings.length;
                double ldet = m_filter.getLogDeterminant();
                if (nm > 0) {
//                    // compute correction of the likelihood...
//                    // exclude from the correction missing values in the initial data (nuisance terms)
//                    int mused = 0;
//                    while (mused < nm && missings[mused] >= nd) {
//                        ++mused;
//                    }
//                    if (mused > 0) {
//                        DataBlock rdiag = qr.getRDiagonal().extract(model.isMeanCorrection() ? 1 : 0, mused);
//                        ldet += 2 * rdiag.sumLog().value;
//                        n -= mused;
//                    }

                    // !!! I don't follow exactly the Tramo implementation
                    // The likelihood is corrected for each missing value, including at the beginning of the series.
                    // Otherwise - for instance -, extending a series with missing values would yield different results
                    // See also Francke, Koopman and de Vos, "Likelihood functions for State Space
                    // models with Diffuse Initial conditions" (2010) or McElroy, Model estimation... (2012).
                    // The transformation of the data can't depend on a parameter to be maximized.
                    int mstart=model.isMeanCorrection() ? 1 : 0;
                    DataBlock rdiag = qr.getRDiagonal().extract(mstart, nm);
                    double corr=rdiag.sumLog().value;
                    if (m_scaling)
                        for (int i=0; i<nm; ++i)
                              corr-=Math.log(factors[mstart+i]);
                    ldet += 2 *corr;
                    n -= nm;
                }
                Matrix bvar = SymmetricMatrix.XXt(UpperTriangularMatrix
                        .inverse(R));
                bvar.mul(ssqerr / n);
                // if some variable are unused, we expand here the array of the
                // coefficients and the matrix of covariance.
                // data related to unused variables are set to 0
                int[] unused = qr.getUnused();
                if (unused != null) {
                    double[] bc = new double[nx];
                    Matrix bvarc = new Matrix(nx, nx);
                    for (int i = 0, j = 0, k = 0; i < nx; ++i) {
                        if (k < unused.length && i == unused[k]) {
                            ++k;
                        } else {
                            bc[i] = b.get(j);
                            for (int ci = 0, cj = 0, ck = 0; ci <= i; ++ci) {
                                if (ck < unused.length && ci == unused[ck]) {
                                    ++ck;
                                } else {
                                    double d = bvar.get(j, cj);
                                    bvarc.set(i, ci, d);
                                    bvarc.set(ci, i, d);
                                    ++cj;
                                }
                            }
                            ++j;
                        }
                    }
                    b = new DataBlock(bc);
                    bvar = bvarc;
                }

                m_ll.set(ssqerr, ldet, n);
                m_ll.setRes(res.getData());
                m_ll.setB(b.getData(), bvar, qr.getRank() - nm);
                DataBlock el = yl.deepClone();
                for (int i = 0; i < nx; ++i) {
                    el.addAY(-b.get(i), xl.column(i));
                }
                if (m_scaling) {
                    m_ll.rescale(yfactor, factors);
                    el.div(yfactor);
                }
                m_el = el.getData();
//                DataBlock e=y.deepClone();
//                for (int i = 0; i < nx; ++i)
//		    e.addAY(-b.get(i), x.column(i));
//                DataBlock el=new DataBlock(nl);
//                m_filter.filter(e, el);
//                m_el=el.getData();
                return true;
            }
        } else {
            double ssqerr = yl.ssq();
            double ldet = m_filter.getLogDeterminant();
            m_ll.set(ssqerr, ldet, n);
            m_ll.setRes(yl.getData());
            if (m_scaling) {
                m_ll.rescale(yfactor);
            }
            m_el = m_ll.getResiduals();
            return true;
        }
    }

}
