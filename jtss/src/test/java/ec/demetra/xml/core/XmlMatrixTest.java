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
package ec.demetra.xml.core;

import ec.tstoolkit.maths.matrices.Matrix;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.xml.sax.SAXException;
import xml.Schemas;
import xml.TestErrorHandler;
import xml.TestValidationEventHandler;

/**
 *
 * @author Jean Palate
 */
public class XmlMatrixTest {

    private static final String FILE = "c:\\localdata\\matrix.xml";
    public XmlMatrixTest() {
    }

    @Test
    @Ignore
    public void testMarshal() throws FileNotFoundException, JAXBException, IOException {

        JAXBContext jaxb = JAXBContext.newInstance(XmlMatrix.class);
        XmlMatrix xmat = new XmlMatrix();
        Matrix m = new Matrix(5, 8);
        m.set((r, c) -> (r + 1) * (c + 1));
        xmat.copy(m);
        FileOutputStream ostream = new FileOutputStream(FILE);
        try (OutputStreamWriter writer = new OutputStreamWriter(ostream, StandardCharsets.UTF_8)) {
            Marshaller marshaller = jaxb.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(xmat, writer);
            writer.flush();
        }

        XmlMatrix rslt = null;
        FileInputStream istream = new FileInputStream(FILE);
        try (InputStreamReader reader = new InputStreamReader(istream, StandardCharsets.UTF_8)) {
            Unmarshaller unmarshaller = jaxb.createUnmarshaller();
            unmarshaller.setSchema(Schemas.Core);
            unmarshaller.setEventHandler(new TestValidationEventHandler());
            rslt = (XmlMatrix) unmarshaller.unmarshal(reader);
            Matrix m2 = rslt.create();
            assertTrue(m.equals(m2, 1e-6));
        }
    }

    @Test
    public void testValidation() throws FileNotFoundException, JAXBException, IOException, SAXException {

        JAXBContext jaxb = JAXBContext.newInstance(XmlMatrix.class);
        XmlMatrix xmat = new XmlMatrix();
        Matrix m = new Matrix(5, 8);
        m.set((r, c) -> (r + 1) * (c + 1));
        xmat.copy(m);
        JAXBSource source = new JAXBSource(jaxb, xmat);
        Validator validator = Schemas.Core.newValidator();
        //validator.setErrorHandler(new TestErrorHandler());
        validator.validate(source);
    }

}
