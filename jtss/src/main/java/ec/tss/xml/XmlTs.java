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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import ec.tss.TsInformation;
import ec.tss.TsInformationType;
import ec.tss.TsMoniker;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

/**
 * 
 * @author Jean Palate
 */
@XmlRootElement(name = XmlTs.RNAME)
@XmlType(name = XmlTs.NAME)
public class XmlTs implements IXmlConverter<TsInformation> {
    static final String NAME = "tsType";
    static final String RNAME = "ts";

    /**
     *
     */
    @XmlElement
    public Integer freq;
    /**
     *
     */
    @XmlElement
    public Integer firstYear;
    /**
     *
     */
    @XmlElement
    public Integer firstPeriod;

    /**
     *
     */
    @XmlElement(name = "data")
    @XmlList
    public double[] data;
    /**
     *
     */
    @XmlElement
    public XmlMetaData metaData;

    /**
     *
     */
    @XmlAttribute
    public String name;
    /**
     *
     */
    @XmlAttribute
    public String source;
    /**
     *
     */
    @XmlAttribute
    public String identifier;

    /**
     * 
     * @param t
     */
    public void copy(TsInformation t)
    {
	TsData tsdata = t.data;
	if (tsdata != null) {
	    TsPeriod start = tsdata.getStart();
	    freq = start.getFrequency().intValue();
	    firstYear = start.getYear();
	    firstPeriod = start.getPosition() + 1;
	    data = tsdata.internalStorage();
	}
	source = t.moniker.getSource();
	identifier = t.moniker.getId();
	name = t.name;
	if (t.metaData != null && !t.metaData.isEmpty()) {
	    metaData = new XmlMetaData();
	    metaData.copy(t.metaData);
	}
    }

    /**
     * 
     * @return
     */
    @Override
    public TsInformation create()
    {
        TsMoniker moniker=TsMoniker.create(source, identifier);
	TsInformation info = new TsInformation(name, moniker, data != null ?
		TsInformationType.UserDefined : TsInformationType.None);
	if (metaData != null)
	    info.metaData = metaData.create();
	if (data != null)
	    info.data = new TsData(TsFrequency.valueOf(freq), firstYear,
		    firstPeriod - 1, data, false);
 	return info;
    }
}
