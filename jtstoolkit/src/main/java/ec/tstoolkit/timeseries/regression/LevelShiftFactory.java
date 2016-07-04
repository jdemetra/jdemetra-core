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

package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class LevelShiftFactory implements IOutlierFactory {

    private int minLength = DEF_MINLENGTH;

    private boolean zeroEnded=true;

    /**
     *
     */
    public static final int DEF_MINLENGTH = 1;

    /**
     *
     * @param position
     * @return
     */
    @Override
    public LevelShift create(Day position) {
	LevelShift ls = new LevelShift(position);
	ls.zeroEnded = zeroEnded;
	return ls;
    }

    /**
     *
     * @param tsdomain
     * @return
     */
    @Override
    public TsDomain definitionDomain(TsDomain tsdomain) {
	return tsdomain.drop(minLength, minLength ); // ?
    }

    /**
     * 
     * @return
     */
    public int getMinLength()
    {
	return minLength;
    }

    /**
     *
     * @return
     */
    @Override
    public OutlierType getOutlierType() {
	return OutlierType.LS;
    }

    /**
     * 
     * @return
     */
    public boolean isZeroEnded()
    {
	return zeroEnded;
    }

    /**
     * 
     * @param value
     */
    public void setMinLength(int value)
    {
	minLength = value;
    }

    /**
     * 
     * @param value
     */
    public void setZeroEnded(boolean value)
    {
	zeroEnded = value;
    }
}
