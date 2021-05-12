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

import demetra.timeseries.calendars.CalendarDefinition;
import demetra.timeseries.calendars.CalendarManager;
import demetra.timeseries.calendars.CompositeCalendar;
import demetra.util.WeightedItem;
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

    public static XmlCompositeCalendar create(String code, CalendarManager mgr) {
        CalendarDefinition cal = mgr.get(code);
        if (cal == null || !(cal instanceof CompositeCalendar))
            return null;
        CompositeCalendar t = (CompositeCalendar) cal;
        XmlCompositeCalendar xcal = new XmlCompositeCalendar();
        xcal.name = code;

        int n = t.getCalendars().length;
        if (n > 0) {
            xcal.wcalendars = new XmlWeightedItem[n];
            int i = 0;
            for (WeightedItem<String> item : t.getCalendars()) {
                XmlWeightedItem witem = new XmlWeightedItem();
                witem.weight = item.getWeight();
                witem.item = item.getItem();
                if (witem.item == null) {
                    return null;
                }
                xcal.wcalendars[i++] = witem;
            }
        }
        return xcal;
    }

   @Override
    public boolean addTo(CalendarManager mgr) {
        CompositeCalendar composite = null;
        if (wcalendars != null) {
            WeightedItem[] items=new WeightedItem[wcalendars.length]; 
            for (int i = 0; i < wcalendars.length; ++i) {
                //IGregorianCalendarProvider calendar = mgr.get(wcalendars[i].item, IGregorianCalendarProvider.class);
                String calendar = wcalendars[i].item;
                items[i]=new WeightedItem<>(calendar, wcalendars[i].weight);
            }
            composite=new CompositeCalendar(items);
        }
        if (name != null && composite != null) {
            mgr.set(name, composite);
            return true;
        } else {
            return false;
        }
    }
}
