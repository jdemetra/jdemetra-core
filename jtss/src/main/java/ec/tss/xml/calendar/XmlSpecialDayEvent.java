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
import ec.tss.xml.XmlValidityPeriod;
import ec.tstoolkit.timeseries.calendars.SpecialDayEvent;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Jean Palate
 */
@XmlType(name = XmlSpecialDayEvent.NAME)
public class XmlSpecialDayEvent implements IXmlConverter<SpecialDayEvent> {

    static final String NAME = "specialDayEventType";
    @XmlElements(value = {
        @XmlElement(name = "fixedDay", type = XmlFixedDay.class),
        @XmlElement(name = "fixedWeekDay", type = XmlFixedWeekDay.class),
        @XmlElement(name = "easterRelatedDay", type = XmlEasterRelatedDay.class),
        @XmlElement(name = "specialCalendarDay", type = XmlSpecialCalendarDay.class)
    })
    public AbstractXmlDay sday;
    
    @XmlElement
    public XmlValidityPeriod validityperiod;

    @Override
    public SpecialDayEvent create() {
        if (sday == null) {
            return null;
        }
        SpecialDayEvent rslt = new SpecialDayEvent(sday.create());
        if (validityperiod != null) {
            rslt.setValidityPeriod(validityperiod.create());
        }
        return rslt;
    }

    @Override
    public void copy(SpecialDayEvent t) {
        sday = AbstractXmlDay.convert(t.day);
        if (t.getValidityPeriod() != null) {
            validityperiod = new XmlValidityPeriod();
            validityperiod.copy(t.getValidityPeriod());
        }
        else {
            validityperiod = null;
        }
    }
}
