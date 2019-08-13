//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.11 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2016.09.19 à 12:39:06 PM CEST 
//
package ec.demetra.xml.calendars;

import ec.tstoolkit.timeseries.calendars.GregorianCalendarManager;
import ec.tstoolkit.timeseries.calendars.NationalCalendar;
import ec.tstoolkit.timeseries.calendars.NationalCalendarProvider;
import ec.tstoolkit.timeseries.calendars.SpecialDayEvent;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
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
    public static class Adapter extends CalendarAdapter<XmlNationalCalendar, NationalCalendarProvider> {

        @Override
        public Class<NationalCalendarProvider> getValueType() {
            return NationalCalendarProvider.class;
        }

        @Override
        public Class<XmlNationalCalendar> getXmlType() {
            return XmlNationalCalendar.class;
        }

        @Override
        public NationalCalendarProvider unmarshal(XmlNationalCalendar v) {
            NationalCalendar nc = new NationalCalendar();
            for (XmlSpecialDayEvent s : v.getSpecialDayEvent()) {
                nc.add(XmlSpecialDayEvent.getAdapter().unmarshal(s));
            }
            return new NationalCalendarProvider(nc);
        }

        @Override
        public NationalCalendarProvider unmarshal(XmlNationalCalendar v, GregorianCalendarManager mgr) {
           return unmarshal(v);
        }
        

        @Override
        public XmlNationalCalendar marshal(NationalCalendarProvider v) {
            XmlNationalCalendar xcal = new XmlNationalCalendar();

            for (SpecialDayEvent s : v.events()) {
                XmlSpecialDayEvent xday = XmlSpecialDayEvent.getAdapter().marshal(s);
                xcal.getSpecialDayEvent().add(xday);
            }
            return xcal;
        }
    }
    
    private static final Adapter ADAPTER = new Adapter();

    public static Adapter getAdapter() {
        return ADAPTER;
    }
}
