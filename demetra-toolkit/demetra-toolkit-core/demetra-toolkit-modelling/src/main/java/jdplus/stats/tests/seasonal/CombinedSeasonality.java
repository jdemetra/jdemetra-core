/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.stats.tests.seasonal;

import demetra.design.Development;
import jdplus.stats.tests.AnovaTest;
import jdplus.stats.tests.StatisticalTest;
import demetra.data.DoubleSeq;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public class CombinedSeasonality {
    
    /**
     * 
     */
    public static enum IdentifiableSeasonality
    {
        /**
         * No identifiable seasonality
         */
        None,
        /**
         * Probably no identifiable seasonality
         */
        ProbablyNone,
        /**
         * Identifiable seasonality present
         */
        Present
    }

    private double thfs = 0.001, thfm = 0.05, thkw = 0.001;
    private final AnovaTest stable, evolutive;
    private final KruskalWallis kruskallwallis;

    /**
     * 
     * @param series Data
     * @param period Tested periodicity
     * @param startPeriod
     * @param multiplicative True for multiplicative model
     */
    public CombinedSeasonality(DoubleSeq series, int period, int startPeriod, boolean multiplicative)
    {
	kruskallwallis = new KruskalWallis(series, period);
	stable = StableSeasonality.of(series, period);
	evolutive = EvolutiveSeasonality.of(series, period, startPeriod, multiplicative);

    }

    /**
     * 
     * @return
     */
    public AnovaTest getEvolutiveSeasonalityTest()
    {
	return evolutive;
    }

    /**
     * 
     * @return
     */
    public double getMovingSeasonalityAcceptanceLevel()
    {
	return thfm;
    }

    /**
     * 
     * @return
     */
    public double getNonParametricAcceptanceLevel()
    {
	return thkw;
    }

    /**
     * 
     * @return
     */
    public KruskalWallis getNonParametricTestForStableSeasonality()
    {
	return kruskallwallis;
    }

    /**
     * 
     * @return
     */
    public AnovaTest getStableSeasonalityTest()
    {
	return stable;
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
	return thfs;
    }

    /**
     * 
     * @return
     */
    public IdentifiableSeasonality getSummary()
    {
        StatisticalTest stest=stable.asTest(), etest=evolutive.asTest();
	double ps = stest.getPValue(), pm = etest.getPValue();
	if (ps >= thfs)
	    return IdentifiableSeasonality.None;
	boolean resfm = (pm < thfm);

	double fs = stest.getValue(), fm = etest.getValue();
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

	if (kruskallwallis.build().getPValue() >= thkw)
	    return IdentifiableSeasonality.ProbablyNone;
	return IdentifiableSeasonality.Present;
    }

    /**
     * 
     * @return
     */
    public double mvalue()
    {
	double fs = stable.asTest().getValue(), fm = evolutive.asTest().getValue();
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
	thfm = value;
    }

    /**
     * sets the acceptance level for the Kruskal-Wallis
     * @param value
     */
    public void setNonParametricAcceptanceLevel(double value)
    {
	thkw = value;
    }

    /**
     * 
     * @param value
     */
    public void setStableSeasonalityAcceptanceLevel(double value)
    {
	thfs = value;
    }
}
