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
package jdplus.ssf.univariate;

import nbbrd.design.Development;
import demetra.data.DoubleSeq;


/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class ExtendedSsfData implements ISsfData {

    private final int nbcasts;
    private final int nfcasts;
    private final ISsfData data;

     /**
     * 
     * @param data
     * @param bcasts
     * @param fcasts
     */
    public ExtendedSsfData(final ISsfData data, final int bcasts, final int fcasts)
    {
	this.data = data;
        nfcasts=fcasts;
        nbcasts=bcasts;
    }

    /**
     * 
     * @param n
     * @return
     */
    @Override
    public double get(final int n)
    {
	if (n < nbcasts)
	    return Double.NaN;
	else
	    return data.get(n - nbcasts);
    }

    /**
     * 
     * @return
     */
    public int getBackcastsCount()
    {
	return nbcasts;
    }

    /**
     * 
     * @return
     */
    @Override
    public int length()
    {
	return nbcasts + nfcasts + data.length();
    }

    /**
     * 
     * @return
     */
    public int getForecastsCount()
    {
	return nfcasts;
    }

    /**
     * 
     * @param pos
     * @return
     */
    @Override
    public boolean isMissing(final int pos)
    {
	if (pos < nbcasts)
	    return true;
	return data.isMissing(pos - nbcasts);
    }


}
