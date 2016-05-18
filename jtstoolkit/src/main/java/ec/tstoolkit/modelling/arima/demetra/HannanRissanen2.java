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
package ec.tstoolkit.modelling.arima.demetra;

import ec.tstoolkit.sarima.estimation.*;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.matrices.Householder;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.MatrixException;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarmaSpecification;

/**
 * The Hannan-Rissanen procedure is performed as in TRAMO. See
 * Gomez-Maravall-Caporello
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class HannanRissanen2 {

    private SarimaModel m_model;
    private SarmaSpecification m_spec = new SarmaSpecification();

    private boolean m_ok;

    private double[] m_data, m_a, m_pi;

    private static final int MAXNPI = 50;
    private static final double OVERFLOW = 1e16, EPS = 1e-9;

    /**
     *
     */
    public HannanRissanen2() {
    }

    private void biascorrection() {
        // int p = m_spec.P + m_spec.Frequency * m_spec.BP;
        // int q = m_spec.Q + m_spec.Frequency * m_spec.BQ;
        int n = m_data.length;

        int np = m_spec.getP() + m_spec.getBP() * (1 + m_spec.getP());
        int nq = m_spec.getQ() + m_spec.getBQ() * (1 + m_spec.getQ());

        // new residuals
        double[] a1 = new double[n];
        double[] a2 = new double[n];
        double[] res = new double[n];
        Matrix mat = new Matrix(n, np + nq);
        double[] mdata = mat.internalStorage();
        for (int i = 0; i < n; ++i) {
            int picur = 0;
            double sum = m_data[i], sum1 = 0, sum2 = 0;
            for (int j = 1; j <= m_spec.getP(); ++j, ++picur) {
                if (i - j >= 0) {
                    sum += m_pi[picur] * m_data[i - j];
                    sum1 -= m_pi[picur] * a1[i - j];
                    mdata[i + n * picur] = -a1[i - j];
                }
            }

            for (int j = 1; j <= m_spec.getBP(); ++j) {
                int l = j * m_spec.getFrequency();
                if (i - l >= 0) {
                    sum += m_pi[picur] * m_data[i - l];
                    sum1 -= m_pi[picur] * a1[i - l];
                    mdata[i + n * picur] = -a1[i - l];
                }
                ++picur;
                ++l;
                for (int k = 0; k < m_spec.getP(); ++k, ++picur, ++l) {
                    if (i - l >= 0) {
                        sum += m_pi[picur] * m_data[i - l];
                        sum1 -= m_pi[picur] * a1[i - l];
                        mdata[i + n * picur] = -a1[i - l];
                    }
                }
            }
            for (int j = 1; j <= m_spec.getQ(); ++j, ++picur) {
                if (i - j >= 0) {
                    sum -= m_pi[picur] * res[i - j];
                    sum2 -= m_pi[picur] * a2[i - j];
                    mdata[i + n * picur] = a2[i - j];
                }
            }

            for (int j = 1; j <= m_spec.getBQ(); ++j) {
                int l = j * m_spec.getFrequency();
                if (i - l >= 0) {
                    sum -= m_pi[picur] * res[i - l];
                    sum2 -= m_pi[picur] * a2[i - l];
                    mdata[i + n * picur] = a2[i - l];
                }
                ++picur;
                ++l;
                for (int k = 0; k < m_spec.getQ(); ++k, ++picur, ++l) {
                    if (i - l >= 0) {
                        sum -= m_pi[picur] * res[i - l];
                        sum2 -= m_pi[picur] * a2[i - l];
                        mdata[i + n * picur] = a2[i - l];
                    }
                }
            }
            if (Math.abs(sum) > OVERFLOW) {
                m_ok = false;
                return;
            }
            res[i] = sum;
            a1[i] = sum1 + sum;
            a2[i] = sum2 + sum;

        }

        Householder qr = new Householder(false);
        qr.decompose(mat);
        qr.setEpsilon(EPS);
        if (qr.getRank() == 0) {
            return;
        }
        double[] dpi = qr.solve(res);
        int[] unused = qr.getUnused();
        if (unused == null) {
            for (int i = 0; i < dpi.length; ++i) {
                m_pi[i] += dpi[i];

            }
        } else {
            for (int i = 0, j = 0, k = 0; i < np; ++i) {
                if (k < unused.length && i == unused[k]) {
                    ++k;
                } else {
                    m_pi[i] += dpi[j];
                }
            }
        }
    }

    private boolean calc() {
        m_ok = true;
        m_model = new SarimaModel(m_spec);
        int p = m_spec.getP() + m_spec.getFrequency() * m_spec.getBP();
        int q = m_spec.getQ() + m_spec.getFrequency() * m_spec.getBQ();
        if (p == 0 && q == 0) {
            return true;
        }

        if (q > 0) {
            initialize();
        }
        minspq(false);
	if (q > 0)
	    biascorrection();

        updatemodel();
        return true;
    }

    private void clear() {
        m_model = null;
        m_a = null;
        m_ok = false;
    }

    /**
     *
     * @return
     */
    public IReadDataBlock getData() {
        return new ReadDataBlock(m_data);
    }

    /**
     *
     * @return
     */
    public SarimaModel getModel() {
        return m_model;
    }

    /**
     *
     * @return
     */
    public SarmaSpecification getSpec() {
        return m_spec;
    }

    // step 0 of the process...
    private double[] initac() {
        int q = m_spec.getQ() + m_spec.getFrequency() * m_spec.getBQ();
        int p = m_spec.getP() + m_spec.getFrequency() * m_spec.getBP();
        int n = m_data.length;

        double ln = Math.log(n);
        int npi = Math.max((int) (ln * ln), Math.max(p, 4 * q));
        if (npi >= n) {
            npi = n - n / 4;
        }
        if (npi > MAXNPI) {
            npi = MAXNPI;
        }

        return DescriptiveStatistics.ac(npi, m_data);
    }

    // compute estimates of innovations
    private void initialize() {
        int n = m_data.length;
        m_a = new double[n];
        double[] ac = initac();
        double[] pc = new double[ac.length];
        DescriptiveStatistics.pac(ac, pc);

        for (int i = 0; i < n; ++i) {
            double e = m_data[i];
            int jmax = ac.length > i ? i : ac.length;
            for (int j = 1; j <= jmax; ++j) {
                e -= pc[j - 1] * m_data[i - j];
            }
            m_a[i] = e;
        }
    }

    // regression (with lags of y and e)
    private void minspq(boolean nres) {
        int p = m_spec.getP() + m_spec.getFrequency() * m_spec.getBP();
        int q = m_spec.getQ() + m_spec.getFrequency() * m_spec.getBQ();
        int n = m_data.length;

        int m = p > q ? p : q;
        int nc = n - m;
        int ccur = 0;

        int np = m_spec.getP() + m_spec.getBP() * (1 + m_spec.getP());
        int nq = m_spec.getQ() + m_spec.getBQ() * (1 + m_spec.getQ());

        Matrix mat = new Matrix(nc, np + nq);
        double[] dmat = mat.internalStorage();
        double[] data = new double[nc];
        System.arraycopy(m_data, m, data, 0, nc);
        // P
        for (int i = 1; i <= m_spec.getP(); ++i, ccur += nc) {
            System.arraycopy(m_data, m - i, dmat, ccur, nc);
        }
        // BP
        for (int i = m_spec.getFrequency(); i <= m_spec.getFrequency()
                * m_spec.getBP(); i += m_spec.getFrequency()) {
            for (int k = 0; k <= m_spec.getP(); ++k, ccur += nc) {
                System.arraycopy(m_data, m - i - k, dmat, ccur, nc);
            }
        }
        // Q
        for (int i = 1; i <= m_spec.getQ(); ++i, ccur += nc) {
            System.arraycopy(m_a, m - i, dmat, ccur, nc);
        }
        // BQ
        for (int i = m_spec.getFrequency(); i <= m_spec.getFrequency()
                * m_spec.getBQ(); i += m_spec.getFrequency()) {
            for (int k = 0; k <= m_spec.getQ(); ++k, ccur += nc) {
                System.arraycopy(m_a, m - i - k, dmat, ccur, nc);
            }
        }
        Householder qr = new Householder(false);
        qr.setEpsilon(EPS);
        qr.decompose(mat);
        if (qr.getRank() == 0) {
            return;
        }
        double[] pi = qr.solve(data);
        int[] unused = qr.getUnused();
        if (unused == null) {
            m_pi = pi;
        } else {
            m_pi = new double[np + nq];
            for (int i = 0, j = 0, k = 0; i < np; ++i) {
                if (k < unused.length && i == unused[k]) {
                    ++k;
                } else {
                    m_pi[i] = pi[j];
                }
            }
        }
        for (int i = 0; i < np; ++i) {
            m_pi[i] = -pi[i];
        }
    }

    /**
     *
     * @param value
     * @param spec
     * @return
     */
    public boolean process(final IReadDataBlock value, SarmaSpecification spec) {
        clear();
        m_data = new double[value.getLength()];
        m_spec = spec.clone();
        value.copyTo(m_data, 0);
        return calc();
    }

    private void updatemodel() {
        if (m_pi == null) {
            return;
        }
        int ccur = 0;
        if (m_spec.getP() != 0) {
            for (int i = 1; i <= m_spec.getP(); ++i) {
                m_model.setPhi(i, m_pi[ccur++]);
            }
        }

        if (m_spec.getBP() != 0) {
            for (int i = 1; i <= m_spec.getBP(); ++i) {
                m_model.setBPhi(i, m_pi[ccur]);
                ccur += 1 + m_spec.getP();
            }
        }
        if (m_spec.getQ() != 0) {
            for (int i = 1; i <= m_spec.getQ(); ++i) {
                m_model.setTheta(i, m_pi[ccur++]);
            }
        }
        if (m_spec.getBQ() != 0) {
            for (int i = 1; i <= m_spec.getBQ(); ++i) {
                m_model.setBTheta(i, m_pi[ccur]);
                ccur += 1 + m_spec.getQ();
            }
        }
    }
}
