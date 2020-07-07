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
import ec.tstoolkit.timeseries.calendars.ChainedGregorianCalendarProvider;
import ec.tstoolkit.timeseries.calendars.GregorianCalendarManager;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
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

    @ServiceProvider(CalendarAdapter.class)
    public static class Adapter extends CalendarAdapter<XmlChainedCalendar, ChainedGregorianCalendarProvider> {

        @Override
        public Class<ChainedGregorianCalendarProvider> getValueType() {
            return ChainedGregorianCalendarProvider.class;
        }

        @Override
        public Class<XmlChainedCalendar> getXmlType() {
            return XmlChainedCalendar.class;
        }

        @Override
        public ChainedGregorianCalendarProvider unmarshal(XmlChainedCalendar v){
            return new ChainedGregorianCalendarProvider(v.startCalendar, v.calendarBreak, v.endCalendar);
        }

        @Override
        public ChainedGregorianCalendarProvider unmarshal(XmlChainedCalendar v, GregorianCalendarManager mgr) {
            return new ChainedGregorianCalendarProvider(mgr, v.startCalendar, v.calendarBreak, v.endCalendar);
        }
        
        @Override
        public XmlChainedCalendar marshal(ChainedGregorianCalendarProvider v){
            XmlChainedCalendar xcal=new XmlChainedCalendar();
            xcal.startCalendar=v.first;
            xcal.endCalendar=v.second;
            xcal.calendarBreak=v.breakDay;
            return xcal;
        }
    }
    
    private static final Adapter ADAPTER = new Adapter();

    public static Adapter getAdapter() {
        return ADAPTER;
    }
    
}
