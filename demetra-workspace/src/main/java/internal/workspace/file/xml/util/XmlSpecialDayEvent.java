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

import demetra.timeseries.ValidityPeriod;
import demetra.timeseries.calendars.Holiday;
import demetra.toolkit.io.xml.legacy.IXmlConverter;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Jean Palate
 */
@XmlType(name = XmlSpecialDayEvent.NAME)
public class XmlSpecialDayEvent implements IXmlConverter<demetra.timeseries.calendars.Holiday> {

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
    public Holiday create() {
        if (sday == null) {
            return null;
        }
        
        Holiday rslt = sday.create();
        if (validityperiod != null) {
            ValidityPeriod vp = validityperiod.create();
            rslt.forPeriod(vp.getStart(), vp.getEnd());
        }
        return rslt;
    }

    @Override
    public void copy(Holiday t) {
        sday = AbstractXmlDay.convert(t);
        if (!t.getValidityPeriod().equals(ValidityPeriod.ALWAYS)) {
            validityperiod = new XmlValidityPeriod();
            validityperiod.copy(t.getValidityPeriod());
        }
        else {
            validityperiod = null;
        }
    }
}
