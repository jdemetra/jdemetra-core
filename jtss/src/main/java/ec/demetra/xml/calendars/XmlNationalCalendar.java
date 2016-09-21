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
import org.openide.util.lookup.ServiceProvider;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "NationalCalendar")
@XmlType(name = "NationalCalendarType", propOrder = {
    "specialDayEvent"
})
public class XmlNationalCalendar
        extends XmlCalendar {

    @XmlAttribute(name = "meanCorrection")
    protected Boolean meanCorrection;

    @XmlAttribute(name = "julian")
    protected Boolean julian;

    @XmlElement(name = "SpecialDayEvent", required = true)
    protected List<XmlSpecialDayEvent> specialDayEvent;

    public void setMeanCorrection(boolean val) {
        if (val) {
            meanCorrection = null;
        } else {
            meanCorrection = val;
        }
    }

    public void setJulian(boolean j) {
        if (j) {
            julian = j;
        } else {
            julian = null;
        }
    }

    public boolean isMeanCorrection() {
        return meanCorrection == null ? true : meanCorrection;
    }

    public boolean isJulian() {
        if (julian == null) {
            return false;
        } else {
            return julian;
        }
    }

    public List<XmlSpecialDayEvent> getSpecialDayEvent() {
        if (specialDayEvent == null) {
            specialDayEvent = new ArrayList<>();
        }
        return this.specialDayEvent;
    }

    @ServiceProvider(service = CalendarAdapter.class)
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
        public NationalCalendarProvider unmarshal(XmlNationalCalendar v) throws Exception {
            NationalCalendar nc = new NationalCalendar(v.isMeanCorrection(), v.isJulian());
            for (XmlSpecialDayEvent s : v.getSpecialDayEvent()) {
                nc.add(XmlSpecialDayEvent.getAdapter().unmarshal(s));
            }
            return new NationalCalendarProvider(nc);
        }

        @Override
        public XmlNationalCalendar marshal(NationalCalendarProvider v) throws Exception {
            XmlNationalCalendar xcal = new XmlNationalCalendar();
            xcal.setMeanCorrection(v.isLongTermMeanCorrection());
            xcal.setJulian(v.isJulianEaster());

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
