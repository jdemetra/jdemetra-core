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
package demetra.sarima;

import demetra.ar.IAutoRegressiveEstimation;
import demetra.data.normalizer.AbsMeanNormalizer;
import demetra.data.DataBlock;
import demetra.design.Development;
import demetra.design.IBuilder;
import demetra.leastsquares.IQRSolver;
import demetra.maths.linearfilters.BackFilter;
import demetra.maths.matrices.Matrix;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarmaSpecification;
import demetra.data.DoubleSequence;
import demetra.stats.AutoCovariances;

/**
 * The Hannan-Rissanen procedure is performed as in TRAMO. See
 * Gomez-Maravall-Caporello
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class HannanRissanen {
    
    public static class Builder implements IBuilder<HannanRissanen>{
        
        private boolean finalcorrection=true, biascorrection=true;
        private Initialization initialization=Initialization.Levinson;
        
        public Builder finalCorrection(boolean correction){
            this.finalcorrection=correction;
            return this;
        }

        public Builder biasCorrection(boolean correction){
            this.biascorrection=correction;
            return this;
        }

        public Builder initialization(Initialization initialization){
            this.initialization=initialization;
            return this;
        }

        @Override
        public HannanRissanen build() {
            return new HannanRissanen(this);
        }
                
    }
    
    public static Builder builder(){
        return new Builder();
    }

    /**
     * @return the biascorrection
     */
    public boolean isBiasCorrection() {
        return biascorrection;
    }

 
    /**
     * @return the finalcorrection
     */
    public boolean isFinalCorrection() {
        return finalcorrection;
    }

    /**
     * @return the initialization
     */
    public Initialization getInitialization() {
        return initialization;
    }

    /**
     * @return the ssq
     */
    public double getBic() {
        return bic;
    }

    public static enum Initialization {

        Ols,
        Levinson,
        Burg
    }

    private SarimaModel m_model;
    private SarmaSpecification m_spec = new SarmaSpecification();

    private final boolean biascorrection, finalcorrection;
    private final Initialization initialization;

    private double[] m_data, m_a, m_pi;
    private DoubleSequence m_odata;
    private double bic;

    private static final int MAXNPI = 50;
    private static final double OVERFLOW = 1e16, EPS = 1e-6;

    /**
     *
     */
    public HannanRissanen() {
        this(new Builder());
    }

    private HannanRissanen(Builder builder){
        initialization=builder.initialization;
        this.finalcorrection=builder.finalcorrection;
        this.biascorrection=builder.biascorrection;
    }
    
    private double[] ls(Matrix mat, double[] y, boolean bbic) {
        IQRSolver solver = IQRSolver.fastSolver();
        solver.solve(DataBlock.ofInternal(y), mat);
        DoubleSequence pi = solver.coefficients();
        int n = y.length, m = pi.count(x -> x != 0);
        if (bbic) {
            bic = Math.log(solver.ssqerr() / n) + Math.log(n) * m / n;
        }
        return pi.toArray();
    }

    private void biascorrection() {
        // int p = m_spec.P + m_spec.Frequency * m_spec.BP;
        // int q = m_spec.Q + m_spec.Frequency * m_spec.BQ;
        int n = m_data.length;

        int np = m_spec.getP() + m_spec.getBp() * (1 + m_spec.getP());
        int nq = m_spec.getQ() + m_spec.getBq() * (1 + m_spec.getQ());

        // new residuals
        double[] a1 = new double[n];
        double[] a2 = new double[n];
        double[] res = new double[n];
        Matrix mat = Matrix.make(n, np + nq);
        double[] mdata = mat.getStorage();
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

            for (int j = 1; j <= m_spec.getBp(); ++j) {
                int l = j * m_spec.getPeriod();
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

            for (int j = 1; j <= m_spec.getBq(); ++j) {
                int l = j * m_spec.getPeriod();
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
        double[] dpi = ls(mat, res, false);
        for (int i = 0; i < dpi.length; ++i) {
            m_pi[i] += dpi[i];
        }
    }

    private boolean calc() {
        m_model = SarimaModel.builder(m_spec).build();
        int p = m_spec.getP() + m_spec.getPeriod() * m_spec.getBp();
        int q = m_spec.getQ() + m_spec.getPeriod() * m_spec.getBq();
        if (p == 0 && q == 0) {
            bic = Math.log(DataBlock.ofInternal(m_data).ssq() / m_data.length);
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
        DataBlock ndata = DataBlock.make(m_data.length - ar.length()+1);
        ar.apply(DataBlock.ofInternal(m_data), ndata);
        HannanRissanen hr=HannanRissanen.builder()
                .biasCorrection(biascorrection)
                .finalCorrection(false)
                .build();
        SarmaSpecification nspec = m_spec.clone();
        nspec.setP(0);
        nspec.setBp(0);
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
    public DoubleSequence getData() {
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
        int q = m_spec.getQ() + m_spec.getPeriod() * m_spec.getBq();
        int p = m_spec.getP() + m_spec.getPeriod() * m_spec.getBp();
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

    private void initialize() {
        IAutoRegressiveEstimation ar;
        switch (initialization) {
            case Ols:
                ar=IAutoRegressiveEstimation.ols();
                break;
            case Burg:
                ar=IAutoRegressiveEstimation.burg();
                break;
            default:
                ar=IAutoRegressiveEstimation.levinson();
                break;
        }
        ar.estimate(DoubleSequence.of(m_data), npi());
        m_a=ar.residuals().toArray();
    }

    // regression (with lags of y and e)
    private void minspq() {
        int p = m_spec.getP() + m_spec.getPeriod() * m_spec.getBp();
        int q = m_spec.getQ() + m_spec.getPeriod() * m_spec.getBq();
        int n = m_data.length;

        int m = p > q ? p : q;
        int nc = n - m;
        int ccur = 0;

        int np = m_spec.getP() + m_spec.getBp() * (1 + m_spec.getP());
        int nq = m_spec.getQ() + m_spec.getBq() * (1 + m_spec.getQ());

        Matrix mat =  Matrix.make(nc, np + nq);
        double[] dmat = mat.getStorage();
        double[] data = new double[nc];
        System.arraycopy(m_data, m, data, 0, nc);
        // P
        for (int i = 1; i <= m_spec.getP(); ++i, ccur += nc) {
            System.arraycopy(m_data, m - i, dmat, ccur, nc);
        }
        // BP
        for (int i = m_spec.getPeriod(); i <= m_spec.getPeriod()
                * m_spec.getBp(); i += m_spec.getPeriod()) {
            for (int k = 0; k <= m_spec.getP(); ++k, ccur += nc) {
                System.arraycopy(m_data, m - i - k, dmat, ccur, nc);
            }
        }
        // Q
        for (int i = 1; i <= m_spec.getQ(); ++i, ccur += nc) {
            System.arraycopy(m_a, m - i, dmat, ccur, nc);
        }
        // BQ
        for (int i = m_spec.getPeriod(); i <= m_spec.getPeriod()
                * m_spec.getBq(); i += m_spec.getPeriod()) {
            for (int k = 0; k <= m_spec.getQ(); ++k, ccur += nc) {
                System.arraycopy(m_a, m - i - k, dmat, ccur, nc);
            }
        }
        m_pi = ls(mat, data, true);
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
    public boolean process(final DoubleSequence value, SarmaSpecification spec) {
        clear();
        m_spec = spec.clone();
        m_odata = value;
        m_data=value.toArray();
        AbsMeanNormalizer normalizer = new AbsMeanNormalizer();
        normalizer.normalize(DataBlock.ofInternal(m_data));
        return calc();
    }

    private void updatemodel() {
        if (m_pi == null) {
            return;
        }
        int ccur = 0;
        SarimaModel.Builder builder = SarimaModel.builder(m_spec);
        if (m_spec.getP() != 0) {
            for (int i = 1; i <= m_spec.getP(); ++i) {
                builder.phi(i, m_pi[ccur++]);
            }
        }

        if (m_spec.getBp() != 0) {
            for (int i = 1; i <= m_spec.getBp(); ++i) {
                builder.bphi(i, m_pi[ccur]);
                ccur += 1 + m_spec.getP();
            }
        }
        if (m_spec.getQ() != 0) {
            for (int i = 1; i <= m_spec.getQ(); ++i) {
                builder.theta(i, m_pi[ccur++]);
            }
        }
        if (m_spec.getBq() != 0) {
            for (int i = 1; i <= m_spec.getBq(); ++i) {
                builder.btheta(i, m_pi[ccur]);
                ccur += 1 + m_spec.getQ();
            }
        }
        m_model=builder.build();
    }
}
