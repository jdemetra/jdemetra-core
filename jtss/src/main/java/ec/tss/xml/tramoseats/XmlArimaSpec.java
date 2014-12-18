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
import ec.tss.xml.XmlEmptyElement;
import ec.tstoolkit.modelling.arima.tramo.ArimaSpec;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlArimaSpec.NAME)
public class XmlArimaSpec extends AbstractXmlArimaSpec {

    static final String NAME = "arimaSpecType";
    @XmlElement
    public XmlEmptyElement mean;
    @XmlElements(value = {
        @XmlElement(name = "spec", type = XmlArimaOrders.class),
        @XmlElement(name = "model", type = XmlArimaPolynomials.class)
    })
    public AbstractXmlArimaModel model;

    public static XmlArimaSpec create(ArimaSpec spec) {
        XmlArimaSpec arima = new XmlArimaSpec();
        if (spec.isMean()) {
            arima.mean = new XmlEmptyElement();
        }
        if (spec.hasParameters()) {
            arima.model = XmlArimaPolynomials.create(spec);
        } else {
            arima.model = XmlArimaOrders.create(spec);
        }
        return arima;
    }

    @Override
    public void copyTo(TramoSeatsSpecification spec) {
        model.copyTo(spec);
        spec.getTramoSpecification().getArima().setMean(mean != null);
    }
}
