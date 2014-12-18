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
package ec.tstoolkit.ssf;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.data.SubArrayOfInt;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;

/**
 * Ordinary Kalman filter
 *
 * @param <F>
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class Filter<F extends ISsf> {

    /**
     *
     */
    public static int fnCalls = 0;
    private State m_state;
    private F m_ssf;
    private ISsfData m_data;
    private ISsfInitializer<F> m_initializer;
    private int m_pos, m_end, m_r, m_steadypos = -1;
    private int[] m_idxR;
    private Matrix m_Q, m_WQW;
    private Matrix m_W;
    private boolean m_bsteady, m_fixedsteadypos, m_qinit;
    // for steady state
    /**
     *
     */
    protected double m_lastff;

    // private int m_nchecks = 12, m_curcheck = 0;
    // private double m_oldf = 0;
    // private double m_eps = 0;//State.g_Zero;
    /**
     *
     */
    public Filter() {
    }

    /**
     *
     * @param ssf
     * @param initializer
     */
    public Filter(final F ssf, final ISsfInitializer<F> initializer) {
        m_ssf = ssf;
        m_initializer = initializer;
    }

    private void addRQR(final Matrix P) { // RQR'
        if (!m_ssf.hasTransitionRes(m_pos)) {
            return;
        }
        if (m_WQW != null) {
            if (!m_ssf.hasR()) {
                P.add(m_WQW);
            } else {
                int nr = m_idxR.length;
                for (int i = 0; i < nr; ++i) {
                    for (int j = 0; j <= i; ++j) {
                        double w = m_WQW.get(i, j);
                        P.add(m_idxR[i], m_idxR[j], w);
                        if (i != j) {
                            P.add(m_idxR[j], m_idxR[i], w);
                        }
                    }
                }
            }
        }
    }

    private void checkSteadyState() {
        /*
         * if (m_pos == m_steadypos) { m_bsteady = true; return; } if (m_oldf ==
         * 0 || (m_oldf - m_state.f) / m_state.f > m_eps) { m_oldf = m_state.f;
         * m_curcheck = 0; } else ++m_curcheck; if (m_curcheck == m_nchecks) {
         * m_steadypos = m_pos; m_bsteady = true; }
         */
    }

    /**
     *
     */
    public void epred() {
        // calc ff and fi
        // fi = Z Pi Z' , ff = Z Pf Z' + H
        // m_fi=m_Pi.quadraticForm(m_Z);
        // m_ff=m_Pf.quadraticForm(m_Z)+m_h;

        if (!m_bsteady) {
            m_state.f = m_ssf.ZVZ(m_pos, m_state.P.subMatrix());
            if (m_state.f < State.ZERO) {
                m_state.f = 0;
            }
            m_lastff = m_state.f;
        }
        if (m_data.hasData()) {
            double y = m_data.get(m_pos);
            if (Double.isNaN(y)) {
                m_bsteady = false;
                m_state.e = Double.NaN;
            } else {
                m_ssf.ZM(m_pos, m_state.P.subMatrix(), m_state.C);
                m_ssf.TX(m_pos, m_state.C);
                m_state.e = y - m_ssf.ZX(m_pos, m_state.A);
                if (m_state.f == 0) {
                    if (Math.abs(m_state.e) > State.EPS) {
                        throw new SsfException(SsfException.INCONSISTENT);
                    } else {
                        m_state.e = 0;
                    }
                }
            }
        } else {
            m_ssf.ZM(m_pos, m_state.P.subMatrix(), m_state.C);
            m_ssf.TX(m_pos, m_state.C);
            m_state.e = 0;
        }

    }

    /**
     *
     * @return
     */
    public ISsfInitializer<F> getInitializer() {
        return m_initializer;
    }

    /**
     *
     * @return
     */
    public double getLastFf() {
        return m_lastff;
    }

    private boolean getModelInfo() {
        try {
            // m_ssf.Z(m_pos, m_Z);
            if (!m_ssf.hasTransitionRes(m_pos)
                    || (m_qinit && m_ssf.isTransitionEquationTimeInvariant())) {
                return true;
            }
            if (m_idxR != null && m_ssf.hasR()) {
                SubArrayOfInt R = SubArrayOfInt.create(m_idxR);
                R.set(0);
                m_ssf.R(m_pos, R);
            }
            if (m_Q != null) {
                m_Q.set(0);
                m_ssf.Q(m_pos, m_Q.subMatrix());
            }
            if (m_W != null && m_ssf.hasW()) {
                m_W.set(0);
                m_ssf.W(m_pos, m_W.subMatrix());
                SymmetricMatrix.quadraticFormT(m_Q.subMatrix(),
                        m_W.subMatrix(), m_WQW.subMatrix());
                if (m_ssf.isTransitionEquationTimeInvariant()) {
                    m_qinit = true;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     *
     * @return
     */
    public F getSsf() {
        return m_ssf;
    }

    /**
     *
     * @return
     */
    public State getState() {
        return m_state;
    }

    /**
     *
     * @return
     */
    public int getSteadyStatePosition() {
        return m_steadypos;
    }

    private boolean initFilter() {
        m_qinit = false;
        m_pos = 0;
        m_bsteady = false;
        if (!m_fixedsteadypos) {
            m_steadypos = -1;
        }
        m_lastff = 0;
        m_r = m_ssf.getStateDim();
        m_end = m_data.getCount();
        m_pos = 0;

        int rescount = m_ssf.getTransitionResCount(), resdim = m_ssf.getTransitionResDim();

        if (rescount != 0) {
            m_Q = new Matrix(resdim, resdim);
            if (m_ssf.hasR()) {
                m_idxR = new int[rescount];
            } else {
                m_idxR = null;
            }
            if (m_ssf.hasW()) {
                m_W = new Matrix(rescount, resdim);
            } else {
                m_W = null;
            }
            if (m_W == null) {
                m_WQW = m_Q;
            } else {
                m_WQW = new Matrix(rescount, rescount);
            }
        } else {
            m_Q = null;
            m_idxR = null;
            m_W = null;
            m_WQW = null;
        }
        getModelInfo();
        return true;
    }

    private int initState(final IFilteringResults rslts) {
        m_state = new State(m_r, m_data.hasData());
        if (m_initializer != null) {
            return m_initializer.initialize(m_ssf, m_data, m_state, rslts);
        } else if (!m_ssf.isDiffuse()) {
            new DefaultSsfInitializer().initialize(m_ssf, m_data, m_state,
                    rslts);
            return 0;
        } else {
            DurbinKoopmanInitializer dk = new DurbinKoopmanInitializer();
            return dk.initialize(m_ssf, m_data, m_state, rslts);
        }
    }

    /**
     *
     * @return
     */
    public boolean isInSteadyState() {
        return m_bsteady;
    }

    @SuppressWarnings("unused")
    private boolean isNull(final Matrix P) {
        return P.isZero(BaseState.EPS);
    }

    /**
     *
     */
    public void next() {
        if (m_state.isMissing()) {
            nextMissing();
        } else {
            if (!m_bsteady) {

                // F = ZPZ' + H
                // m_f=m_P.quadraticForm(m_Z)+m_h;
                // M = PZ'
                // P = TPT' - (TM)*(TM)' / f + RQR' --> Symmetric
                // A = Ta + (TM) v / f
                m_ssf.TVT(m_pos, m_state.P.subMatrix());
                if (m_state.f != 0) {
                    DataBlockIterator cols = m_state.P.columns();
                    DataBlock col = cols.getData();
                    int pos = 0;
                    do {
                        double c = -m_state.C.get(pos) / m_state.f;
                        if (pos > 0) {
                            col.drop(pos, 0).addAY(c, m_state.C.drop(pos, 0));
                        } else {
                            col.addAY(c, m_state.C);
                        }
                        ++pos;
                    } while (cols.next());
                    SymmetricMatrix.fromLower(m_state.P);
                }
                // RQR'
                addRQR(m_state.P);
                if (m_ssf.isTimeInvariant()) {
                    checkSteadyState();
                }
            }

            // compute Ta in tmp
            // prod(n, m_T, m_a, m_tmp);
            // v = y(t)-Z*A
            if (m_data.hasData()) {
                m_ssf.TX(m_pos, m_state.A);
                if (m_state.e != 0) {
                    double v = m_state.e / m_state.f;
                    m_state.A.addAY(v, m_state.C);
                }
            }
        }
    }

    /**
     *
     * @return
     */
    public boolean nextForecast() {
        if (!m_ssf.isTimeInvariant() && !getModelInfo()) {
            return false;
        }
        next();
        ++m_pos;
        return true;
    }

    private void nextMissing() {
        // variance
        m_ssf.TVT(m_pos, m_state.P.subMatrix());
        addRQR(m_state.P);

        // state
        m_ssf.TX(m_pos, m_state.A);
    }

    /**
     *
     * @param data
     * @param rslts
     * @return
     */
    public boolean process(final ISsfData data, final IFilteringResults rslts) {
        if (m_ssf == null) {
            return false;
        }
        ++fnCalls;
        m_data = data;
        if (!initFilter()) {
            return false;
        }
        m_pos = initState(rslts);
        if (m_pos < 0) {
            return false;
        }
        if (rslts != null) {
            rslts.prepare(m_ssf, m_data);
        }
        if (m_pos < m_end) {
            do {
                if (!m_ssf.isTimeInvariant() && !getModelInfo()) {
                    return false;
                }
                epred();
                if (rslts != null) {
                    rslts.save(m_pos, m_state);
                }
                next();
            } while (++m_pos < m_end);
        }
        if (rslts != null) {
            rslts.close();
        }
        return true;
    }

    /**
     *
     * @param value
     */
    public void setInitializer(final ISsfInitializer<F> value) {
        m_initializer = value;
    }

    /**
     *
     * @param value
     */
    public void setSsf(final F value) {
        m_ssf = value;
    }

    /**
     *
     * @param value
     */
    public void setSteadyStatePosition(final int value) {
        m_steadypos = value;
        m_fixedsteadypos = m_steadypos > 0;
    }
}
