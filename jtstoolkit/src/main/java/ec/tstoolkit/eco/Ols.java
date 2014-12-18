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
package ec.tstoolkit.eco;

import ec.tstoolkit.BaseException;
import ec.tstoolkit.maths.matrices.Householder;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.maths.matrices.UpperTriangularMatrix;

/**
 *
 * @author Jean Palate
 */
public class Ols {

    private RegModel m_model;
    private ConcentratedLikelihood m_ll;

    /**
     *
     */
    public Ols() {
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
    public RegModel getModel() {
        return m_model;
    }

    /**
     *
     * @return
     */
    public DataBlock getResiduals() {
        return m_model.calcRes(new DataBlock(m_ll.getB()));
    }

    /**
     *
     * @param model
     * @return
     */
    public boolean process(RegModel model) {
        m_model = model;
        m_ll = new ConcentratedLikelihood();
        Matrix x = model.variables();
        if (x == null) {
            return false;
        }

        int n = x.getRowsCount(), nx = x.getColumnsCount();
        DataBlock y = model.getY();

        if (nx > 0) {
            Householder qr = new Householder(true);
            try {
                qr.decompose(x);
                double[] res = new double[n - qr.getRank()];
                double[] b = new double[qr.getRank()];
                qr.leastSquares(y, new DataBlock(b), new DataBlock(res));
                double ssqerr = 0;
                for (int i = 0; i < res.length; ++i) {
                    ssqerr += res[i] * res[i];
                }
                Matrix u = UpperTriangularMatrix.inverse(qr.getR());

                // initializing the results...
                double sig = ssqerr / n;
                Matrix bvar = SymmetricMatrix.XXt(u);
                bvar.mul(sig);
                m_ll.set(ssqerr, 0, n);
                m_ll.setRes(res);
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
                            bc[i] = b[j];
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
                    b = bc;
                    bvar = bvarc;
                }

                m_ll.setB(b, bvar, qr.getRank());
                return true;
            } catch (BaseException ex) {
                return false;
            }
        } else {
            double ssqerr = y.ssq();
            // initializing the results...
            double[] res = new double[y.getLength()];
            y.copyTo(res, 0);
            m_ll.setRes(res);
            m_ll.set(ssqerr, 0, n);
            return true;
        }
    }
}
