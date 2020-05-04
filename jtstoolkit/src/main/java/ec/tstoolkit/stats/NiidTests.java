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

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.dstats.DStatException;
import ec.tstoolkit.dstats.TestType;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class NiidTests {
    // / <summary>
    // /
    // / </summary>
    // / <param name="data">Data</param>
    // / <param name="freq">Frequency. Could be 0. In that case seasonal test
    // are not computed</param>
    // / <param name="nhp">Number of hyper-parameters. Could be 0</param>

    /**
     *
     * @param freq
     * @return
     */
    public static int calcLBLength(int freq) {
        int n = 0;
        if (freq == 12) {
            n = 24;
        } else if (freq == 1) {
            n = 8;
        } else {
            n = 4 * freq;
        }
        return n;
    }

    private DescriptiveStatistics stats;

    private AutoCorrelations ac, ac2;

    private LjungBoxTest m_lb, m_lb2, m_lbs;

    private BoxPierceTest m_bp, m_bp2, m_bps;

    private MeanTest m_mean;

    private DoornikHansenTest m_dh;

    private SkewnessTest m_skewness;

    private KurtosisTest m_kurtosis;

    private TestofRuns m_runs;

    private TestofUpDownRuns m_udruns;

    // Data
    private int m_freq, m_nhp;

    private int m_k, m_ks;

    private boolean m_seas;

    /**
     *
     * @param data
     * @param freq
     * @param nhp
     * @param seas
     */
    public NiidTests(IReadDataBlock data, int freq, int nhp, boolean seas) {
        stats = new DescriptiveStatistics(data);
        m_freq = freq;
        m_seas = seas && freq > 1;
        m_nhp = nhp;
        ac = new AutoCorrelations(stats);
        ac.setKMax(calcLBLength(freq <= 1 ? 6 : freq));
        calcAC2();
        ac2.setKMax(calcLBLength(freq <= 1 ? 6 : freq));
    }

    private void calcAC2() {
        double[] data = stats.internalStorage();
        double[] d2 = new double[data.length];
        for (int i = 0; i < d2.length; ++i) {
            double cur = data[i];
            if (Double.isFinite(cur)) {
                d2[i] = cur * cur;
            } else {
                d2[i] = Double.NaN;
            }
        }
        ac2 = new AutoCorrelations(new DataBlock(d2));
        ac2.setCorrectedForMean(true);
    }

    /**
     *
     * @return
     */
    public AutoCorrelations getAutoCorrelations() {
        return ac;
    }

    /**
     *
     * @return
     */
    public AutoCorrelations getAutoCorrelationsOnSquare() {
        return ac2;
    }

    /**
     *
     * @return
     */
    public BoxPierceTest getBoxPierce() {

        if (m_bp == null)
	    try {
            int k = m_k != 0 ? m_k : calcLBLength(m_freq);

            m_bp = new BoxPierceTest();
            m_bp.setHyperParametersCount(m_nhp);
            m_bp.setLag(1);
            m_bp.setK(k);
            m_bp.test(ac);
        } catch (StatException ex) {
            m_bp = null;
            return null;
        } catch (DStatException ex) {
            m_runs = null;
            return null;
        }
        return m_bp.isValid() ? m_bp : null;
    }

    /**
     *
     * @return
     */
    public BoxPierceTest getBoxPierceOnSquare() {
        if (m_bp2 == null)
	    try {
            int k = m_k != 0 ? m_k : calcLBLength(m_freq);

            m_bp2 = new BoxPierceTest();
            m_bp2.setHyperParametersCount(m_nhp);
            m_bp2.setLag(1);
            m_bp2.setK(k);
            m_bp2.test(ac2);
        } catch (StatException ex) {
            m_bp2 = null;
            return null;
        } catch (DStatException ex) {
            m_runs = null;
            return null;
        }
        return m_bp2.isValid() ? m_bp2 : null;
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
    public int getHyperParametersCount() {
        return m_nhp;
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
    public KurtosisTest getKurtosis() {
        if (m_kurtosis == null)
	    try {
            m_kurtosis = new KurtosisTest();
            m_kurtosis.test(stats);
        } catch (StatException ex) {
            m_kurtosis = null;
            return null;
        } catch (DStatException ex) {
            m_runs = null;
            return null;
        }
        return m_kurtosis.isValid() ? m_kurtosis : null;
    }

    /**
     *
     * @return
     */
    public LjungBoxTest getLjungBox() {
        if (m_lb == null)
	    try {
            int k = m_k != 0 ? m_k : calcLBLength(m_freq);

            m_lb = new LjungBoxTest();
            m_lb.setHyperParametersCount(m_nhp);
            m_lb.setLag(1);
            m_lb.setK(k);
            m_lb.test(ac);
        } catch (StatException ex) {
            m_lb = null;
            return null;
        }
        return m_lb.isValid() ? m_lb : null;
    }

    /**
     *
     * @return
     */
    public LjungBoxTest getLjungBoxOnSquare() {
        if (m_lb2 == null)
	    try {
            int k = m_k != 0 ? m_k : calcLBLength(m_freq);

            m_lb2 = new LjungBoxTest();
            m_lb2.setHyperParametersCount(m_nhp);
            m_lb2.setLag(1);
            m_lb2.setK(k);
            m_lb2.test(ac2);
        } catch (StatException ex) {
            m_lb2 = null;
            return null;
        } catch (DStatException ex) {
            m_runs = null;
            return null;
        }
        return m_lb2.isValid() ? m_lb2 : null;
    }

    /**
     *
     * @return
     *
     */
    public MeanTest getMeanTest() {
        if (m_mean == null)
	    try {
            m_mean = new MeanTest();
            m_mean.zeroMean(stats);
        } catch (StatException ex) {
            m_mean = null;
            return null;
        } catch (DStatException ex) {
            m_runs = null;
            return null;
        }
        return m_mean.isValid() ? m_mean : null;
    }

    /**
     *
     * @return
     */
    public DoornikHansenTest getNormalityTest() {
        if (m_dh == null)
	    try {
            m_dh = new DoornikHansenTest();
            m_dh.test(stats);
        } catch (StatException ex) {
            m_dh = null;
            return null;
        } catch (DStatException ex) {
            m_runs = null;
            return null;
        }
        return m_dh.isValid() ? m_dh : null;
    }

    /**
     *
     * @return
     */
    public TestofRuns getRuns() {
        if (m_runs == null)
	    try {
            m_runs = new TestofRuns();
            m_runs.test(stats);
        } catch (StatException | DStatException ex) {
            m_runs = null;
            return null;
        }
        return m_runs.isValid() ? m_runs : null;
    }

    /**
     *
     * @return
     */
    public BoxPierceTest getSeasonalBoxPierce() {
        if (!m_seas) {
            return null;
        }
        if (m_bps == null)
	    try {
            int k = m_ks != 0 ? m_ks : 2;
            m_bps = new BoxPierceTest();
            m_bps.setHyperParametersCount(m_nhp);
            m_bps.setLag(m_freq);
            m_bps.setK(k);
            m_bps.test(ac);
        } catch (StatException ex) {
            m_bps = null;
            return null;
        } catch (DStatException ex) {
            m_runs = null;
            return null;
        }
        return m_bps.isValid() ? m_bps : null;
    }

    /**
     *
     * @return
     */
    public LjungBoxTest getSeasonalLjungBox() {
        if (!m_seas) {
            return null;
        }
        if (m_lbs == null)
	    try {
            int k = m_ks != 0 ? m_ks : 2;
            m_lbs = new LjungBoxTest();
            m_lbs.setHyperParametersCount(m_nhp);
            m_lbs.setLag(m_freq);
            m_lbs.setK(k);
            m_lbs.test(ac);
        } catch (StatException ex) {
            m_lbs = null;
            return null;
        } catch (DStatException ex) {
            m_runs = null;
            return null;
        }
        return m_lbs.isValid() ? m_lbs : null;
    }

    /**
     *
     * @return
     */
    public int getsK() {
        return m_ks;
    }

    /**
     *
     * @return
     */
    public SkewnessTest getSkewness() {
        if (m_skewness == null)
	    try {
            m_skewness = new SkewnessTest();
            m_skewness.test(stats);
        } catch (StatException ex) {
            m_skewness = null;
            return null;
        } catch (DStatException ex) {
            m_runs = null;
            return null;
        }
        return m_skewness.isValid() ? m_skewness : null;
    }

    /**
     *
     * @return
     */
    public DescriptiveStatistics getStatistics() {
        return stats;
    }

    /**
     *
     * @return
     */
    public TestofUpDownRuns getUpAndDownRuns() {
        if (m_udruns == null)
	    try {
            m_udruns = new TestofUpDownRuns();
            m_udruns.test(stats);
        } catch (StatException ex) {
            m_udruns = null;
            return null;
        } catch (DStatException ex) {
            m_runs = null;
            return null;
        }
        return m_udruns.isValid() ? m_udruns : null;
    }

    /**
     *
     * @param k
     */
    public void prepare(final int k) {
        ac.setKMax(k);
        ac2.setKMax(k);
    }

    /**
     *
     * @param value
     */
    public void setK(int value) {
        m_k = value;
    }

    /**
     *
     * @param value
     */
    public void setsK(int value) {
        m_ks = value;
    }
}
