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
package ec.tstoolkit.stats;

import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.dstats.Chi2;
import ec.tstoolkit.dstats.TestType;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class BoxPierceTest extends StatisticalTest {

    AutoCorrelations ac;

    private int m_lag = 1;
    private boolean m_pos;
    private int m_k = 12;

    private int m_hp;

    /**
     * Creates new LjungBoxTest
     */
    public BoxPierceTest() {
    }

    /**
     *
     * @return
     */
    public int getHyperParametersCount() {
        return m_hp;
    }

    /**
     *
     * @return
     */
    public int getK() {
        return m_k;
    }

    /**
     *
     * @return
     */
    public int getLag() {
        return m_lag;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isValid() {
        if (! m_computed) {
            test();
        }
        return m_dist != null;
    }

    /**
     *
     * @param value
     */
    public void setHyperParametersCount(final int value) {
        if (m_hp != value) {
            m_hp = value;
            clear();
        }
    }

    /**
     *
     * @param value
     */
    public void setK(final int value) {
        if (m_k != value) {
            m_k = value;
            clear();
        }
    }

    /**
     * By default, by setting a lag greater than 1, the use of positive
     * auto-correlations (ac) in enabled (negative ac are considered as 0 and no
     * further ac are taken into account). The method "usePositiveAc(true)"
     * should be called to disable this behaviour
     *
     * @param value
     */
    public void setLag(final int value) {
        if (m_lag != value) {
            m_lag = value;
            m_pos = m_lag > 1;
            clear();
        }
    }

    public void usePositiveAc(boolean pos) {
        m_pos = pos;
        clear();
    }

    private void test() {
        try {
            ac.setKMax(m_k * m_lag);
            if (ac.getKMax() < m_k * m_lag) {
                m_dist = null;
                return;
            }

            double[] a = ac.getAC();

            int n = ac.stats.getDataCount();
            double res = 0.0;
            for (int i = 1; i <= m_k; i++) {
                double ai = a[i * m_lag - 1];
                if (m_pos && ai < 0) {
                    break;
                }
                res += ai * ai;
            }
            m_val = res * n;
            Chi2 chi = new Chi2();
            chi.setDegreesofFreedom(m_lag == 1 ? (m_k - m_hp) : m_k);
            m_dist = chi;
            m_type = TestType.Upper;
            m_asympt = true;
        } catch (Exception err) {
            m_dist = null;
        } finally {
            m_computed = true;
        }
    }

    /**
     *
     * @param ac
     */
    public void test(AutoCorrelations ac) {
        this.ac = ac;
        test();
    }

    /**
     *
     * @param data
     */
    public void test(IReadDataBlock data) {
        ac = new AutoCorrelations(data);
        test();
    }
}
