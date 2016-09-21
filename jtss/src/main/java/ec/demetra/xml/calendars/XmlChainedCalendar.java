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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="ChainedCalendar")
@XmlType(name = "ChainedCalendarType", propOrder = {
    "startCalendar",
    "endCalendar",
    "calendarBreak"
})
public class XmlChainedCalendar
    extends XmlCalendar
{

    @XmlElement(name = "StartCalendar", required = true)
    protected String startCalendar;
    @XmlElement(name = "EndCalendar", required = true)
    protected String endCalendar;
    @XmlElement(name = "CalendarBreak", required = true)
    @XmlJavaTypeAdapter(XmlDayAdapter.class)
    protected Day calendarBreak;

    public String getStartCalendar() {
        return startCalendar;
    }

    public void setStartCalendar(String value) {
        this.startCalendar = value;
    }

    public String getEndCalendar() {
        return endCalendar;
    }

    public void setEndCalendar(String value) {
        this.endCalendar = value;
    }

    public Day getCalendarBreak() {
        return calendarBreak;
    }

    public void setCalendarBreak(Day value) {
        this.calendarBreak = value;
    }

}
