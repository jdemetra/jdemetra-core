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

import demetra.timeseries.calendars.SingleDate;
import demetra.toolkit.io.xml.legacy.IXmlConverter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Jean Palate
 */
@XmlType(name = XmlSingleDate.NAME)
public class XmlSingleDate extends AbstractXmlDay implements IXmlConverter<SingleDate> {

    static final String NAME = "singleDateType";

    @XmlElement
    public String date;

    @Override
    public SingleDate create() {
        return new SingleDate(LocalDate.parse(date, DateTimeFormatter.ISO_DATE), getWeight());
    }

    @Override
    public void copy(SingleDate t) {
        date = t.getDate().format(DateTimeFormatter.ISO_DATE);
        setWeight(t.getWeight());
    }

}
