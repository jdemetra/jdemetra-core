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


package ec.tss.xml.regression;

import ec.tss.xml.IXmlConverter;
import ec.tstoolkit.utilities.StringFormatter;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.regression.Ramp;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlRamp.NAME)
public class XmlRamp implements IXmlConverter<Ramp> {
    static final String NAME = "rampType";

    @XmlElement
    public String start;
    @XmlElement
    public String end;

    public XmlRamp() { }

    @Override
    public Ramp create() {
        Day d0 = StringFormatter.convertDay(start, Day.BEG);
        Day d1 = StringFormatter.convertDay(end, Day.END);
        return new Ramp(d0, d1);
    }

    @Override
    public void copy(Ramp t) {
        start = StringFormatter.convert(t.getStart());
        end = StringFormatter.convert(t.getEnd());
    }
}
