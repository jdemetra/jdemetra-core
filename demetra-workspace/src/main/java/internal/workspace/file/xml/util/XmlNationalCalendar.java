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
package internal.workspace.file.xml.util;

import demetra.timeseries.calendars.Calendar;
import demetra.timeseries.calendars.CalendarDefinition;
import demetra.timeseries.calendars.CalendarManager;
import demetra.timeseries.calendars.Holiday;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlRootElement(name = "nationalCalendar")
@XmlType(name = XmlNationalCalendar.NAME)
public class XmlNationalCalendar extends AbstractXmlCalendar {

    static final String NAME = "nationalCalendarType";
    @XmlElement(name = "specialDayEvent")
    public XmlSpecialDayEvent[] sd;
    @XmlAttribute
    public Boolean meancorrection;
    @XmlAttribute
    public Boolean julianCal;

    private void setMean(boolean mean) {
        if (!mean) {
            meancorrection = mean;
        } else {
            meancorrection = null;
        }
    }

    private boolean isMean() {
        return meancorrection == null ? true : meancorrection;
    }

    private void setJulian(boolean julian) {
        if (julian) {
            julianCal = julian;
        } else {
            julianCal = null;
        }
    }

    private boolean isJulian() {
        return julianCal == null ? false : julianCal;
    }

    public XmlNationalCalendar() {
    }

    public static XmlNationalCalendar create(String code, CalendarManager mgr) {
        CalendarDefinition cal = mgr.get(code);
        if (cal == null || !(cal instanceof Calendar)) {
            return null;
        }

        Calendar t = (Calendar) cal;
        XmlNationalCalendar xcal = new XmlNationalCalendar();
        xcal.name = code;

        Holiday[] holidays = t.getHolidays();
        int n = holidays.length;
        if (n > 0) {
            xcal.sd = new XmlSpecialDayEvent[n];

            int i = 0;
            for (Holiday s : holidays) {
                XmlSpecialDayEvent tmp = new XmlSpecialDayEvent();
                tmp.copy(s);
                xcal.sd[i++] = tmp;
            }
        }
        return xcal;
    }

//    @Override
//    public boolean addTo(InformationSet info) {
//        NationalCalendar nc = new NationalCalendar();
//        if (sd != null) {
//            for (int i = 0; i < sd.length; ++i) {
//                nc.add(sd[i].create());
//            }
//        }
//        NationalCalendarProvider rslt = new NationalCalendarProvider(nc);
//        if (name != null) {
//            info.set(name, rslt);
//            return true;
//        }
//        else {
//            return false;
//        }
//    }
    @Override
    public boolean addTo(CalendarManager mgr) {
        Calendar nc = null;
        if (sd != null) {
            Holiday[] holidays = new Holiday[sd.length];
            for (int i = 0; i < sd.length; ++i) {
                holidays[i] = sd[i].create();
            }
            nc = new Calendar(holidays);
        } else {
            return false;
        }
        if (name != null) {
            mgr.set(name, nc);
            return true;
        } else {
            return false;
        }
    }
}
