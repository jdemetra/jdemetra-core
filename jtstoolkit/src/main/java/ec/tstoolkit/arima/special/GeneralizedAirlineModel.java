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
package ec.tstoolkit.arima.special;

import ec.tstoolkit.arima.AbstractArimaModel;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.arima.StationaryTransformation;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.data.SubArrayOfInt;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.Complex;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.maths.polynomials.UnitRoots;
import ec.tstoolkit.ssf.ISsf;
import ec.tstoolkit.ssf.ucarima.SsfUcarima;
import ec.tstoolkit.ssf.ucarima.SsfUcarimaWithMean;
import ec.tstoolkit.ucarima.ModelDecomposer;
import ec.tstoolkit.ucarima.SeasonalSelector;
import ec.tstoolkit.ucarima.TrendCycleSelector;
import ec.tstoolkit.ucarima.UcarimaModel;
import ec.tstoolkit.utilities.Ref;
import java.util.ArrayList;

/**
 * The generalized airline model as described in [1] John A. D. Aston, David F.
 * Findley, Kellie C. Wills, and Donald E. K. Martin (2004), "Generalizations of
 * the Box-Jenkins Airline Model with Frequency-Specific Seasonal Coefficients
 * and a Generalizaton of Akaike’s MAIC", presented at 2004 NBER/NSF Time Series
 * Conference http://www.census.gov/srd/www/sapaper.html
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Exploratory)
public class GeneralizedAirlineModel extends AbstractArimaModel implements
        IArimaModel, Cloneable {

    static long CKey(SubArrayOfInt c) {
        long l = 0;
        for (int i = 0, q = 1; i < c.getLength(); ++i, q <<= 1) {
            if (c.get(i) == 2) {
                l += q;
            }
        }
        return l;
    }

    // 3 parameters.
    // Normal case:
    // c: 0, 1 |, 2, 2, 1, ... (1 <= c < 3)
    // q: a, c0, c1, ....
    // 4 parameters.
    // Normal case:
    // c: -1, -1 |, 2, 2, 3, ... (2 <= c < 4)
    // q: a, b, c1, c2, ...
    // 1 fixed (real) root (r) in (1+aB+b*B*B)
    // c: 0, 1 |, 2, 2, 3, ... (2 <= c < 4)
    // q: r, s, c1, c2, ...
    double[] m_q;

    int[] m_c;

    int m_pow;

    boolean[] m_fixed;

    int m_freq;

    private boolean m_st;

    private BackFilter m_ma;

    private BackFilter m_ar;

    static double EPS = 1e-6;

    GeneralizedAirlineModel() {
    }

    // / <summary>Creates a new generalized airline model using a given set of
    // parameters</summary>
    // / <param name="freq">The frequency of the model (number of observations
    // by year)</param>
    // / <param name="p">
    // / The set of the parameters. Must be a 2-parameters (airline),
    // 3-parameters or
    // / 4-parameters set. See the detailed documentation for the meaning of the
    // different
    // / parameters sets.
    // / </param>
    // / <param name="c">
    // / <para class="MsoNormal" style="MARGIN: 0in 0in 0pt">
    // / <span style="mso-ansi-language: EN-US"><font size="2"><font
    // face="Arial">Array that
    // / identifies the parameters of each seasonal factor. The length of this
    // array is
    // / freq/2 and the values must be inside {1,2} or {2,3} following the type
    // of the model
    // / (3 or 4 parameters). Should be null for the airline model (2-parameters
    // / model).</font></font></span></para>
    // / </param>
    /**
     *
     * @param freq
     * @param p
     * @param c
     */
    public GeneralizedAirlineModel(int freq, double[] p, SubArrayOfInt c) {
        m_q = p.clone();
        int nparams = p.length;
        m_freq = freq;
        m_c = new int[freq / 2 + 2];
        if (c != null) {
            for (int i = 2; i < m_c.length; ++i) {
                m_c[i] = c.get(i - 2);
            }
        } else {
            for (int i = 2; i < m_c.length; ++i) {
                m_c[i] = 1;
            }
        }
        m_fixed = new boolean[nparams];
        if (nparams == 4) {
            m_c[0] = -1;
            m_c[1] = -1;
        } else {
            m_c[0] = 0;
            m_c[1] = 1;
        }
        m_pow = pow();
        m_st = false;
    }

    /**
     *
     * @param freq
     * @param nparams
     * @param q
     * @param sq
     * @param c
     */
    public GeneralizedAirlineModel(int freq, int nparams, double q, double sq,
            SubArrayOfInt c) {
        if (q <= 0) {
            q = .2;
        }
        if (sq <= 0) {
            sq = .2;
        }
        init(freq, nparams, q, sq, c);
    }

    /**
     *
     * @param freq
     * @param nparams
     * @param c
     */
    public GeneralizedAirlineModel(int freq, int nparams, int[] c) {
        if (nparams == 0 || c == null) {
            init(freq, nparams, .2, Math.pow(.2, 1.0 / freq), null);
        } else {
            int[] cc = new int[freq / 2];
            for (int i = 0; i < cc.length; ++i) {
                cc[i] = nparams - 2;
            }
            for (int i = 0; i < c.length; ++i) {
                cc[c[i] - 1] = nparams - 1;
            }
            init(freq, nparams, .2, Math.pow(.2, 1.0 / freq), SubArrayOfInt
                    .create(cc));
        }
    }

    // / <summary>
    // / Creates a new generalized airline model using the parameters of an
    // usual airline
    // / model
    // / </summary>
    // / <remarks>The current implementation doesn't verify the validity of the
    // parameters</remarks>
    // / <param name="freq">The frequency of the model (number of observations
    // by year)</param>
    // / <param name="nparams">Number of parameters of the model. 2, 3 or
    // 4.</param>
    // / <param name="q">The coefficient of the regular moving average part.
    // Must be inside [-1, 1]</param>
    // / <param name="sq">The coefficient of the regular moving average part.
    // Must be inside [0, 1]</param>
    // / <param name="c">
    // / <para class="MsoNormal" style="MARGIN: 0in 0in 0pt">
    // / <span style="mso-ansi-language: EN-US"><font size="2"><font
    // face="Arial">Array that
    // / identifies the parameters of each seasonal factor. The length of this
    // array is
    // / freq/2 and the values must be inside {1,2} or {2,3} following the type
    // of the model
    // / (3 or 4 parameters). Should be null for the airline model (2-parameters
    // / model).</font></font></span></para>
    // / </param>
    /**
     *
     * @param freq
     * @param nparams
     * @param c
     */
    public GeneralizedAirlineModel(int freq, int nparams, SubArrayOfInt c) {
        init(freq, nparams, .2, Math.pow(.2, 1.0 / freq), c);
    }

    private void calcAR() {
        if (m_ar == null) {
            if (!m_st) {
                UnitRoots ur = new UnitRoots();
                ur.add(1);
                ur.add(m_freq);
                m_ar = BackFilter.of(ur.coefficients());
            } else {
                m_ar = BackFilter.ONE;
            }
        }
    }

    private void calcMA() {
        if (m_ma == null) {
            double[] p = new double[m_freq + 2];
            // compute the polynomial corresponding to the zero frequency
            p[0] = 1;
            if (m_c[0] == -1) {
                p[1] = m_q[0];
                p[2] = m_q[1];
            } else {
                // (1-q[0]B)*(1-q[1]B) = 1-(q[0]+q[1])B+q[0]*q[1]*B*B
                p[1] = -(m_q[0] + m_q[1]);
                p[2] = m_q[0] * m_q[1];
            }
            int freq2 = (m_freq + 1) / 2;
            double z = Complex.TWOPI / m_freq;
            for (int i = 1; i < freq2; ++i) {
                double q = m_q[m_c[1 + i]];
                double b = -2 * q * Math.cos(z * i);
                double c = q * q;
                for (int j = 2 * i; j >= 0; --j) {
                    double w = p[j];
                    p[j + 2] += c * w;
                    p[j + 1] += b * w;
                }
            }
            if (m_freq % 2 == 0) {
                double q = m_q[m_c[1 + freq2]];
                for (int i = m_freq; i >= 0; --i) {
                    p[i + 1] += p[i] * q;
                }

            }
            m_ma = BackFilter.of(p);
        }
    }

    ArimaModel checkModel(double ubound) {
        Polynomial theta = getMA().getPolynomial();
        Ref<Polynomial> thetac = new Ref<>(null);
        ec.tstoolkit.maths.linearfilters.Utilities.stabilize(theta, ubound,
                thetac);
        return new ArimaModel(BackFilter.ONE, getNonStationaryAR(), new BackFilter(
                thetac.val), getInnovationVariance());
    }

    void checkRoots(double rmax) {
        boolean changed = false;
        int np = m_q.length;
        if (np == 4) {
            if (m_c[0] < 0) {
                double ro = m_q[0] * m_q[0] - 4 * m_q[1];
                if (ro >= 0) {
                    double sro = Math.sqrt(ro);
                    double r0 = (-m_q[0] + sro) / 2;
                    double r1 = (-m_q[0] - sro) / 2;
                    if (r0 > rmax) {
                        r0 = rmax;
                    }
                    if (r1 > rmax) {
                        r1 = rmax;
                    }

                    m_q[0] = -(r0 + r1);
                    m_q[1] = r0 * r1;
                    changed = true;
                }
            }
            double rmax1 = Math.pow(rmax, 1.0 / m_pow);
            double rmax2 = Math.pow(rmax, 1.0 / (m_freq - m_pow - 1));
            if (!m_fixed[2] && m_q[2] > rmax1) {
                m_q[2] = rmax1;
                changed = true;
            }
            if (!m_fixed[3] && m_q[3] > rmax1) {
                m_q[3] = rmax2;
                changed = true;
            }
            if (m_c[1] == 1 && !m_fixed[1] && m_q[1] > rmax) {
                m_q[1] = rmax;
                changed = true;
            }
        } else if (np == 3) {
            if (!m_fixed[0] && m_q[0] > rmax) {
                m_q[0] = rmax;
                changed = true;
            }
            double rmax1 = Math.pow(rmax, 1.0 / m_pow);
            double rmax2 = Math.pow(rmax, 1.0 / (m_freq - m_pow));
            if (!m_fixed[1] && m_q[1] > rmax1) {
                m_q[1] = rmax1;
                changed = true;
            }
            if (!m_fixed[2] && m_q[2] > rmax2) {
                m_q[2] = rmax2;
                changed = true;
            }
        } else if (np == 2) {
            if (!m_fixed[0] && m_q[0] > rmax) {
                m_q[0] = rmax;
                changed = true;
            }
            double rmax1 = Math.pow(rmax, 1.0 / m_freq);
            if (!m_fixed[1] && m_q[1] > rmax1) {
                m_q[1] = rmax1;
                changed = true;
            }
        }
        if (changed) {
            clearCachedObjects();
        }
    }

    ArimaModel checkUR() {
        fixUnitRoots(EPS);
        return new ArimaModel(BackFilter.ONE, getNonStationaryAR(), getMA(),
                getInnovationVariance());
    }

    @Override
    protected void clearCachedObjects() {
        super.clearCachedObjects();
        m_ma = null;
    }

    @Override
    public GeneralizedAirlineModel clone() {
        GeneralizedAirlineModel model = (GeneralizedAirlineModel) super.clone();

        if (m_c != null) {
            model.m_c = m_c.clone();
        }
        if (m_q != null) {
            model.m_q = m_q.clone();
        }
        return model;
    }

    void copy(GeneralizedAirlineModel ga) {
        clearCachedObjects();
        m_c = ga.m_c.clone();
        m_q = ga.m_q.clone();
        m_fixed = ga.m_fixed.clone();
        m_ma = ga.m_ma;
        m_st = ga.m_st;
        m_pow = ga.m_pow;
        m_freq = ga.m_freq;
    }

    private UcarimaModel defaultUCModel(double ubound) {
        ArimaModel model = (ubound == 1) ? checkUR() : checkModel(ubound);
        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(new TrendCycleSelector());
        decomposer.add(new SeasonalSelector(m_freq));
        UcarimaModel ucm = decomposer.decompose(model);
        ucm.setVarianceMax(-1);
        if (ucm.isValid()) {
            ucm.compact(2, 2);
            return ucm;
        } else {
            return null;
        }
    }

    /**
     *
     * @param st
     */
    public void doStationary(boolean st) {
        super.clearCachedObjects();
        m_st = st;
        m_ar = null;
    }

    // / <summary>Fixes the parameters that yield quasi-unit roots.</summary>
    // / <returns>
    // / True if the model has been changed (some quasi-unit roots have been
    // / fixed).
    // / </returns>
    // / <param name="eps">The small tolerance value for the identification of
    // quasi-unit roots.</param>
    /**
     *
     * @param eps
     * @return
     */
    public boolean fixUnitRoots(double eps) {
        int idx = 0;
        boolean rslt = false;
        if (m_c[0] < 0) {
            if (m_q[1] < 1 - eps && Math.abs(1 + m_q[0] + m_q[1]) < eps) {
                m_q[0] = 1;
                m_fixed[0] = true;
                m_c[0] = 0;
                m_c[1] = 1;
                rslt = true;
            }
            idx = 1;
        }
        for (int i = idx; i < m_q.length; ++i) {
            if (!m_fixed[i] && m_q[i] > 1 - eps) {
                m_q[i] = 1;
                m_fixed[i] = true;
                rslt = true;
            }
        }
        if (rslt) {
            clearCachedObjects();
        }
        return rslt;
    }

    /**
     *
     * @param ubound
     * @return
     */
    public ISsf fullSsfModel(double ubound) {
        if (ubound == 1) {
            checkUR();
        }
        if (!hasFixedParameters()) {
            UcarimaModel tmp = defaultUCModel(ubound);
            if (tmp == null) {
                return null;
            } else {
                return new SsfUcarima(tmp);
            }
        }
        BackFilter ar0 = getNonStationaryMA(0);
        BackFilter ar1 = getNonStationaryMA(1);

        // simplified model
        BackFilter ma = getStationaryMA(-1);
        BackFilter ar = getNonStationaryMAComplement(-1);

        ArimaModel arima = new ArimaModel(null, ar, ma, 1);
        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(new TrendCycleSelector());
        decomposer.add(new SeasonalSelector(m_freq));
        UcarimaModel ucm = decomposer.decompose(arima);
        ucm.setVarianceMax(-1);
        if (ucm.isValid()) {
            ucm.compact(2, 2);
        } else {
            return null;
        }
        if (ar0.getDegree() != 1) {
            ArrayList<ArimaModel> cmps = new ArrayList<>();
            cmps.add(ucm.getComponent(0));
            if (ar0.getDegree() > 0) {
                cmps.add(new ArimaModel(BackFilter.ONE, ar0, BackFilter.ONE, 0));
            }
            cmps.add(ucm.getComponent(1));
            if (ar1.getDegree() > 0) {
                cmps.add(new ArimaModel(BackFilter.ONE, ar1, BackFilter.ONE, 0));
            }
            cmps.add(ucm.getComponent(2));
            return new SsfUcarima(new UcarimaModel(this, cmps));
        } else {
            UcarimaModel ucmc = new UcarimaModel();
            ArimaModel trend = ucm.getComponent(0);
            ucmc.addComponent(ucm.getComponent(0));
            ucmc.addComponent(ucm.getComponent(1));
            if (ar1.getDegree() > 0) {
                ucmc.addComponent(new ArimaModel(BackFilter.ONE, ar1, BackFilter.ONE,
                        0));
            }
            ucmc.addComponent(ucm.getComponent(2));
            return SsfUcarimaWithMean.build(ucmc);
        }
    }

    /**
     *
     * @param ubound
     * @return
     */
    public UcarimaModel fullUCModel(double ubound) {
        if (ubound == 1) {
            checkUR();
        }
        if (!hasFixedParameters()) {
            return defaultUCModel(ubound);
        }
        BackFilter ar0 = getNonStationaryMA(0);
        BackFilter ar1 = getNonStationaryMA(1);

        // simplified model
        BackFilter ma = getStationaryMA(-1);
        BackFilter ar = getNonStationaryMAComplement(-1);

        ArimaModel arima = new ArimaModel(null, ar, ma, 1);
        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(new TrendCycleSelector());
        decomposer.add(new SeasonalSelector(m_freq));
        UcarimaModel ucm = decomposer.decompose(arima);
        ucm.setVarianceMax(-1);
        if (ucm.isValid()) {
            ucm.compact(2, 2);
        } else {
            return null;
        }
        ArrayList<ArimaModel> cmps = new ArrayList<>();
        if (ar0.getDegree() != 1) {
            cmps.add(ucm.getComponent(0));
            cmps.add(new ArimaModel(BackFilter.ONE, ar0, BackFilter.ONE, 0));
        } else {
            ArimaModel trend = ucm.getComponent(0);
            cmps.add(new ArimaModel(trend.getStationaryAR(), trend
                    .getNonStationaryAR().times(ar0), trend.getMA().times(ar0),
                    trend.getInnovationVariance()));
            cmps.add(new ArimaModel(null, null, null, 0));
        }
        cmps.add(ucm.getComponent(1));
        cmps.add(new ArimaModel(BackFilter.ONE, ar1, BackFilter.ONE, 0));
        cmps.add(ucm.getComponent(2));
        return new UcarimaModel(this, cmps);
    }

    // / <summary>Auto-regressive polynomial.</summary>
    @Override
    public BackFilter getAR() {
        if (m_st) {
            return BackFilter.ONE;
        }
        calcAR();
        return m_ar;
    }

    @Override
    public int getARCount() {
        return m_st ? 0 : m_freq + 1;
    }

    /**
     *
     * @return
     */
    public int[] getC() {
        return m_c;
    }

    // / <summary>The coefficients of the model.</summary>
    // / <value>A 2, 3 or 4-parameters array.</value>
    // / <remarks>The meaning of each returned parameter is defined by the C
    // property.</remarks>
    // / <seealso cref="C">C Property</seealso>
    /**
     *
     * @return
     */
    public double[] getCoefficients() {
        return m_q;
    }

    /**
     *
     * @return
     */
    public int getFrequency() {
        return m_freq;
    }

    /**
     *
     * @return
     */
    public int[] getFullModelType() {
        int nparams = m_q.length;
        if (nparams == 2) {
            return null;
        }
        int gcount = 0;
        for (int i = 0; i < m_c.length; ++i) {
            if (m_c[i] == nparams - 1) {
                ++gcount;
            }
        }
        int[] rslt = new int[gcount];
        for (int i = 2, j = 0; i < m_c.length; ++i) {
            if (m_c[i] == nparams - 1) {
                rslt[j++] = i - 1;
            }
        }

        return rslt;
    }

    // / <summary>Frequency of the model. It is the power of the periodic
    // backward operator.</summary>
    // / <remarks>The model is not limited to the usual frequencies.</remarks>
    // / <summary>Innovation variance</summary>
    @Override
    public double getInnovationVariance() {
        return 1;
    }

    // / <summary><para>Moving average polynomial.</para></summary>
    @Override
    public BackFilter getMA() {
        calcMA();
        return m_ma;
    }

    // / <summary>length of the moving average polynomial</summary>
    @Override
    public int getMACount() {
        return m_freq + 1;
    }

    // / <summary>Type of the model.</summary>
    // / <value>A literal identification of the current model.</value>
    // / <remarks>
    // / The return value only gives a partial identification of the model: for
    // example,
    // / 5-1(3). Further information is obtained through the <em>C</em> property
    // / </remarks>
    /**
     *
     * @return
     */
    public String getModelType() {
        int nparams = m_q.length;
        if (nparams == 2) {
            return "Airline (2)";
        }
        int gcount = 0;
        for (int i = 0; i < m_c.length; ++i) {
            if (m_c[i] == nparams - 1) {
                ++gcount;
            }
        }
        StringBuilder builder = new StringBuilder();
        builder.append(m_freq / 2 - gcount);
        builder.append('-');
        builder.append(gcount);
        builder.append(" (").append(nparams).append(')');

        int[] ids = getFullModelType();
        if (ids != null) {
            builder.append('{');
            for (int j = 0; j < ids.length; ++j) {
                if (j != 0) {
                    builder.append('-');
                }
                builder.append(ids[j]);
            }
            builder.append('}');
        }
        return builder.toString();
    }

    @Override
    public BackFilter getNonStationaryAR() {
        if (m_st) {
            return BackFilter.ONE;
        } else {
            UnitRoots ur = new UnitRoots();
            ur.add(m_freq);
            ur.add(1);
            return new BackFilter(ur.toPolynomial());
        }
    }

    @Override
    public int getNonStationaryARCount() {
        return m_st ? 0 : m_freq + 1;
    }

    // / <summary>
    // /
    // / </summary>
    // / <param name="part">part = -1 : all, part = 0: zero-freq, part = 1:
    // non-zero freq</param>
    // / <returns></returns>
    /**
     *
     * @param part
     * @return
     */
    public BackFilter getNonStationaryMA(int part) {
        double[] p = new double[m_freq + 2];
        // compute the polynomial corresponding to the zero frequency
        p[0] = 1;
        int dcur = 0;
        if (part <= 0) {
            if (m_fixed[0] && m_fixed[1]) {
                p[1] = -2;
                p[2] = 1;
                dcur = 2;
            } else if (m_fixed[0] || m_fixed[1]) {
                p[1] = -1;
                dcur = 1;
            }
        }
        if (part != 0) {
            int freq2 = (m_freq + 1) / 2;
            double z = Complex.TWOPI / m_freq;
            for (int i = 1; i < freq2; ++i) {
                if (m_fixed[m_c[1 + i]]) {
                    double b = -2 * Math.cos(z * i);
                    for (int j = dcur; j >= 0; --j) {
                        double w = p[j];
                        p[j + 2] += w;
                        p[j + 1] += b * w;
                    }
                    dcur += 2;
                }
            }
            if (m_freq % 2 == 0) {
                if (m_fixed[m_c[1 + freq2]]) {
                    for (int i = dcur; i >= 0; --i) {
                        p[i + 1] += p[i];
                    }
                    ++dcur;
                }
            }
        }

        double[] q = new double[dcur + 1];
        System.arraycopy(p, 0, q, 0, q.length);
        return BackFilter.of(q);
    }

    // / <summary>
    // /
    // / </summary>
    // / <param name="part">part = -1 : all, part = 0: zero-freq, part = 1:
    // non-zero freq</param>
    // / <returns></returns>
    /**
     *
     * @param part
     * @return
     */
    public BackFilter getNonStationaryMAComplement(int part) {
        double[] p = new double[m_freq + 2];
        // compute the polynomial corresponding to the zero frequency
        p[0] = 1;
        int dcur = 0;
        if (part <= 0) {
            if (!m_fixed[0] && !m_fixed[1]) {
                p[1] = -2;
                p[2] = 1;
                dcur = 2;
            } else if (!m_fixed[0] || !m_fixed[1]) {
                p[1] = -1;
                dcur = 1;
            }
        }
        if (part != 0) {
            int freq2 = (m_freq + 1) / 2;
            double z = Complex.TWOPI / m_freq;
            for (int i = 1; i < freq2; ++i) {
                if (!m_fixed[m_c[1 + i]]) {
                    double b = -2 * Math.cos(z * i);
                    double c = 1;
                    for (int j = dcur; j >= 0; --j) {
                        double w = p[j];
                        p[j + 2] += c * w;
                        p[j + 1] += b * w;
                    }
                    dcur += 2;
                }
            }
            if (m_freq % 2 == 0) {
                if (!m_fixed[m_c[1 + freq2]]) {
                    for (int i = dcur; i >= 0; --i) {
                        p[i + 1] += p[i];
                    }
                    ++dcur;
                }
            }
        }

        double[] q = new double[dcur + 1];
        System.arraycopy(p, 0, q, 0, q.length);
        return BackFilter.of(q);
    }

    // / <summary>
    // /
    // / </summary>
    // / <param name="part">part = -1 : all, part = 0: zero-freq, part = 1:
    // non-zero freq</param>
    // / <returns></returns>
    // / <summary>Non fixed parameter</summary>
    /**
     *
     * @param idx
     * @return
     */
    public double getParameter(int idx) {
        for (int i = 0, j = 0; i < m_q.length; ++i) {
            if (!m_fixed[i]) {
                if (j == idx) {
                    return m_q[i];
                } else {
                    ++j;
                }
            }
        }
        return 0;
    }

    // / <summary>Array of the non fixed parameters</summary>
    /**
     *
     * @return
     */
    public IReadDataBlock getParameters() {
        double[] p = new double[getParametersCount()];
        for (int i = 0, j = 0; i < m_q.length; ++i) {
            if (!m_fixed[i]) {
                p[j++] = m_q[i];
            }
        }
        return new ReadDataBlock(p);
    }

    /**
     *
     * @return
     */
    public int getParametersCount() {
        int n = 0;
        for (int i = 0; i < m_q.length; ++i) {
            if (!m_fixed[i]) {
                ++n;
            }
        }
        return n;
    }

    @Override
    public BackFilter getStationaryAR() {
        return BackFilter.ONE;
    }

    @Override
    public int getStationaryARCount() {
        return 0;
    }

    /**
     *
     * @param part
     * @return
     */
    public BackFilter getStationaryMA(int part) {
        double[] p = new double[m_freq + 2];
        p[0] = 1;
        int dcur = 0;
        if (part <= 0) {
            if (m_c[0] == -1) {
                p[1] = m_q[0];
                p[2] = m_q[1];
                dcur = 2;
            } else if (!m_fixed[0] && !m_fixed[1]) {
                // (1-q[0]B)*(1-q[1]B) = 1-(q[0]+q[1])B+q[0]*q[1]*B*B
                p[1] = -(m_q[0] + m_q[1]);
                p[2] = m_q[0] * m_q[1];
                dcur = 2;
            } else if (!m_fixed[0]) {
                p[1] = -m_q[0];
                dcur = 1;
            } else if (!m_fixed[1]) {
                p[1] = -m_q[1];
                dcur = 1;
            }
        }
        if (part != 0) {
            int freq2 = (m_freq + 1) / 2;
            double z = Complex.TWOPI / m_freq;
            for (int i = 1; i < freq2; ++i) {
                if (!m_fixed[m_c[1 + i]]) {
                    double q = m_q[m_c[1 + i]];
                    double b = -2 * q * Math.cos(z * i);
                    double c = q * q;
                    for (int j = dcur; j >= 0; --j) {
                        double w = p[j];
                        p[j + 2] += c * w;
                        p[j + 1] += b * w;
                    }
                    dcur += 2;
                }
            }
            if (m_freq % 2 == 0) {
                if (!m_fixed[m_c[1 + freq2]]) {
                    double q = m_q[m_c[1 + freq2]];
                    for (int i = dcur; i >= 0; --i) {
                        p[i + 1] += q * p[i];
                    }
                    ++dcur;
                }
            }
        }

        double[] rslt = Polynomial.Doubles.fromDegree(dcur);
        System.arraycopy(p, 0, rslt, 0, dcur + 1);
        return BackFilter.of(rslt);
    }

    // / <summary>Type of the model (equals the number of
    // coefficients).</summary>
    // / <value>2 (airline), 3, 4.</value>
    /**
     *
     * @return
     */
    public int getType() {
        return m_q.length;
    }

    /**
     *
     * @return
     */
    public boolean hasFixedParameters() {
        for (int i = 0; i < m_fixed.length; ++i) {
            if (m_fixed[i]) {
                return true;
            }
        }
        return false;
    }

    private void init(int freq, int nparams, double q, double sq,
            SubArrayOfInt c) {
        m_freq = freq;
        m_c = new int[freq / 2 + 2];
        if (c != null) {
            for (int i = 2; i < m_c.length; ++i) {
                m_c[i] = c.get(i - 2);
            }
        } else {
            for (int i = 2; i < m_c.length; ++i) {
                m_c[i] = 1;
            }
        }

        m_q = new double[nparams];
        m_fixed = new boolean[nparams];
        int idx = 0;
        if (nparams == 4) {
            m_q[0] = -(q + sq);
            m_q[1] = q * sq;
            m_c[0] = -1;
            m_c[1] = -1;
            idx = 2;
        } else {
            m_q[0] = q;
            m_c[0] = 0;
            m_c[1] = 1;
            idx = 1;
        }
        for (int i = idx; i < nparams; ++i) {
            m_q[i] = sq;
        }
        m_pow = pow();
        m_st = false;
    }

    // / <summary>Identifies fixed parameters.</summary>
    // / <returns>True if a parameter has been fixed.</returns>
    // / <remarks>
    // / Fixed parameters are no more considered in the properties and methods
    // of the
    // / IParametriseable interface. So, they are no more used in optimization
    // procedure.
    // / </remarks>
    // / <param name="idx">The index of the parameter</param>
    /**
     *
     * @param idx
     * @return
     */
    public boolean isFixed(int idx) {
        return m_fixed[idx];
    }

    // / <summary>Meaning of the coefficients</summary>
    // / <value>A (2+Frequency/2) - parameters length.</value>
    // / <remarks>
    // / <para>The possible value of the items of C are:</para>
    // / <para>-1 : coefficients of the polynomial factor (1+aB+bB^2)</para>
    // / <para>&gt;=0: <em>Coefficients[C[i]]</em> is the i-th coefficient of
    // the
    // / model.</para>
    // / </remarks>
    // / <summary>Checks if the model is invertible.</summary>
    // / <remarks>
    // / A model is invertible if all the roots of its moving average polynomial
    // lie
    // / outside the unit circle.
    // / </remarks>
    @Override
    public boolean isInvertible() {
        int idx = 0;
        if (m_c[1] == -1) {
            // unit root or complex roots (with norm = 1)
            if (Math.abs(1 + m_q[0] + m_q[1]) <= EPS
                    || Math.abs(m_q[1] - 1) <= EPS * EPS) {
                return false;
            }
            idx = 2;
        }

        for (int i = idx; i < m_q.length; ++i) {
            if (Math.abs(1 - m_q[i]) <= EPS) {
                return false;
            }
        }

        return true;
    }

    // / <summary>Checks if the model is null.</summary>
    // / <remarks>
    // / <para>A model is null if its innovation variance is 0 AND if its AR
    // polynomial is
    // / 1.</para>
    // / </remarks>
    @Override
    public boolean isNull() {
        return false;
    }

    // / <summary>Checks if the model is stationary</summary>
    // / <remarks>
    // / A model is stationary if the roots of its auto-regressive polynomial
    // lie outside
    // / the unit circle. By construction, the generalized airline model is not
    // stationary.
    // / However, the <em>DoStationary</em> method removes the auto-regressive
    // polynomial while
    // / keeping unchanged the moving average part.
    // / </remarks>
    @Override
    public boolean isStationary() {
        return m_st;
    }

    final int pow() {
        int n = (m_freq - 1) / 2;
        int nparams = m_q.length;
        int idx = nparams == 4 ? 2 : 1;

        int p = nparams == 4 ? 0 : 1;
        for (int i = 1; i <= n; ++i) {
            if (m_c[i + 1] == idx) {
                p += 2;
            }
        }
        if (m_freq % 2 == 0 && m_c[n + 2] == idx) {
            ++p;
        }
        return p;
    }

    /**
     *
     * @param u
     */
    public void resetFixedParameters(double u) {
        for (int i = 0; i < m_q.length; ++i) {
            if (m_fixed[i]) {
                m_fixed[i] = false;
                m_q[i] = u;
            }
        }
        clearCachedObjects();
    }

    /**
     *
     * @param idx
     * @param value
     */
    public void setParameter(int idx, double value) {
        for (int i = 0, j = 0; i < m_q.length; ++i) {
            if (!m_fixed[i]) {
                if (j == idx) {
                    m_q[i] = value;
                    clearCachedObjects();
                    return;
                } else {
                    ++j;
                }
            }
        }
    }

    /**
     *
     * @param value
     */
    public void setParameters(IReadDataBlock value) {
        for (int i = 0, j = 0; i < m_q.length; ++i) {
            if (!m_fixed[i]) {
                m_q[i] = value.get(j++);
            }
        }

        clearCachedObjects();
    }

    // / <summary>Copy of the current object</summary>
    // / <returns>A copy of the current object</returns>
    // / <summary>Do the stationary transformation of the generalized airline
    // model.</summary>
    // / <returns>
    // / A "stationary generalized airline" model. It only contains the moving
    // avergae
    // / part of the initial model.
    // / </returns>
    // / <param name="ur">The unit roots of the auto-regressive model (=
    // (1-B)(1-B^s)).</param>
    /**
     *
     * @return
     */
    @Override
    public StationaryTransformation stationaryTransformation() {
        //UnitRoots ur = new UnitRoots();
        calcAR();
        calcMA();
        GeneralizedAirlineModel ga = clone();
        ga.doStationary(true);

        StationaryTransformation transformation = new StationaryTransformation(
                ga, m_ar);
        return transformation;
    }

    // / <summary>Do a canonical decomposition of the model.</summary>
    // / <returns>The canonical decomposition. Null if the model is not
    // decomposable.</returns>
    // / <remarks>
    // / <para>It should be noted that if the "ubound" parameter is too close to
    // 1 (for
    // / instance 0.9999), some numerical problems may appear. Values like 0.99
    // or 0.999
    // / seem good candidates.</para>
    // / <para>If "ubound" is set to 1, the canonical decomposition contains
    // components with
    // / common unit roots in their AR and MA polynomials. "ubound" = 1 also
    // automatically
    // / calls FixUnitRoots().</para>
    // / </remarks>
    // / <param name="ubound">
    // / Must be 1 or a slightly lower value. <em>ubound</em> is the upper bound
    // of the
    // / inverse of the norm for any root of the moving-average polynomial. The
    // parameters of
    // / the result can be changed to fit that condition.
    // / </param>
    /**
     *
     * @param ubound
     * @return
     */
    public UcarimaModel toUCModel(double ubound) {
        if (ubound < 1) {
            return defaultUCModel(ubound);
        }
        checkUR();
        BackFilter ar0 = getNonStationaryMA(0);
        BackFilter ar1 = getNonStationaryMA(1);

        // simplified model
        BackFilter ma = getStationaryMA(-1);
        BackFilter ar = getNonStationaryMAComplement(-1);

        ArimaModel arima = new ArimaModel(null, ar, ma, 1);
        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(new TrendCycleSelector());
        decomposer.add(new SeasonalSelector(m_freq));
        UcarimaModel ucm = decomposer.decompose(arima);
        ucm.setVarianceMax(-1);
        if (ucm.isValid()) {
            ucm.compact(2, 2);
        } else {
            return null;
        }

        ArrayList<ArimaModel> cmps = new ArrayList<>();
        ArimaModel trend = ucm.getComponent(0);
        if (ar0.getDegree() > 0) {
            cmps.add(new ArimaModel(trend.getStationaryAR(), trend
                    .getNonStationaryAR().times(ar0), trend.getMA().times(ar0),
                    trend.getInnovationVariance()));
        } else {
            cmps.add(trend);
        }
        ArimaModel seas = ucm.getComponent(1);
        if (ar1.getDegree() > 0) {
            cmps.add(new ArimaModel(seas.getStationaryAR(), seas
                    .getNonStationaryAR().times(ar1), seas.getMA().times(ar1),
                    seas.getInnovationVariance()));
        } else {
            cmps.add(seas);
        }

        cmps.add(ucm.getComponent(2));
        return new UcarimaModel(this, cmps);
    }
}
