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

import ec.tstoolkit.data.AbsMeanNormalizer;
import ec.tstoolkit.data.BurgAlgorithm;
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

    /**
     * @return the biascorrection
     */
    public boolean isBiasCorrection() {
        return biascorrection;
    }

    /**
     * @param biascorrection the biascorrection to set
     */
    public void setBiasCorrection(boolean biascorrection) {
        this.biascorrection = biascorrection;
    }

    /**
     * @return the finalcorrection
     */
    public boolean isFinalCorrection() {
        return finalcorrection;
    }

    /**
     * @param finalcorrection the finalcorrection to set
     */
    public void setFinalCorrection(boolean finalcorrection) {
        this.finalcorrection = finalcorrection;
    }

    /**
     * @return the initialization
     */
    public Initialization getInitialization() {
        return initialization;
    }

    /**
     * @param initialization the initialization to set
     */
    public void setInitialization(Initialization initialization) {
        this.initialization = initialization;
    }

    public static enum Initialization {

        Ols,
        Durbin,
        Burg
    }

    private SarimaModel m_model;
    private SarmaSpecification m_spec = new SarmaSpecification();

    private boolean biascorrection = true, finalcorrection = true;
    private Initialization initialization = Initialization.Burg;

    private double[] m_data, m_a, m_pi;
    private IReadDataBlock m_odata;

    private static final int MAXNPI = 50;
    private static final double OVERFLOW = 1e16, EPS = 1e-6;

    /**
     *
     */
    public HannanRissanen2() {
    }

    private double[] ls(Matrix m, double[] y) {
        Householder qr = new Householder(false);
        qr.setEpsilon(EPS);
        qr.decompose(m);
        int nx = m.getColumnsCount();
        if (qr.getRank() == 0) {
            return new double[nx];
        }
        double[] pi = qr.solve(y);
        int[] unused = qr.getUnused();
        if (unused == null) {
            return pi;
        } else {
            double[] pic = new double[nx];
            for (int i = 0, j = 0, k = 0; i < nx; ++i) {
                if (k < unused.length && i == unused[k]) {
                    ++k;
                } else {
                    pic[i] = pi[j];
                }
            }
            return pic;
        }
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
                return;
            }
            res[i] = sum;
            a1[i] = sum1 + sum;
            a2[i] = sum2 + sum;

        }
        double[] dpi = ls(mat, res);
        for (int i = 0; i < dpi.length; ++i) {
            m_pi[i] += dpi[i];
        }
    }

    private boolean calc() {
        m_model = new SarimaModel(m_spec);
        int p = m_spec.getP() + m_spec.getFrequency() * m_spec.getBP();
        int q = m_spec.getQ() + m_spec.getFrequency() * m_spec.getBQ();
        if (p == 0 && q == 0) {
            return true;
        }

        if (q > 0) {
            initialize();
        }
        minspq();
        if (q > 0 && biascorrection) {
            biascorrection();
        }

        updatemodel();
        if (q > 0 && p > 0 && finalcorrection) {
            finalcorrection();
        }
        return true;
    }

    private void finalcorrection() {
        BackFilter ar = m_model.getAR();
        DataBlock ndata = new DataBlock(m_data.length - ar.getDegree());
        ar.filter(new DataBlock(m_data), ndata);
        HannanRissanen2 hr = new HannanRissanen2();
        hr.setBiasCorrection(biascorrection);
        SarmaSpecification nspec = m_spec.clone();
        nspec.setP(0);
        nspec.setBP(0);
        if (!hr.process(ndata, nspec)) {
            return;
        }
        for (int i = hr.m_pi.length - 1, j = m_pi.length - 1; i >= 0; --i, --j) {
            m_pi[j] = hr.m_pi[i];
        }
        updatemodel();
    }

    private void clear() {
        m_model = null;
        m_a = null;
    }

    /**
     *
     * @return
     */
    public IReadDataBlock getData() {
        return m_odata;
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

    private int npi() {
        int q = m_spec.getQ() + m_spec.getFrequency() * m_spec.getBQ();
        int p = m_spec.getP() + m_spec.getFrequency() * m_spec.getBP();
        int n = m_data.length;

        double ln = Math.log(n);
        int npi = Math.max((int) (ln * ln), Math.max(p, 2 * q));
        if (npi >= n) {
            npi = n - n / 4;
        }
        if (npi > MAXNPI) {
            npi = MAXNPI;
        }
        return npi;
    }

    // step 0 of the process...
    private double[] initac() {
        return DescriptiveStatistics.ac(npi(), m_data);
    }

    private void initialize() {
        switch (initialization) {
            case Durbin:
                durbin();
                break;
            case Ols:
                ols();
                break;
            case Burg:
                burg();
                break;
        }
    }

    private void ols() {
        int n = m_data.length;
        int nar = npi();
        Matrix M = new Matrix(n, nar);
        DataBlockIterator cols = M.columns();
        DataBlock col = cols.getData();
        int cur = 0;
        do {
            col.drop(++cur, 0).copyFrom(m_data, 0);
        } while (cols.next());

        double[] pi = ls(M, m_data);
        m_a = new double[n];
        for (int i = 0; i < n; ++i) {
            double e = m_data[i];
            int jmax = pi.length > i ? i : pi.length;
            for (int j = 1; j <= jmax; ++j) {
                e -= pi[j - 1] * m_data[i - j];
            }
            m_a[i] = e;
        }
    }

    private void burg() {
         int n = m_data.length;
        m_a = new double[n];
        BurgAlgorithm bg = new BurgAlgorithm();
        bg.solve(new ReadDataBlock(m_data), npi());
        m_a = bg.residuals();
    }

    // compute estimates of innovations
    private void durbin() {
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
    private void minspq() {
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
        m_pi = ls(mat, data);
        for (int i = 0; i < np; ++i) {
            m_pi[i] = -m_pi[i];
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
        m_spec = spec.clone();
        AbsMeanNormalizer normalizer = new AbsMeanNormalizer();
        if (!normalizer.process(value)) {
            return false;
        }
        m_data = normalizer.getNormalizedData();
        m_odata = value;
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
