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
import demetra.toolkit.io.xml.legacy.IXmlConverter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Jean Palate
 */
@XmlRootElement(name = "validityperiod")
@XmlType(name = "validityperiod")
public class XmlValidityPeriod implements IXmlConverter<ValidityPeriod> {

    @XmlAttribute
    public String start;
    @XmlAttribute
    public String end;

    @Override
    public ValidityPeriod create() {
        LocalDate s = start == null ? LocalDate.MIN : LocalDate.parse(start, DateTimeFormatter.ISO_DATE);
        LocalDate e = end == null ? LocalDate.MAX : LocalDate.parse(end, DateTimeFormatter.ISO_DATE);
        return ValidityPeriod.between(s, e);
    }

    @Override
    public void copy(ValidityPeriod t) {
        if (t.isStartSpecified()) {
            start = t.getStart().format(DateTimeFormatter.ISO_DATE);
        } else {
            start = null;
        }
        if (t.isEndSpecified()) {
            end = t.getEnd().format(DateTimeFormatter.ISO_DATE);
        } else {
            end = null;
        }
    }
}
