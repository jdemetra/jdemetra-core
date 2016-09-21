/*
 * Copyright 2016 National Bank of Belgium
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
package ec.demetra.xml.calendars;

import ec.tstoolkit.timeseries.calendars.EasterRelatedDay;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.openide.util.lookup.ServiceProvider;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EasterRelatedDayType", propOrder = {
    "offset"
})
public class XmlEasterRelatedDay
        extends XmlDay {

    @XmlElement(name = "Offset")
    protected short offset;
    @XmlAttribute(name = "julian")
    protected Boolean julian;

    public short getOffset() {
        return offset;
    }

    public void setOffset(short value) {
        this.offset = value;
    }

    public boolean isJulian() {
        if (julian == null) {
            return false;
        } else {
            return julian;
        }
    }

    public void setJulian(boolean value) {
        if (value) {
            this.julian = value;
        } else {
            this.julian = null;
        }
    }

    @ServiceProvider(service = DayAdapter.class)
    public static class Adapter extends DayAdapter<XmlEasterRelatedDay, EasterRelatedDay> {

        @Override
        public Class<EasterRelatedDay> getValueType() {
            return EasterRelatedDay.class;
        }

        @Override
        public Class<XmlEasterRelatedDay> getXmlType() {
            return XmlEasterRelatedDay.class;
        }

        @Override
        public EasterRelatedDay unmarshal(XmlEasterRelatedDay v) throws Exception {
            EasterRelatedDay o = new EasterRelatedDay(v.offset, v.weight, v.julian);
            return o;
        }

        @Override
        public XmlEasterRelatedDay marshal(EasterRelatedDay v) throws Exception {
            XmlEasterRelatedDay xml = new XmlEasterRelatedDay();
            xml.offset = (short) v.offset;
            xml.julian = v.isJulian();
            xml.weight = v.getWeight();
            return xml;
        }

    }
}
