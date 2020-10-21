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
package demetra.toolkit.io.xml.legacy.calendars;

import demetra.timeseries.calendars.CompositeCalendar;
import demetra.toolkit.io.xml.legacy.core.XmlWeightedItem;
import demetra.util.WeightedItem;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import nbbrd.service.ServiceProvider;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CompositeCalendarType", propOrder = {
    "weightedCalendar"
})
public class XmlCompositeCalendar
        extends XmlCalendar {

    @XmlElement(name = "WeightedCalendar", required = true)
    protected List<XmlWeightedItem> weightedCalendar;

    public List<XmlWeightedItem> getWeightedCalendar() {
        if (weightedCalendar == null) {
            weightedCalendar = new ArrayList<>();
        }
        return this.weightedCalendar;
    }

    @ServiceProvider(CalendarAdapter.class)
    public static class Adapter extends CalendarAdapter<XmlCompositeCalendar, CompositeCalendar> {

        @Override
        public Class<CompositeCalendar> getValueType() {
            return CompositeCalendar.class;
        }

        @Override
        public Class<XmlCompositeCalendar> getXmlType() {
            return XmlCompositeCalendar.class;
        }

        @Override
        public CompositeCalendar unmarshal(XmlCompositeCalendar v) {
            List<XmlWeightedItem> list = v.getWeightedCalendar();
            WeightedItem[] items = new WeightedItem[list.size()];
            int i = 0;
            for (XmlWeightedItem item : list) {
                items[i++] = new WeightedItem<>(item.getItem(), item.getWeight());
            }
            return new CompositeCalendar(items);
        }

        @Override
        public XmlCompositeCalendar marshal(CompositeCalendar v) {
            XmlCompositeCalendar xcal = new XmlCompositeCalendar();
            List<XmlWeightedItem> list = xcal.getWeightedCalendar();
            WeightedItem<String>[] calendars = v.getCalendars();
            for (int i = 0; i < calendars.length; ++i) {
                list.add(new XmlWeightedItem(calendars[i].getItem(), calendars[i].getWeight()));
            }
            return xcal;
        }
    }

    private static final Adapter ADAPTER = new Adapter();

    public static Adapter getAdapter() {
        return ADAPTER;
    }

}
