//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.11 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2016.09.19 à 12:39:06 PM CEST 
//
package demetra.toolkit.io.xml.legacy.calendars;

import demetra.timeseries.calendars.Calendar;
import demetra.timeseries.calendars.CalendarManager;
import demetra.timeseries.calendars.Holiday;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import nbbrd.service.ServiceProvider;

@XmlAccessorType(XmlAccessType.FIELD)
//@XmlRootElement(name = "NationalCalendar")
@XmlType(name = "NationalCalendarType", propOrder = {
    "specialDayEvent"
})
public class XmlNationalCalendar
        extends XmlCalendar {

    @XmlElement(name = "SpecialDayEvent", required = true)
    protected List<XmlSpecialDayEvent> specialDayEvent;

    public List<XmlSpecialDayEvent> getSpecialDayEvent() {
        if (specialDayEvent == null) {
            specialDayEvent = new ArrayList<>();
        }
        return this.specialDayEvent;
    }

    @ServiceProvider(CalendarAdapter.class)
    public static class Adapter extends CalendarAdapter<XmlNationalCalendar, Calendar> {

        @Override
        public Class<Calendar> getValueType() {
            return Calendar.class;
        }

        @Override
        public Class<XmlNationalCalendar> getXmlType() {
            return XmlNationalCalendar.class;
        }

        @Override
        public Calendar unmarshal(XmlNationalCalendar v) {
            if (v.specialDayEvent == null) {
                return Calendar.DEFAULT;
            }
            Holiday[] hol = new Holiday[v.specialDayEvent.size()];
            for (int i = 0; i < hol.length; ++i) {
                hol[i] = XmlSpecialDayEvent.getAdapter().unmarshal(v.specialDayEvent.get(i));
            }
            return new Calendar(hol);
        }

        @Override
        public XmlNationalCalendar marshal(Calendar v) {
            XmlNationalCalendar xcal = new XmlNationalCalendar();
            Holiday[] hol = v.getHolidays();
            if (hol.length == 0) {
                return xcal;
            }
            List<XmlSpecialDayEvent> list = xcal.getSpecialDayEvent();
            for (int i = 0; i < hol.length; ++i) {
                list.add(XmlSpecialDayEvent.getAdapter().marshal(hol[i]));
            }
            return xcal;
        }
    }

    private static final Adapter ADAPTER = new Adapter();

    public static Adapter getAdapter() {
        return ADAPTER;
    }
}
