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


package demetra.xml.sa.uscb;

import demetra.xml.XmlPeriodSelection;
import ec.tstoolkit.modelling.arima.x13.BasicSpec;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlBasicSpec.NAME)
public class XmlBasicSpec {
    static final String NAME = "basicSpecType";

    @XmlElement
    public XmlPeriodSelection span;

    @XmlAttribute
    public boolean preprocess;

    public XmlBasicSpec() { }

    public static XmlBasicSpec create(BasicSpec spec) {
        if (spec.isDefault())
            return null;
        XmlBasicSpec x = new XmlBasicSpec();
        x.preprocess = spec.isPreprocessing();
        if (spec.getSpan() != null) {
            x.span = new XmlPeriodSelection();
            x.span.copy(spec.getSpan());
        }
        return x;
    }

    public void initSpec(BasicSpec spec) {
        if (span != null)
            spec.setSpan(span.create());
        spec.setPreprocessing(preprocess);
    }
}
