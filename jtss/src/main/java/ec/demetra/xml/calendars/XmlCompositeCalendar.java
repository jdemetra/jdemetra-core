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
package ec.demetra.xml.calendars;

import ec.demetra.xml.core.XmlWeightedItem;
import ec.tstoolkit.timeseries.calendars.CompositeGregorianCalendarProvider;
import ec.tstoolkit.timeseries.calendars.GregorianCalendarManager;
import ec.tstoolkit.utilities.WeightedItem;
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
    public static class Adapter extends CalendarAdapter<XmlCompositeCalendar, CompositeGregorianCalendarProvider> {

        @Override
        public Class<CompositeGregorianCalendarProvider> getValueType() {
            return CompositeGregorianCalendarProvider.class;
        }

        @Override
        public Class<XmlCompositeCalendar> getXmlType() {
            return XmlCompositeCalendar.class;
        }

        @Override
        public CompositeGregorianCalendarProvider unmarshal(XmlCompositeCalendar v) {
            CompositeGregorianCalendarProvider cal = new CompositeGregorianCalendarProvider();
            List<XmlWeightedItem> list = v.getWeightedCalendar();
            for (XmlWeightedItem item : list) {
                cal.add(new WeightedItem<>(item.getItem(), item.getWeight()));
            }
            return cal;
        }

        @Override
        public CompositeGregorianCalendarProvider unmarshal(XmlCompositeCalendar v, GregorianCalendarManager mgr) {
            CompositeGregorianCalendarProvider cal = new CompositeGregorianCalendarProvider(mgr);
            List<XmlWeightedItem> list = v.getWeightedCalendar();
            for (XmlWeightedItem item : list) {
                cal.add(new WeightedItem<>(item.getItem(), item.getWeight()));
            }
            return cal;
        }

        @Override
        public XmlCompositeCalendar marshal(CompositeGregorianCalendarProvider v) {
            XmlCompositeCalendar xcal = new XmlCompositeCalendar();
            List<WeightedItem<String>> items = v.items();
            List<XmlWeightedItem> list = xcal.getWeightedCalendar();
            for (WeightedItem<String> item : items) {
                list.add(new XmlWeightedItem(item.item, item.weight));
            }
            return xcal;
        }
    }

    private static final Adapter ADAPTER = new Adapter();

    public static Adapter getAdapter() {
        return ADAPTER;
    }

}
