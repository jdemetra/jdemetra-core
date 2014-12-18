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

import ec.satoolkit.SaException;
import ec.tstoolkit.design.Development;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class ResidualSeasonalityDiagnostics {

    /**
     * 
     */
    public static class Configuration implements Cloneable
    {
        /**
         *
         */
        /**
         *
         */
        /**
         *
         */
        /**
         *
         */
        /**
         *
         */
        /**
         *
         */
        /**
         *
         */
        /**
         *
         */
        /**
         *
         */
        public static final double SASEV = 0.01, SABAD = 0.05, SAUNC = 0.1,
		ISEV = 0.01, IBAD = 0.05, IUNC = 0.1, SA3SEV = 0.01,
		SA3BAD = 0.05, SA3UNC = 0.1;

	private static void check(double severe, double bad, double unc) {
	    if (severe > bad || bad > unc || unc > 1 || severe <= 0)
		throw new SaException("SA", "Invalid settings");
	}

	private double m_SASevere = SASEV;
	private double m_SABad = SABAD;
	private double m_SAUncertain = SAUNC;
	private double m_IrrSevere = ISEV;
	private double m_IrrBad = IBAD;
	private double m_IrrUncertain = IUNC;
	private double m_SA3Severe = SA3SEV;
	private double m_SA3Bad = SA3BAD;
	private double m_SA3Uncertain = SA3UNC;

	private boolean m_Enabled = true;

        /**
         * 
         */
        public Configuration()
        {
	}

	@Override
	public Configuration clone() {
	    try {
		return (Configuration) super.clone();
	    } catch (CloneNotSupportedException ex) {
		throw new AssertionError();
	    }
	}

        /**
         * 
         * @return
         */
        public double getIrrBad()
        {
	    return m_IrrBad;
	}

        /**
         * 
         * @return
         */
        public double getIrrSevere()
        {
	    return m_IrrSevere;
	}

        /**
         * 
         * @return
         */
        public double getIrrUncertain()
        {
	    return m_IrrUncertain;
	}

        /**
         * 
         * @return
         */
        public double getSA3Bad()
        {
	    return m_SA3Bad;
	}

        /**
         * 
         * @return
         */
        public double getSA3Severe()
        {
	    return m_SA3Severe;
	}

        /**
         * 
         * @return
         */
        public double getSA3Uncertain()
        {
	    return m_SA3Uncertain;
	}

        /**
         * 
         * @return
         */
        public double getSABad()
        {
	    return m_SABad;
	}

        /**
         * 
         * @return
         */
        public double getSASevere()
        {
	    return m_SASevere;
	}

        /**
         * 
         * @return
         */
        public double getSAUncertain()
        {
	    return m_SAUncertain;
	}

        /**
         * 
         * @return
         */
        public boolean idEnabled()
        {
	    return m_Enabled;
	}

        /**
         * 
         * @param value
         */
        public void seEnabled(boolean value)
        {
	    m_Enabled = value;
	}

        /**
         * 
         * @param severe
         * @param bad
         * @param uncertain
         */
        public void setIrrthresholds(double severe, double bad, double uncertain)
        {
	    check(severe, bad, uncertain);
	    m_IrrSevere = severe;
	    m_IrrBad = bad;
	    m_IrrUncertain = uncertain;
	}

        /**
         * 
         * @param severe
         * @param bad
         * @param uncertain
         */
        public void setSA3thresholds(double severe, double bad, double uncertain)
        {
	    check(severe, bad, uncertain);
	    m_SA3Severe = severe;
	    m_SA3Bad = bad;
	    m_SA3Uncertain = uncertain;
	}

        /**
         * 
         * @param severe
         * @param bad
         * @param uncertain
         */
        public void setSAthresholds(double severe, double bad, double uncertain)
        {
	    check(severe, bad, uncertain);
	    m_SASevere = severe;
	    m_SABad = bad;
	    m_SAUncertain = uncertain;
	}
    }

    /**
     *
     */
    public static final Configuration defaultConfiguration = new Configuration();
}
