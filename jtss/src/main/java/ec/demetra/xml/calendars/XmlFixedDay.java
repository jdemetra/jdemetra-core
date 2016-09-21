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

import ec.tstoolkit.timeseries.Month;
import ec.tstoolkit.timeseries.calendars.FixedDay;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import org.openide.util.lookup.ServiceProvider;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FixedDayType", propOrder = {
    "month",
    "day"
})
public class XmlFixedDay
    extends XmlDay
{
    @XmlElement(name = "Month", required = true)
    @XmlSchemaType(name = "NMTOKEN")
    protected Month month;
    @XmlElement(name = "Day")
    protected int day;

    public Month getMonth() {
        return month;
    }

    public void setMonth(Month value) {
        this.month = value;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int value) {
        this.day = value;
    }

    @ServiceProvider(service = DayAdapter.class)
    public static class Adapter extends DayAdapter<XmlFixedDay, FixedDay> {

        @Override
        public Class<FixedDay> getValueType() {
            return FixedDay.class;
        }

        @Override
        public Class<XmlFixedDay> getXmlType() {
            return XmlFixedDay.class;
        }

         @Override
        public FixedDay unmarshal(XmlFixedDay v) throws Exception {
            FixedDay o = new FixedDay(v.day-1, v.month, v.weight);
            return o;
        }

        @Override
        public XmlFixedDay marshal(FixedDay v) throws Exception {
            XmlFixedDay xml = new XmlFixedDay();
            xml.day=v.day+1;
            xml.month=v.month;
            xml.weight=v.getWeight();
            return xml;
        }

    }
}
