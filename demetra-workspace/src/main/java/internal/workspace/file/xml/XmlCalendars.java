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

import demetra.timeseries.calendars.CalendarManager;
import demetra.toolkit.io.xml.legacy.IXmlConverter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlRootElement(name = XmlCalendars.RNAME)
@XmlType(name = XmlCalendars.NAME)
public class XmlCalendars implements IXmlConverter<CalendarManager> {

    static final String NAME = "calendarsType";
    static final String RNAME = "calendars";
    
    @XmlElements(value = {
        @XmlElement(name = "nationalCalendar", type = XmlNationalCalendar.class),
        @XmlElement(name = "compositeCalendar", type = XmlCompositeCalendar.class),
        @XmlElement(name = "chainedCalendar", type = XmlChainedCalendar.class)
    })
    public AbstractXmlCalendar[] calendars;

    public XmlCalendars() {
    }

//    public boolean copyTo(InformationSet info) {
//        info.clear();
//        boolean rslt = true;
//        if (calendars != null) {
//            for (int i = 0; i < calendars.length; ++i) {
//                if (!calendars[i].addTo(info)) {
//                    rslt = false;
//                }
//            }
//        }
//        return rslt;
//    }
//
    public boolean copyTo(CalendarManager mgr) {
        boolean rslt = true;
        if (calendars != null) {
            for (int i = 0; i < calendars.length; ++i) {
                if (!calendars[i].addTo(mgr)) {
                    rslt = false;
                }
            }
        }
        return rslt;
    }

    public boolean isEmpty() {
        return calendars == null || calendars.length == 0;
    }

    @Override
    public CalendarManager create() {
        CalendarManager mgr = new CalendarManager();
        copyTo(mgr);
        return mgr;
    }

    @Override
    public void copy(CalendarManager mgr) {
        String[] names = mgr.getNames();
        List<AbstractXmlCalendar> tmp = new ArrayList<>();
        for (int i = 0; i < names.length; ++i) {
            String code = names[i];
            if (!code.equals(CalendarManager.DEF)) {
                AbstractXmlCalendar xcal = XmlNationalCalendar.create(code, mgr);
                if (xcal == null) {
                    xcal = XmlChainedCalendar.create(code, mgr);
                }
                if (xcal == null) {
                    xcal = XmlCompositeCalendar.create(code, mgr);
                }
                tmp.add(xcal);
            }
        }
        calendars = tmp.toArray(new AbstractXmlCalendar[tmp.size()]);
    }
}
