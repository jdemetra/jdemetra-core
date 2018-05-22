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

package demetra.xml.sa;

import ec.satoolkit.ISaSpecification;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.satoolkit.x13.X13Specification;
import demetra.xml.sa.tramoseats.XmlTramoSeatsSpecification;
import demetra.xml.sa.x13.XmlX13Specification;
import java.io.Writer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAttribute;

/**
 *
 * @author Kristof Bayens
 */
public abstract class AbstractXmlSaSpecification {

    @XmlAttribute
    public String name;

    public static AbstractXmlSaSpecification create(ISaSpecification spec) {
        if (spec instanceof TramoSeatsSpecification) {
            TramoSeatsSpecification trspec = (TramoSeatsSpecification) spec;
            XmlTramoSeatsSpecification xspec = new XmlTramoSeatsSpecification();
            xspec.copy(trspec);
            return xspec;
        }
        if (spec instanceof X13Specification) {
            X13Specification x13spec = (X13Specification) spec;
            XmlX13Specification xspec = new XmlX13Specification();
            xspec.copy(x13spec);
            return xspec;
        }
        return null;
    }

    public void serialize(Writer writer) {
        try {
            JAXBContext context = JAXBContext.newInstance(this.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.marshal(this, writer);
        }
        catch (Exception ex) {}
    }

    public abstract ISaSpecification convert(boolean useSystem);
}
