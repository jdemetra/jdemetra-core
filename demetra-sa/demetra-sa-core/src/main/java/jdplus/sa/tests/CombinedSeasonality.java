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
package jdplus.sa.tests;

import nbbrd.design.Development;
import demetra.data.DoubleSeq;
import demetra.sa.diagnostics.CombinedSeasonalityTest;
import demetra.stats.OneWayAnova;
import demetra.stats.StatisticalTest;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import jdplus.stats.tests.TestsUtility;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public class CombinedSeasonality {
    

    private double thfs = 0.001, thfm = 0.05, thkw = 0.001;
    private final OneWayAnova stable, evolutive;
    private final KruskalWallis kruskallwallis;
    
    public static CombinedSeasonality of(TsData s, double xbar){
        TsPeriod start = s.getStart();
        return new CombinedSeasonality(s.getValues(), start.annualFrequency(), start.annualPosition(), xbar);
    }

    /**
     * 
     * @param series Data
     * @param period Tested periodicity
     * @param startPeriod
     * @param xbar Average of the series (usually 0 or 1)
     */
    public CombinedSeasonality(DoubleSeq series, int period, int startPeriod, double xbar)
    {
	kruskallwallis = new KruskalWallis(series, period);
	stable = StableSeasonality.of(series, period);
	evolutive = EvolutiveSeasonality.of(series, period, startPeriod, xbar);

    }

    /**
     * 
     * @return
     */
    public OneWayAnova getEvolutiveSeasonalityTest()
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
    public OneWayAnova getStableSeasonalityTest()
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
    public CombinedSeasonalityTest.IdentifiableSeasonality getSummary()
    {
        StatisticalTest stest=TestsUtility.ofAnova(stable), etest=TestsUtility.ofAnova(evolutive);
	double ps = stest.getPvalue(), pm = etest.getPvalue();
	if (ps >= thfs)
	    return CombinedSeasonalityTest.IdentifiableSeasonality.None;
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
		return CombinedSeasonalityTest.IdentifiableSeasonality.None;
	}

	if (T1 >= 1.0 || T2 >= 1.0)
	    return CombinedSeasonalityTest.IdentifiableSeasonality.ProbablyNone;

	if (kruskallwallis.build().getPvalue() >= thkw)
	    return CombinedSeasonalityTest.IdentifiableSeasonality.ProbablyNone;
	return CombinedSeasonalityTest.IdentifiableSeasonality.Present;
    }

    /**
     * 
     * @return
     */
    public double mvalue()
    {
	double fs = stable.ftest(), fm = evolutive.ftest();
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
