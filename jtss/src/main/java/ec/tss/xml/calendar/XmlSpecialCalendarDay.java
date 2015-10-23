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
import ec.tstoolkit.timeseries.calendars.DayEvent;
import ec.tstoolkit.timeseries.calendars.SpecialCalendarDay;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Jean Palate
 */
@XmlType(name = XmlSpecialCalendarDay.NAME)
public class XmlSpecialCalendarDay extends AbstractXmlDay implements IXmlConverter<SpecialCalendarDay> {

    static final String NAME = "specialCalendarDayType";
    @XmlElement(name = "event")
    public DayEvent ev;
    @XmlElement
    public Integer offset;
    @XmlAttribute
    public Boolean julian;

    @Override
    public SpecialCalendarDay create() {
        return new SpecialCalendarDay(ev, getOffset(), getWeight(), isJulian());
    }

    @Override
    public void copy(SpecialCalendarDay t) {
        ev=t.event;
        setOffset(t.offset);
        setWeight(t.getWeight());
        setJulian(t.isJulianEaster());
    }
    
    private void setOffset(int val) {
        if (val == 0) {
            offset = null;
        }
        else {
            offset = val;
        }
    }

    private int getOffset() {
        if (offset == null) {
            return 0;
        }
        else {
            return offset;
        }
    }
    
    private void setJulian(boolean j) {
        if (j)
            julian = true;
        else
            julian = null;
    }
    
    private boolean isJulian() {
        if (julian == null) {
            return false;
        } else {
            return julian;
        }
    }
}
