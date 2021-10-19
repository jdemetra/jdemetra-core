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
package demetra.toolkit.io.xml.information;

import demetra.timeseries.Ts;
import demetra.timeseries.TsData;
import demetra.timeseries.TsInformationType;
import demetra.timeseries.TsMoniker;
import demetra.timeseries.TsPeriod;
import demetra.toolkit.io.xml.legacy.IXmlConverter;
import java.util.Map;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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
    public void copy(Ts t) {
        TsData tsdata = t.getData();
        TsPeriod start = tsdata.getStart();
        freq = start.getUnit().getAnnualFrequency();
        firstYear = start.year();
        firstPeriod = start.annualPosition() + 1;
        data = tsdata.getValues().toArray();
        source = t.getMoniker().getSource();
        identifier = t.getMoniker().getId();
        name = t.getName();
        Map<String, String> meta = t.getMeta();
        if (meta != null && !meta.isEmpty()) {
            metaData = new XmlMetaData();
            metaData.copy(meta);
        }
    }

    /**
     *
     * @return
     */
    @Override
    public Ts create() {
        TsMoniker moniker = (source == null && identifier == null)? TsMoniker.of() : TsMoniker.of(source, identifier);
        Ts.Builder info = Ts.builder()
                .name(name)
                .moniker(moniker)
                .type(data != null
                        ? TsInformationType.UserDefined : TsInformationType.None);
        if (metaData != null) {
            info.meta(metaData.create());
        }
        if (data != null) {
            info.data(XmlTsData.of(freq, firstYear, firstPeriod, data));
        }
        return info.build();
    }
}
