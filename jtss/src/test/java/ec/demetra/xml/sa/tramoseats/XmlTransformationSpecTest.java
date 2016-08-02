/*
 * Copyright 2016 National Bank of Belgium
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
package ec.demetra.xml.sa.tramoseats;

import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.modelling.arima.tramo.TransformSpec;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

/**
 *
 * @author Jean Palate
 */
public class XmlTransformationSpecTest {

    private static final String FILE = "c:\\localdata\\trs_transformationspec.xml";
    private static final Schema schema;

    static {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema core = null;
        try {
            URL resource = XmlTransformationSpec.class.getResource("/modelling.xsd");
            core = factory.newSchema(resource);
        } catch (SAXException ex) {
            Logger.getLogger(XmlTransformationSpecTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        schema = core;
    }

    public XmlTransformationSpecTest() {
    }

    @Test
    //@Ignore
    public void testMarshal() throws FileNotFoundException, JAXBException, IOException {

        TramoSpecification spec = TramoSpecification.TRfull;
        TransformSpec tspec = spec.getTransform().clone();
        XmlTransformationSpec xspec = new XmlTransformationSpec();
        xspec.copy(tspec);
        JAXBContext jaxb = JAXBContext.newInstance(xspec.getClass());
        FileOutputStream ostream = new FileOutputStream(FILE);
        try (OutputStreamWriter writer = new OutputStreamWriter(ostream, StandardCharsets.UTF_8)) {
            Marshaller marshaller = jaxb.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(xspec, writer);
            writer.flush();
        }

        XmlTransformationSpec rslt = null;
        FileInputStream istream = new FileInputStream(FILE);
        try (InputStreamReader reader = new InputStreamReader(istream, StandardCharsets.UTF_8)) {
            Unmarshaller unmarshaller = jaxb.createUnmarshaller();
            rslt = (XmlTransformationSpec) unmarshaller.unmarshal(reader);

            assertTrue(rslt.create().equals(tspec));
        }
    }

}
