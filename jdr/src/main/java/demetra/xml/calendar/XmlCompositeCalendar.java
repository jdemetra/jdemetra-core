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

package demetra.xml.calendar;

import demetra.xml.XmlWeightedItem;
import ec.tstoolkit.timeseries.calendars.CompositeGregorianCalendarProvider;
import ec.tstoolkit.timeseries.calendars.GregorianCalendarManager;
import ec.tstoolkit.timeseries.calendars.IGregorianCalendarProvider;
import ec.tstoolkit.utilities.WeightedItem;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlRootElement(name = "compositeCalendar")
@XmlType(name = XmlCompositeCalendar.NAME)
public class XmlCompositeCalendar extends AbstractXmlCalendar {

    static final String NAME = "compositeCalendarType";
 
    @XmlElement(name = "wCalendar")
    public XmlWeightedItem[] wcalendars;

    public XmlCompositeCalendar() {
    }

    public static XmlCompositeCalendar create(String code, GregorianCalendarManager mgr) {
        IGregorianCalendarProvider cal = mgr.get(code);
        if (cal == null || !(cal instanceof CompositeGregorianCalendarProvider))
            return null;
        CompositeGregorianCalendarProvider t = (CompositeGregorianCalendarProvider) cal;
        XmlCompositeCalendar xcal = new XmlCompositeCalendar();
        xcal.name = code;

        int n = t.getCount();
        if (n > 0) {
            xcal.wcalendars = new XmlWeightedItem[n];
            int i = 0;
            for (WeightedItem<String> item : t.items()) {
                XmlWeightedItem witem = new XmlWeightedItem();
                witem.weight = item.weight;
                witem.item = item.item;
                if (witem.item == null) {
                    return null;
                }
                xcal.wcalendars[i++] = witem;
            }
        }
        return xcal;
    }

//    @Override
//    public boolean addTo(InformationSet info) {
//        CompositeGregorianCalendarProvider composite = new CompositeGregorianCalendarProvider();
//        if (wcalendars != null) {
//            for (int i = 0; i < wcalendars.length; ++i) {
//                //IGregorianCalendarProvider calendar = info.get(wcalendars[i].item, IGregorianCalendarProvider.class);
//                String calendar = wcalendars[i].item;
//                if (calendar != null) {
//                    composite.add(new WeightedItem<String>(calendar, wcalendars[i].weight));
//                } else {
//                    return false;
//                }
//            }
//        }
//        if (name != null) {
//            info.set(name, composite);
//            return true;
//        } else {
//            return false;
//        }
//    }
   @Override
    public boolean addTo(GregorianCalendarManager mgr) {
        CompositeGregorianCalendarProvider composite = new CompositeGregorianCalendarProvider(mgr);
        if (wcalendars != null) {
            for (int i = 0; i < wcalendars.length; ++i) {
                //IGregorianCalendarProvider calendar = mgr.get(wcalendars[i].item, IGregorianCalendarProvider.class);
                String calendar = wcalendars[i].item;
                if (calendar != null) {
                    composite.add(new WeightedItem<>(calendar, wcalendars[i].weight));
                } else {
                    return false;
                }
            }
        }
        if (name != null) {
            mgr.set(name, composite);
            return true;
        } else {
            return false;
        }
    }
}
