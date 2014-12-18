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


package ec.tss.xml.uscb;

import ec.tss.xml.IXmlConverter;
import ec.tstoolkit.utilities.StringFormatter;
import ec.tstoolkit.modelling.ChangeOfRegimeSpec;
import ec.tstoolkit.timeseries.Day;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlChangeOfRegimeSpec.NAME)
public class XmlChangeOfRegimeSpec implements IXmlConverter<ChangeOfRegimeSpec> {
    static final String NAME = "changeOfRegimeSpecType";

    @XmlElement
    public String date;
    @XmlElement
    public String type;

    @Override
    public ChangeOfRegimeSpec create() {
        return new ChangeOfRegimeSpec(StringFormatter.convertDay(date, Day.BEG), ChangeOfRegimeSpec.Type.valueOf(type));
    }

    @Override
    public void copy(ChangeOfRegimeSpec t) {
        type = t.getType().name();
        date = StringFormatter.convert(t.getDate());
    }
}
