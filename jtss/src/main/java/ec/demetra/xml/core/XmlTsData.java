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
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Jean Palate
 */
@XmlRootElement(name = XmlTsData.RNAME)
@XmlType(name = XmlTsData.NAME)
public class XmlTsData implements IXmlConverter<TsData> {

    static final String RNAME = "TsData", NAME = RNAME + "Type";
    /**
     *
     */
    @XmlElement(name = "Frequency")
    public int freq;
    /**
     *
     */
    @XmlElement(name = "FirstYear")
    public int firstYear;
    /**
     *
     */
    @XmlElement(name = "FirstPeriod")
    public Integer firstPeriod;
    /**
     *
     */
    @XmlElement(name = "Values")
    @XmlList
    public double[] data;

    @XmlAttribute(name = "name")
    public String name;

    /**
     *
     * @param t
     */
    @Override
    public void copy(TsData t) {
        TsPeriod start = t.getStart();
        freq = start.getFrequency().intValue();
        firstYear = start.getYear();
        if (freq != 1) {
            firstPeriod = start.getPosition() + 1;
        } else {
            firstPeriod = null;
        }
        data = t.internalStorage();
    }

    /**
     *
     * @return
     */
    @Override
    public TsData create() {
        int firstperiod = firstPeriod != null ? firstPeriod - 1 : 0;

        return new TsData(TsFrequency.valueOf(freq), firstYear, firstperiod,
                data, false);
    }
}
