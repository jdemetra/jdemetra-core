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

import ec.tss.xml.XmlDayAdapter;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.ValidityPeriod;
import ec.tstoolkit.timeseries.calendars.ISpecialDay;
import ec.tstoolkit.timeseries.calendars.SpecialDayEvent;
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
    @XmlJavaTypeAdapter(XmlDayAdapter.class)
    protected Day from;
    @XmlElement(name = "To")
    @XmlJavaTypeAdapter(XmlDayAdapter.class)
    protected Day to;

    public XmlDay getDay() {
        return day;
    }

    public void setDay(XmlDay value) {
        this.day = value;
    }

    public Day getFrom() {
        return from;
    }

    public void setFrom(Day value) {
        this.from = value;
    }

    public Day getTo() {
        return to;
    }

    public void setTo(Day value) {
        this.to = value;
    }

    public static class Adapter extends XmlAdapter<XmlSpecialDayEvent, SpecialDayEvent> {

        @Override
        public SpecialDayEvent unmarshal(XmlSpecialDayEvent v) throws Exception {
            ISpecialDay sday = DayAdapters.getDefault().unmarshal(v.day);
            SpecialDayEvent sev = new SpecialDayEvent(sday);
            Day from = v.from, to = v.to;
            if (from != null || to != null) {
                sev.setValidityPeriod(new ValidityPeriod(from, to));
            }
            return sev;
        }

        @Override
        public XmlSpecialDayEvent marshal(SpecialDayEvent v) throws Exception {
            XmlSpecialDayEvent xse = new XmlSpecialDayEvent();
            xse.day = DayAdapters.getDefault().marshal(v.day);
            if (v.getValidityPeriod() != null && v.getValidityPeriod().isStartSpecified()) {
                xse.from = v.getStart();
            }
            if (v.getValidityPeriod() != null && v.getValidityPeriod().isEndSpecified()) {
                xse.to = v.getEnd();
            }
            return xse;
        }

    }

    private static final Adapter ADAPTER = new Adapter();

    public static Adapter getAdapter() {
        return ADAPTER;
    }
}
