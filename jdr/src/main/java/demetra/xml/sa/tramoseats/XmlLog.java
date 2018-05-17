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


package demetra.xml.sa.tramoseats;

import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import demetra.xml.sa.IXmlLog;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.arima.tramo.TransformSpec;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlLog.NAME)
public class XmlLog extends AbstractXmlTransform implements IXmlLog {
    static final String NAME = "logTransformSpecType";

    public XmlLog() { }

    @Override
    public void copyTo(TramoSeatsSpecification spec) {
        spec.getTramoSpecification().setTransform(new TransformSpec());
        spec.getTramoSpecification().getTransform().setFunction(DefaultTransformationType.Log);
    }
}
