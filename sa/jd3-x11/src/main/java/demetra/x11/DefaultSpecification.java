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


package demetra.x11;

import demetra.design.Development;
import demetra.maths.linearfilters.SymmetricFilter;

/**
 *
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class DefaultSpecification {

    private boolean mul = true;

    private double lsigma = 1.5, usigma = 2.5;

    private int henderson = -1;

    /**
     * 
     * @return
     */
    public int getHendersonFilterLength()
    {
	return henderson;
    }

    /**
     * 
     * @return
     */
    public double getLowerSigma()
    {
	return lsigma;
    }

    /**
     * 
     * @return
     */
    public SymmetricFilter getSeasonalFilter()
    {
	return SeasonalFilterFactory.S3X5;
    }

    /**
     * 
     * @return
     */
    public int getSeasonalFilterLength()
    {
	return 7;
    }

    /**
     * 
     * @return
     */
    public SymmetricFilter getTrendFilter()
    {
	return TrendCycleFilterFactory.makeHendersonFilter(13);
    }

    /**
     * 
     * @return
     */
    public double getUpperSigma()
    {
	return usigma;
    }

    /**
     * 
     * @return
     */
    public boolean isAutoHenderson()
    {
	return henderson < 0;
    }

    /**
     * 
     * @return
     */
    public boolean isMultiplicative()
    {
	return mul;
    }

    /**
     * 
     * @param mul
     */
    public void setMultiplicative(boolean mul)
    {
	this.mul = mul;
    }

    /**
     * 
     * @param lsig
     * @param usig
     */
    public void setSigma(double lsig, double usig)
    {
	if (usig <= lsig || lsig <= 0.5)
	    throw new X11Exception("Invalid sigma options");
	lsigma = lsig;
	usigma = usig;
    }
}
