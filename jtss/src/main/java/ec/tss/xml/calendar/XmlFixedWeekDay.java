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

import ec.tss.xml.IXmlConverter;
import ec.tstoolkit.timeseries.DayOfWeek;
import ec.tstoolkit.timeseries.Month;
import ec.tstoolkit.timeseries.calendars.FixedWeekDay;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author PCuser
 */
@XmlType(name = XmlFixedWeekDay.NAME)
public class XmlFixedWeekDay extends AbstractXmlDay implements IXmlConverter<FixedWeekDay> {
    static final String NAME = "fixedWeekDayType";

    @XmlElement
    public Month month;
    @XmlElement
    public DayOfWeek dayofweek;
    @XmlElement
    public int week;

    @Override
    public FixedWeekDay create() {
        return new FixedWeekDay(week - 1, dayofweek, month, getWeight());
    }

    @Override
    public void copy(FixedWeekDay t) {
        dayofweek = t.dayOfWeek;
        month = t.month;
        week = t.week + 1;
        setWeight(t.getWeight());
    }
}
