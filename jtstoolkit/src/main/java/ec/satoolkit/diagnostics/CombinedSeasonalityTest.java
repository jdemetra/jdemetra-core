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

package ec.satoolkit.diagnostics;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class CombinedSeasonalityTest {

    /**
     * 
     */
    public static enum IdentifiableSeasonality
    {
	// / <summary>
	// / No identifiable seasonality
	// / </summary>

        /**
         *
         */
        None,
	// / <summary>
	// / Probably no identifiable seasonality
	// / </summary>
        /**
         *
         */
        ProbablyNone,
	// / <summary>
	// / Identifiable seasonality present
	// / </summary>
        /**
         *
         */
        Present
    }

    private double m_thfs = 0.001;

    private double m_thfm = 0.05;

    private double m_thkw = 0.001;

    private final SeasonalityTest m_stable, m_evolutive;

    private final KruskalWallisTest m_kwtest;

    /**
     * 
     * @param ts
     * @param mul
     */
    public CombinedSeasonalityTest(TsData ts, boolean mul)
    {
	m_kwtest = new KruskalWallisTest(ts);
	m_stable = SeasonalityTest.stableSeasonality(ts);
	m_evolutive = SeasonalityTest.evolutiveSeasonality(ts, mul);

    }

    /**
     * 
     * @return
     */
    public SeasonalityTest getEvolutiveSeasonality()
    {
	return m_evolutive;
    }

    /**
     * 
     * @return
     */
    public double getMovingSeasonalityAcceptanceLevel()
    {
	return m_thfm;
    }

    /**
     * 
     * @return
     */
    public double getNonParametricAcceptanceLevel()
    {
	return m_thkw;
    }

    /**
     * 
     * @return
     */
    public KruskalWallisTest getNonParametricTestForStableSeasonality()
    {
	return m_kwtest;
    }

    /**
     * 
     * @return
     */
    public SeasonalityTest getStableSeasonality()
    {
	return m_stable;
    }

    // / <summary>
    // / The property sets/gets the acceptance level for the stable seasonality
    // F-value
    // / </summary>
    /**
     * 
     * @return
     */
    public double getStableSeasonalityAcceptanceLevel()
    {
	return m_thfs;
    }

    /**
     * 
     * @return
     */
    public IdentifiableSeasonality getSummary()
    {
	double ps = m_stable.getPValue(), pm = m_evolutive.getPValue();
	if (ps >= m_thfs)
	    return IdentifiableSeasonality.None;
	boolean resfm = (pm < m_thfm);

	double fs = m_stable.getValue(), fm = m_evolutive.getValue();
	double T1 = 7.0 / fs;
	double T2 = fs == 0 ? 9 : 3.0 * fm / fs;
	if (T1 > 9)
	    T1 = 9;
	if (T2 > 9)
	    T2 = 9;

	if (resfm) {
	    double T = Math.sqrt((T1 + T2) / 2.0);
	    if (T >= 1.0)
		return IdentifiableSeasonality.None;
	}

	if (T1 >= 1.0 || T2 >= 1.0)
	    return IdentifiableSeasonality.ProbablyNone;

	if (m_kwtest.getPValue() >= m_thkw)
	    return IdentifiableSeasonality.ProbablyNone;
	return IdentifiableSeasonality.Present;
    }

    /**
     * 
     * @return
     */
    public double mvalue()
    {
	double fs = m_stable.getValue(), fm = m_evolutive.getValue();
	double T1 = 7.0 / fs;

	double T2 = fs == 0 ? 9 : 3.0 * fm / fs;
	if (T1 > 9)
	    T1 = 9;
	if (T2 > 9)
	    T2 = 9;
	return Math.sqrt(.5 * (T1 + T2));
    }

    /**
     * 
     * @param value
     */
    public void setMovingSeasonalityAcceptanceLevel(double value)
    {
	m_thfm = value;
    }

    // / <summary>
    // / The property sets/gets the acceptance level for the Kruskal-Wallis
    // seasonality test F-value
    // / </summary>
    /**
     * 
     * @param value
     */
    public void setNonParametricAcceptanceLevel(double value)
    {
	m_thkw = value;
    }

    /**
     * 
     * @param value
     */
    public void setStableSeasonalityAcceptanceLevel(double value)
    {
	m_thfs = value;
    }
    // / <summary>
    // / The property sets/gets the acceptance level for the moving seasonality
    // F-value
    // / </summary>
}
