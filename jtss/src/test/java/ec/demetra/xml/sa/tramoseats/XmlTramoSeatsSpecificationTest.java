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
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import javax.xml.validation.Validator;
import org.junit.Test;
import static org.junit.Assert.*;
import org.xml.sax.SAXException;
import xml.Schemas;
import xml.TestErrorHandler;

/**
 *
 * @author Jean Palate
 */
public class XmlTramoSeatsSpecificationTest {
    
    private static final TramoSeatsSpecification pspec;

    static {
        SaManager.instance.add(new TramoSeatsProcessor());
        TramoSeatsSpecification spec=TramoSeatsSpecification.RSAfull.clone();
        spec.getBenchmarkingSpecification().setEnabled(true);
        SaItem item = new SaItem(spec, TsFactory.instance.createTs("test", null, Data.P));
        item.process();
        TramoSeatsSpecification tspec = (TramoSeatsSpecification) item.getPointSpecification();
        pspec = tspec;
    }

    public XmlTramoSeatsSpecificationTest() {
    }

    @Test
    public void testValidation() throws FileNotFoundException, JAXBException, IOException, SAXException {
        testValidation(TramoSeatsSpecification.RSA0);
        testValidation(TramoSeatsSpecification.RSA1);
        testValidation(TramoSeatsSpecification.RSA2);
        testValidation(TramoSeatsSpecification.RSA3);
        testValidation(TramoSeatsSpecification.RSA4);
        testValidation(TramoSeatsSpecification.RSA5);
        testValidation(TramoSeatsSpecification.RSAfull);
        testValidation(pspec);
    }
    
    @Test
    public void testMarshalling() {
        test(TramoSeatsSpecification.RSA0);
        test(TramoSeatsSpecification.RSA1);
        test(TramoSeatsSpecification.RSA2);
        test(TramoSeatsSpecification.RSA3);
        test(TramoSeatsSpecification.RSA4);
        test(TramoSeatsSpecification.RSA5);
        test(TramoSeatsSpecification.RSAfull);
        test(pspec);
    }

    private void test(TramoSeatsSpecification spec) {
        XmlTramoSeatsSpecification xspec = new XmlTramoSeatsSpecification();
        XmlTramoSeatsSpecification.MARSHALLER.marshal(spec, xspec);
        TramoSeatsSpecification nspec = new TramoSeatsSpecification();
        XmlTramoSeatsSpecification.UNMARSHALLER.unmarshal(xspec, nspec);
        assertTrue(spec.equals(nspec));
    }
    
    private void testValidation(TramoSeatsSpecification spec) throws FileNotFoundException, JAXBException, IOException, SAXException {
        XmlTramoSeatsSpecification xspec = new XmlTramoSeatsSpecification();
        XmlTramoSeatsSpecification.MARSHALLER.marshal(spec, xspec);
        JAXBContext jaxb = JAXBContext.newInstance(xspec.getClass());
        JAXBSource source = new JAXBSource(jaxb, xspec);
        Validator validator = Schemas.TramoSeats.newValidator();
        //validator.setErrorHandler(new TestErrorHandler());
        validator.validate(source);
    }

}
