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

import ec.tstoolkit.timeseries.DayOfWeek;
import ec.tstoolkit.timeseries.Month;
import ec.tstoolkit.timeseries.calendars.FixedWeekDay;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import nbbrd.service.ServiceProvider;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FixedWeekDayType", propOrder = {
    "month",
    "dayofWeek",
    "week"
})
public class XmlFixedWeekDay
        extends XmlDay {

    @XmlElement(name = "Month", required = true)
    @XmlSchemaType(name = "NMTOKEN")
    protected Month month;
    @XmlElement(name = "DayofWeek", required = true)
    @XmlSchemaType(name = "NMTOKEN")
    protected DayOfWeek dayofWeek;
    // Currently, only numerical positions of the week are supported
    @XmlElement(name = "Week", required = true)
    protected int week;

    public Month getMonth() {
        return month;
    }

    public void setMonth(Month value) {
        this.month = value;
    }

    public DayOfWeek getDayofWeek() {
        return dayofWeek;
    }

    public void setDayofWeek(DayOfWeek value) {
        this.dayofWeek = value;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int value) {
        this.week = value;
    }

    @ServiceProvider(DayAdapter.class)
    public static class Adapter extends DayAdapter<XmlFixedWeekDay, FixedWeekDay> {

        @Override
        public Class<FixedWeekDay> getValueType() {
            return FixedWeekDay.class;
        }

        @Override
        public Class<XmlFixedWeekDay> getXmlType() {
            return XmlFixedWeekDay.class;
        }

        @Override
        public FixedWeekDay unmarshal(XmlFixedWeekDay v) throws Exception {
            FixedWeekDay o = new FixedWeekDay(v.getWeek(), v.getDayofWeek(), v.getMonth(), v.getWeight());
            return o;
        }

        @Override
        public XmlFixedWeekDay marshal(FixedWeekDay v) throws Exception {
            XmlFixedWeekDay xml = new XmlFixedWeekDay();
            xml.setDayofWeek(v.dayOfWeek);
            xml.setWeek(v.week);
            xml.setMonth(v.month);
            xml.setWeight(v.getWeight());
            return xml;
        }

    }
}
