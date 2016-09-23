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

import ec.tstoolkit.modelling.arima.tramo.EstimateSpec;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.modelling.arima.tramo.TransformSpec;
import ec.tstoolkit.timeseries.TsPeriodSelector;
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
public class XmlEstimationSpecTest {

    private static final String FILE = "c:\\localdata\\trs_estimationspec.xml";

    public XmlEstimationSpecTest() {
    }

    @Test
    @Ignore
    public void testMarshal() throws FileNotFoundException, JAXBException, IOException, Exception {

        TramoSpecification spec = TramoSpecification.TRfull;
        EstimateSpec espec = spec.getEstimate().clone();
        espec.setUbp(1);
        TsPeriodSelector sel = new TsPeriodSelector();
        sel.last(120);
        espec.setSpan(sel);
        XmlEstimationSpec xspec = new XmlEstimationSpec();
        XmlEstimationSpec.MARSHALLER.marshal(espec, xspec);
        JAXBContext jaxb = JAXBContext.newInstance(xspec.getClass());
        FileOutputStream ostream = new FileOutputStream(FILE);
        try (OutputStreamWriter writer = new OutputStreamWriter(ostream, StandardCharsets.UTF_8)) {
            Marshaller marshaller = jaxb.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(xspec, writer);
            writer.flush();
        }

        XmlEstimationSpec rslt;
        FileInputStream istream = new FileInputStream(FILE);
        try (InputStreamReader reader = new InputStreamReader(istream, StandardCharsets.UTF_8)) {
            Unmarshaller unmarshaller = jaxb.createUnmarshaller();
            rslt = (XmlEstimationSpec) unmarshaller.unmarshal(reader);
            EstimateSpec nspec = new EstimateSpec();
            XmlEstimationSpec.UNMARSHALLER.unmarshal(rslt, nspec);
            assertTrue(nspec.equals(espec));
        }
    }

    @Test
    @Ignore
    public void testValidation() throws FileNotFoundException, JAXBException, IOException, SAXException, Exception {

        TramoSpecification spec = TramoSpecification.TRfull;
        EstimateSpec espec = spec.getEstimate().clone();
        espec.setUbp(1);
        TsPeriodSelector sel = new TsPeriodSelector();
        sel.last(120);
        espec.setSpan(sel);
        XmlEstimationSpec xspec = new XmlEstimationSpec();
        XmlEstimationSpec.MARSHALLER.marshal(espec, xspec);
        JAXBContext jaxb = JAXBContext.newInstance(xspec.getClass());
        JAXBSource source = new JAXBSource(jaxb, xspec);
        Validator validator = Schemas.TramoSeats.newValidator();
        //validator.setErrorHandler(new TestErrorHandler());
        validator.validate(source);
    }

}
