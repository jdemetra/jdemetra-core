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

package ec.tss.tsproviders.common.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

/**
 * 
 * @author Jean Palate
 */
@XmlRootElement(name = "tsdata")
@XmlType(name = "tsdata")
class wsTsData {

    @XmlAttribute(name = "pstart")
    public int firstperiod;
    @XmlAttribute(name = "ystart")
    public int firstyear;
    @XmlAttribute(name = "freq")
    public int frequency;
    @XmlElement(name = "data")
    @XmlList
    public double[] data;

    void copy(TsData t) {
	TsPeriod start = t.getStart();
	frequency = start.getFrequency().intValue();
	firstyear = start.getYear();
	firstperiod = start.getPosition() + 1;
	data = t.internalStorage();
    }

    TsData create() {
	return new TsData(TsFrequency.valueOf(frequency), firstyear,
		firstperiod - 1, data, false);
    }
}
