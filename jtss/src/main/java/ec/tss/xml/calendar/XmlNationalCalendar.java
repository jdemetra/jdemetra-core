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
package ec.tss.xml.calendar;

import ec.tstoolkit.timeseries.calendars.GregorianCalendarManager;
import ec.tstoolkit.timeseries.calendars.IGregorianCalendarProvider;
import ec.tstoolkit.timeseries.calendars.NationalCalendar;
import ec.tstoolkit.timeseries.calendars.NationalCalendarProvider;
import ec.tstoolkit.timeseries.calendars.SpecialDayEvent;
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

    public static XmlNationalCalendar create(String code, GregorianCalendarManager mgr) {
        IGregorianCalendarProvider cal = mgr.get(code);
        if (cal == null || !(cal instanceof NationalCalendarProvider)) {
            return null;
        }

        NationalCalendarProvider t = (NationalCalendarProvider) cal;
        XmlNationalCalendar xcal = new XmlNationalCalendar();
        xcal.name = code;
        xcal.setMean(t.isLongTermMeanCorrection());
        xcal.setJulian(t.isJulianEaster());

        int n = t.events().size();
        if (n > 0) {
            xcal.sd = new XmlSpecialDayEvent[n];

            int i = 0;
            for (SpecialDayEvent s : t.events()) {
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
    public boolean addTo(GregorianCalendarManager mgr) {
        NationalCalendar nc = new NationalCalendar(isMean(), isJulian());
        if (sd != null) {
            for (int i = 0; i < sd.length; ++i) {
                nc.add(sd[i].create());
            }
        }
        NationalCalendarProvider rslt = new NationalCalendarProvider(nc);
        if (name != null) {
            mgr.set(name, rslt);
            return true;
        } else {
            return false;
        }
    }
}
