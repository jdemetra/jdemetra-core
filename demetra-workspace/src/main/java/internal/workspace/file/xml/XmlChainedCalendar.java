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

package internal.workspace.file.xml;

import demetra.timeseries.calendars.CalendarDefinition;
import demetra.timeseries.calendars.CalendarManager;
import demetra.timeseries.calendars.ChainedCalendar;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlRootElement(name = "chainedCalendar")
@XmlType(name = XmlChainedCalendar.NAME)
public class XmlChainedCalendar extends AbstractXmlCalendar {

    static final String NAME = "chainedCalendarType";
    @XmlElement
    public String startCalendar;
    @XmlElement
    public String endCalendar;
    @XmlElement
    public String calendarBreak;

    public XmlChainedCalendar() {
    }

    public static XmlChainedCalendar create(String code, CalendarManager mgr) {
        CalendarDefinition cal = mgr.get(code);
        if (cal == null || !(cal instanceof ChainedCalendar)) {
            return null;
        }
        ChainedCalendar t = (ChainedCalendar) cal;
        XmlChainedCalendar xcal = new XmlChainedCalendar();
        xcal.startCalendar = t.getFirst();
        xcal.endCalendar = t.getSecond();
        xcal.calendarBreak = t.getBreakDate().format(DateTimeFormatter.ISO_DATE);
        xcal.name = code;
        return xcal;
    }

//    @Override
//    public boolean addTo(InformationSet info) {
//        IGregorianCalendarProvider first = info.get(startCalendar, IGregorianCalendarProvider.class);
//        IGregorianCalendarProvider last = info.get(endCalendar, IGregorianCalendarProvider.class);
//        if (first == null || last == null) {
//            return false;
//        }
//        ChainedGregorianCalendarProvider ccp = new ChainedGregorianCalendarProvider(startCalendar, StringFormatter.convertDay(calendarBreak), endCalendar);
//        if (name != null) {
//            info.set(name, ccp);
//            return true;
//        }
//        else {
//            return false;
//        }
//    }

    @Override
    public boolean addTo(CalendarManager mgr) {
        ChainedCalendar ccp = new ChainedCalendar(startCalendar, endCalendar, LocalDate.parse(calendarBreak, DateTimeFormatter.ISO_DATE));
        if (name != null) {
            mgr.set(name, ccp);
            return true;
        }
        else {
            return false;
        }
    }
}
