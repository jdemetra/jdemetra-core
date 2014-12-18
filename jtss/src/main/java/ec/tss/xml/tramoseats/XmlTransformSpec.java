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

package ec.tss.xml.tramoseats;

import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tss.xml.XmlPeriodSelection;
import ec.tstoolkit.modelling.arima.tramo.TransformSpec;
import ec.tstoolkit.timeseries.PeriodSelectorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlTransformSpec.NAME)
public class XmlTransformSpec implements IXmlTramoSeatsSpec {

    static final String NAME = "transformSpecType";
    @XmlElement
    public XmlPeriodSelection span;
    @XmlElements(value = {
        @XmlElement(name = "auto", type = XmlAuto.class),
        @XmlElement(name = "log", type = XmlLog.class),
        @XmlElement(name = "level", type = XmlLevel.class)})
    public AbstractXmlTransform transform;

    public XmlTransformSpec() {
    }

    public static XmlTransformSpec create(TransformSpec spec) {
        AbstractXmlTransform transform = AbstractXmlTransform.create(spec);
        if (transform == null) {
            return null;
        }
        XmlTransformSpec x = new XmlTransformSpec();
        x.transform = transform;
        if (spec.getSpan().getType() != PeriodSelectorType.All) {
            x.span = new XmlPeriodSelection();
            x.span.copy(spec.getSpan());
        }
        return x;
    }

    @Override
    public void copyTo(TramoSeatsSpecification spec) {
        transform.copyTo(spec);
        if (span != null) {
            TransformSpec tspec = spec.getTramoSpecification().getTransform();
            if (tspec == null) {
                tspec = new TransformSpec();
                spec.getTramoSpecification().setTransform(tspec);
            }
            tspec.setSpan(span.create());
        }
    }
}
