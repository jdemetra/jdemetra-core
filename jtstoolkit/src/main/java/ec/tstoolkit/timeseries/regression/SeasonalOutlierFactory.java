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
public class SeasonalOutlierFactory implements IOutlierFactory {
    /**
     *
     */
    public static final int DEF_MINREPEAT = 1;

    private boolean zeroEnded;

    private int repeatCount = DEF_MINREPEAT;

    /**
     *
     * @param position
     * @return
     */
    @Override
    public SeasonalOutlier create(Day position) {
	SeasonalOutlier so = new SeasonalOutlier(position);
	so.zeroEnded = zeroEnded;
	return so;
    }

    /**
     *
     * @param tsdomain
     * @return
     */
    @Override
    public TsDomain definitionDomain(TsDomain tsdomain) {
	int freq = tsdomain.getFrequency().intValue();
	if (freq <= 1)
	    return null;
	// exclude seasonal dummy and AO
	return tsdomain.drop(repeatCount * freq, repeatCount * freq);
    }

    /**
     *
     * @return
     */
    @Override
    public OutlierType getOutlierType() {
	return OutlierType.SO;
    }

    /**
     * 
     * @return
     */
    public int getRepeatCount()
    {
	return repeatCount;
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
    public void setRepeatCount(int value)
    {
	repeatCount = value;
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
