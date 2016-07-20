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

package ec.demetra.xml.core;

import ec.tss.xml.IXmlConverter;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * @author Jean Palate
 */
@XmlType(name = XmlTsDomain.NAME)
public class XmlTsDomain implements IXmlConverter<TsDomain> {

    static final String NAME = "TsDomainType";
    /**
     *
     */
    @XmlElement(name="Frequency")
    public int freq;
    /**
     *
     */
    @XmlElement(name="FirstYear")
    public int firstYear;
    /**
     *
     */
    @XmlElement(name="FirstPeriod")
    public Integer firstPeriod;
    /**
     *
     */
    @XmlElement(name="Length")
    public int length;

    /**
     * 
     * @param t
     */
    @Override
    public void copy(TsDomain t)
    {
	TsPeriod start = t.getStart();
	freq = start.getFrequency().intValue();
	if (freq != 1)
	    firstPeriod = start.getPosition() + 1;
	else
	    firstPeriod = null;
	firstYear = start.getYear();
	length = t.getLength();
    }

    /**
     * 
     * @return
     */
    @Override
    public TsDomain create()
    {
	TsPeriod p = new TsPeriod(TsFrequency.valueOf(freq));
	if (firstPeriod != null)
	    p.set(firstYear, firstPeriod - 1);
	else
	    p.set(firstYear, 0);
	return new TsDomain(p, length);
    }
}
