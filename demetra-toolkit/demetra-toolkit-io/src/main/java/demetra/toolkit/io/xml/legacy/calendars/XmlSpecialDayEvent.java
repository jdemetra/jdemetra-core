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
package demetra.toolkit.io.xml.legacy.calendars;

import demetra.timeseries.ValidityPeriod;
import demetra.timeseries.calendars.Holiday;
import demetra.toolkit.io.xml.legacy.XmlDateAdapter;
import java.time.LocalDate;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpecialDayEventType", propOrder = {
    "day",
    "from",
    "to"
})
public class XmlSpecialDayEvent {

    @XmlElement(name = "Day", required = true)
    protected XmlDay day;
    @XmlElement(name = "From")
    @XmlJavaTypeAdapter(XmlDateAdapter.class)
    protected LocalDate from;
    @XmlElement(name = "To")
    @XmlJavaTypeAdapter(XmlDateAdapter.class)
    protected LocalDate to;

    public XmlDay getDay() {
        return day;
    }

    public void setDay(XmlDay value) {
        this.day = value;
    }

    public LocalDate getFrom() {
        return from;
    }

    public void setFrom(LocalDate value) {
        this.from = value;
    }

    public LocalDate getTo() {
        return to;
    }

    public void setTo(LocalDate value) {
        this.to = value;
    }

    public static class Adapter extends XmlAdapter<XmlSpecialDayEvent, Holiday> {

        @Override
        public Holiday unmarshal(XmlSpecialDayEvent v) {
            Holiday sday = HolidayAdapters.getDefault().unmarshal(v.day);
            LocalDate from = v.from, to = v.to;
            if (from != null || to != null) {
                return sday.forPeriod(from, to);
            }
            return sday;
        }

        @Override
        public XmlSpecialDayEvent marshal(Holiday v) {
            XmlSpecialDayEvent xse = new XmlSpecialDayEvent();
            xse.day = HolidayAdapters.getDefault().marshal(v);
            ValidityPeriod validityPeriod = v.getValidityPeriod();
            if (validityPeriod != null) {
                if (validityPeriod.isStartSpecified()) {
                    xse.from = validityPeriod.getStart();
                    if (validityPeriod.isEndSpecified()) {
                        xse.to = validityPeriod.getEnd();
                    }
                }
            }
            return xse;
        }
    }

    private static final Adapter ADAPTER = new Adapter();

    public static Adapter getAdapter() {
        return ADAPTER;
    }
}
