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

import demetra.timeseries.ValidityPeriod;
import demetra.timeseries.calendars.FixedDay;
import demetra.toolkit.io.xml.legacy.IXmlConverter;
import java.time.Month;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Jean Palate
 */
@XmlType(name = XmlFixedDay.NAME)
public class XmlFixedDay extends AbstractXmlDay implements IXmlConverter<FixedDay> {
    static final String NAME = "fixedDayType";

    @XmlElement
    public Month month;
    @XmlElement
    public int day;

    @Override
    public FixedDay create() {
        return new FixedDay(day, month.getValue(), getWeight(), ValidityPeriod.ALWAYS);
    }

    @Override
    public void copy(FixedDay t) {
        day=t.getDay();
        month=Month.of(t.getMonth());
        setWeight(t.getWeight());
    }
}

