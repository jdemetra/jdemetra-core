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

import demetra.timeseries.calendars.ChainedCalendar;
import demetra.toolkit.io.xml.legacy.XmlDateAdapter;
import java.time.LocalDate;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import nbbrd.service.ServiceProvider;


@XmlAccessorType(XmlAccessType.FIELD)
//@XmlRootElement(name="ChainedCalendar")
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
    @XmlJavaTypeAdapter(XmlDateAdapter.class)
    protected LocalDate calendarBreak;

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

    public LocalDate getCalendarBreak() {
        return calendarBreak;
    }

    public void setCalendarBreak(LocalDate value) {
        this.calendarBreak = value;
    }

    @ServiceProvider(CalendarAdapter.class)
    public static class Adapter extends CalendarAdapter<XmlChainedCalendar, ChainedCalendar> {

        @Override
        public Class<ChainedCalendar> getValueType() {
            return ChainedCalendar.class;
        }

        @Override
        public Class<XmlChainedCalendar> getXmlType() {
            return XmlChainedCalendar.class;
        }

        @Override
        public ChainedCalendar unmarshal(XmlChainedCalendar v){
            return new ChainedCalendar(v.startCalendar, v.endCalendar, v.calendarBreak);
        }

        @Override
        public XmlChainedCalendar marshal(ChainedCalendar v){
            XmlChainedCalendar xcal=new XmlChainedCalendar();
            xcal.startCalendar=v.getFirst();
            xcal.endCalendar=v.getSecond();
            xcal.calendarBreak=v.getBreakDate();
            return xcal;
        }
    }
    
    private static final Adapter ADAPTER = new Adapter();

    public static Adapter getAdapter() {
        return ADAPTER;
    }
    
}
