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
package ec.demetra.xml.sa.x13;

import data.Data;
import ec.satoolkit.x13.X13Specification;
import ec.tss.TsFactory;
import ec.tss.sa.SaItem;
import ec.tss.sa.SaManager;
import ec.tss.sa.processors.X13Processor;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.validation.Validator;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.xml.sax.SAXException;
import xml.Schemas;
import xml.TestErrorHandler;

/**
 *
 * @author Jean Palate
 */
public class XmlRegArimaSpecificationTest {
    
    private static final String FILE = "c:\\localdata\\regarima.xml";
    private static final String PFILE = "c:\\localdata\\pregarima.xml";
    private static final RegArimaSpecification pspec;

    static {
        SaManager.instance.add(new X13Processor());
        SaItem item = new SaItem(X13Specification.RSA5, TsFactory.instance.createTs("test", null, Data.P));
        item.process();
        X13Specification tspec = (X13Specification) item.getPointSpecification();
        pspec = tspec.getRegArimaSpecification();
    }

    public XmlRegArimaSpecificationTest() {
    }

    @Test
    @Ignore
    public void testFile() throws FileNotFoundException, JAXBException, IOException {

        RegArimaSpecification spec = RegArimaSpecification.RG5;
        XmlRegArimaSpecification xspec = new XmlRegArimaSpecification();
        XmlRegArimaSpecification.MARSHALLER.marshal(spec, xspec);
        JAXBContext jaxb = JAXBContext.newInstance(xspec.getClass());
        FileOutputStream ostream = new FileOutputStream(FILE);
        try (OutputStreamWriter writer = new OutputStreamWriter(ostream, StandardCharsets.UTF_8)) {
            Marshaller marshaller = jaxb.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(xspec, writer);
            writer.flush();
        }

        XmlRegArimaSpecification rslt = null;
        FileInputStream istream = new FileInputStream(FILE);
        try (InputStreamReader reader = new InputStreamReader(istream, StandardCharsets.UTF_8)) {
            Unmarshaller unmarshaller = jaxb.createUnmarshaller();
            rslt = (XmlRegArimaSpecification) unmarshaller.unmarshal(reader);
            RegArimaSpecification nspec = new RegArimaSpecification();
            XmlRegArimaSpecification.UNMARSHALLER.unmarshal(rslt, nspec);
            assertTrue(spec.equals(nspec));
        }
    }

    @Test
    //@Ignore
    public void testValidation() throws FileNotFoundException, JAXBException, IOException, SAXException {

        RegArimaSpecification spec = RegArimaSpecification.RG5;
        XmlRegArimaSpecification xspec = new XmlRegArimaSpecification();
        XmlRegArimaSpecification.MARSHALLER.marshal(spec, xspec);
        JAXBContext jaxb = JAXBContext.newInstance(xspec.getClass());
        JAXBSource source = new JAXBSource(jaxb, xspec);
        Validator validator = Schemas.X13.newValidator();
        //validator.setErrorHandler(new TestErrorHandler());
        validator.validate(source);
    }

    @Test
    @Ignore
    public void testPFile() throws FileNotFoundException, JAXBException, IOException {

        XmlRegArimaSpecification xspec = new XmlRegArimaSpecification();
        XmlRegArimaSpecification.MARSHALLER.marshal(pspec, xspec);
        JAXBContext jaxb = JAXBContext.newInstance(xspec.getClass());
        FileOutputStream ostream = new FileOutputStream(PFILE);
        try (OutputStreamWriter writer = new OutputStreamWriter(ostream, StandardCharsets.UTF_8)) {
            Marshaller marshaller = jaxb.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(xspec, writer);
            writer.flush();
        }

        XmlRegArimaSpecification rslt = null;
        FileInputStream istream = new FileInputStream(PFILE);
        try (InputStreamReader reader = new InputStreamReader(istream, StandardCharsets.UTF_8)) {
            Unmarshaller unmarshaller = jaxb.createUnmarshaller();
            rslt = (XmlRegArimaSpecification) unmarshaller.unmarshal(reader);
            RegArimaSpecification nspec = new RegArimaSpecification();
            XmlRegArimaSpecification.UNMARSHALLER.unmarshal(rslt, nspec);
            assertTrue(pspec.equals(nspec));
        }
    }

    @Test
    //@Ignore
    public void testPValidation() throws FileNotFoundException, JAXBException, IOException, SAXException {

        XmlRegArimaSpecification xspec = new XmlRegArimaSpecification();
        XmlRegArimaSpecification.MARSHALLER.marshal(pspec, xspec);
        JAXBContext jaxb = JAXBContext.newInstance(xspec.getClass());
        JAXBSource source = new JAXBSource(jaxb, xspec);
        Validator validator = Schemas.X13.newValidator();
        //validator.setErrorHandler(new TestErrorHandler());
        validator.validate(source);
    }

    @Test
    //@Ignore
    public void testMarshalling() {
        test(RegArimaSpecification.RG0);
        test(RegArimaSpecification.RG1);
        test(RegArimaSpecification.RG2);
        test(RegArimaSpecification.RG3);
        test(RegArimaSpecification.RG4);
        test(RegArimaSpecification.RG5);
        test(pspec);
    }

    private void test(RegArimaSpecification spec) {
        XmlRegArimaSpecification xspec = new XmlRegArimaSpecification();
        XmlRegArimaSpecification.MARSHALLER.marshal(spec, xspec);
        RegArimaSpecification nspec = new RegArimaSpecification();
        XmlRegArimaSpecification.UNMARSHALLER.unmarshal(xspec, nspec);
        assertTrue(spec.equals(nspec));
    }

}
