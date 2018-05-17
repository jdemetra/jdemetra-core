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


package demetra.xml;

import demetra.datatypes.Ts;
import demetra.datatypes.TsInformationType;
import demetra.datatypes.TsMoniker;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.util.Collections;

/**
 * 
 * @author Jean Palate
 */
@XmlRootElement(name = XmlTs.RNAME)
@XmlType(name = XmlTs.NAME)
public class XmlTs implements IXmlConverter<Ts> {
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
    @Override
    public void copy(Ts t)
    {
	TsData tsdata = t.getData();
	if (tsdata != null) {
	    TsPeriod start = tsdata.getStart();
	    freq = start.getFrequency().intValue();
	    firstYear = start.getYear();
	    firstPeriod = start.getPosition() + 1;
	    data = tsdata.internalStorage();
	}
	source = t.getMoniker().getSource();
	identifier = t.getMoniker().getId();
	name = t.getName();
	if ( !t.getMetaData().isEmpty()) {
	    metaData = new XmlMetaData();
	    metaData.copy(t.getMetaData());
	}
    }

    /**
     * 
     * @return
     */
    @Override
    public Ts create()
    {
        TsMoniker moniker=TsMoniker.create(source, identifier);
	return Ts.builder()
                .moniker(moniker)
                .name(name)
                .metaData(metaData == null ? Collections.EMPTY_MAP : metaData.create())
                .type(data != null ? TsInformationType.UserDefined : TsInformationType.None)
                .data(data != null ? new TsData(TsFrequency.valueOf(freq), firstYear,
		    firstPeriod - 1, data, false) : null)
                .build();
     }
}
