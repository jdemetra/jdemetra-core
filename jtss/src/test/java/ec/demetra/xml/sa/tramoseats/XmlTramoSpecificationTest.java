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

import data.Data;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tss.TsFactory;
import ec.tss.sa.SaItem;
import ec.tss.sa.SaManager;
import ec.tss.sa.processors.TramoSeatsProcessor;
import ec.tstoolkit.algorithm.implementation.TramoProcessingFactory;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.modelling.arima.tramo.TransformSpec;
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
public class XmlTramoSpecificationTest {

    private static final String FILE = "c:\\localdata\\tramo.xml";
    private static final String PFILE = "c:\\localdata\\ptramo.xml";
    private static final TramoSpecification pspec;

    static {
        SaManager.instance.add(new TramoSeatsProcessor());
        SaItem item = new SaItem(TramoSeatsSpecification.RSAfull, TsFactory.instance.createTs("test", null, Data.P));
        item.process();
        TramoSeatsSpecification tspec = (TramoSeatsSpecification) item.getPointSpecification();
        pspec = tspec.getTramoSpecification();
    }

    public XmlTramoSpecificationTest() {
    }

    @Test
    @Ignore
    public void testFile() throws FileNotFoundException, JAXBException, IOException {

        TramoSpecification spec = TramoSpecification.TRfull;
        XmlTramoSpecification xspec = new XmlTramoSpecification();
        XmlTramoSpecification.MARSHALLER.marshal(spec, xspec);
        JAXBContext jaxb = JAXBContext.newInstance(xspec.getClass());
        FileOutputStream ostream = new FileOutputStream(FILE);
        try (OutputStreamWriter writer = new OutputStreamWriter(ostream, StandardCharsets.UTF_8)) {
            Marshaller marshaller = jaxb.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(xspec, writer);
            writer.flush();
        }

        XmlTramoSpecification rslt = null;
        FileInputStream istream = new FileInputStream(FILE);
        try (InputStreamReader reader = new InputStreamReader(istream, StandardCharsets.UTF_8)) {
            Unmarshaller unmarshaller = jaxb.createUnmarshaller();
            rslt = (XmlTramoSpecification) unmarshaller.unmarshal(reader);
            TramoSpecification nspec = new TramoSpecification();
            XmlTramoSpecification.UNMARSHALLER.unmarshal(rslt, nspec);
            assertTrue(spec.equals(nspec));
        }
    }

    @Test
    public void testValidation() throws FileNotFoundException, JAXBException, IOException, SAXException {

        TramoSpecification spec = TramoSpecification.TRfull;
        XmlTramoSpecification xspec = new XmlTramoSpecification();
        XmlTramoSpecification.MARSHALLER.marshal(spec, xspec);
        JAXBContext jaxb = JAXBContext.newInstance(xspec.getClass());
        JAXBSource source = new JAXBSource(jaxb, xspec);
        Validator validator = Schemas.TramoSeats.newValidator();
        //validator.setErrorHandler(new TestErrorHandler());
        validator.validate(source);
    }

    @Test
    @Ignore
    public void testPFile() throws FileNotFoundException, JAXBException, IOException {

        XmlTramoSpecification xspec = new XmlTramoSpecification();
        XmlTramoSpecification.MARSHALLER.marshal(pspec, xspec);
        JAXBContext jaxb = JAXBContext.newInstance(xspec.getClass());
        FileOutputStream ostream = new FileOutputStream(PFILE);
        try (OutputStreamWriter writer = new OutputStreamWriter(ostream, StandardCharsets.UTF_8)) {
            Marshaller marshaller = jaxb.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(xspec, writer);
            writer.flush();
        }

        XmlTramoSpecification rslt = null;
        FileInputStream istream = new FileInputStream(PFILE);
        try (InputStreamReader reader = new InputStreamReader(istream, StandardCharsets.UTF_8)) {
            Unmarshaller unmarshaller = jaxb.createUnmarshaller();
            rslt = (XmlTramoSpecification) unmarshaller.unmarshal(reader);
            TramoSpecification nspec = new TramoSpecification();
            XmlTramoSpecification.UNMARSHALLER.unmarshal(rslt, nspec);
            assertTrue(pspec.equals(nspec));
        }
    }

    @Test
    //@Ignore
    public void testPValidation() throws FileNotFoundException, JAXBException, IOException, SAXException {

        XmlTramoSpecification xspec = new XmlTramoSpecification();
        XmlTramoSpecification.MARSHALLER.marshal(pspec, xspec);
        JAXBContext jaxb = JAXBContext.newInstance(xspec.getClass());
        JAXBSource source = new JAXBSource(jaxb, xspec);
        Validator validator = Schemas.TramoSeats.newValidator();
        //validator.setErrorHandler(new TestErrorHandler());
        validator.validate(source);
    }

    @Test
    public void testMarshalling() {
        test(TramoSpecification.TR0);
        test(TramoSpecification.TR1);
        test(TramoSpecification.TR2);
        test(TramoSpecification.TR3);
        test(TramoSpecification.TR4);
        test(TramoSpecification.TR5);
        test(TramoSpecification.TRfull);
        test(pspec);
    }

    private void test(TramoSpecification spec) {
        XmlTramoSpecification xspec = new XmlTramoSpecification();
        XmlTramoSpecification.MARSHALLER.marshal(spec, xspec);
        TramoSpecification nspec = new TramoSpecification();
        XmlTramoSpecification.UNMARSHALLER.unmarshal(xspec, nspec);
        assertTrue(spec.equals(nspec));
    }

}
