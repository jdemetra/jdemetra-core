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

import ec.tstoolkit.modelling.arima.x13.TransformSpec;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlTransformSpec.NAME)
public class XmlTransformSpec {
    static final String NAME = "transformSpecType";

    @XmlElement
    public Double constant;

    @XmlElements(value = {
        @XmlElement(name = "auto", type = XmlAuto.class),
        @XmlElement(name = "log", type = XmlLog.class),
        @XmlElement(name = "level", type = XmlLevel.class)})
    public AbstractXmlTransform transform;

    public XmlTransformSpec() { }

    public static XmlTransformSpec create(TransformSpec spec) {
        AbstractXmlTransform transform = AbstractXmlTransform.create(spec);
        if (transform == null)
            return null;
        XmlTransformSpec x = new XmlTransformSpec();
        x.transform = transform;
        return x;
    }

    public void initSpec(TransformSpec spec) {
        transform.copyTo(spec);
    }
}
