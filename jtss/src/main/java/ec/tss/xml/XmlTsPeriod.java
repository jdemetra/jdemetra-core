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

package ec.tss.xml;

import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * @author pcuser
 */
@XmlType(name = XmlTsPeriod.NAME)
public class XmlTsPeriod implements IXmlConverter<TsPeriod> {

    static final String NAME = "tsPeriodType";
    /**
     *
     */
    @XmlElement(name = "freq")
    public int freq;
    /**
     *
     */
    @XmlElement(name = "year")
    public int year;
    /**
     *
     */
    @XmlElement(name = "period")
    public Integer period;

    /**
     * 
     * @param t
     */
    public void copy(TsPeriod t)
    {
	freq = t.getFrequency().intValue();
	if (freq != 1)
	    period = t.getPosition() + 1;
	else
	    period = null;
	year = t.getYear();
    }

    /**
     * 
     * @return
     */
    public TsPeriod create()
    {
	TsPeriod p = new TsPeriod(TsFrequency.valueOf(freq));
	if (period != null)
	    p.set(year, period - 1);
	else
	    p.set(year, 0);
	return p;

    }
}
