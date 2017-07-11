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
public class XmlX13SpecificationTest {
    
    private static final X13Specification pspec;

    static {
        SaManager.instance.add(new X13Processor());
        SaItem item = new SaItem(X13Specification.RSA5, TsFactory.instance.createTs("test", null, Data.P));
        item.process();
        pspec = (X13Specification) item.getPointSpecification();
    }
    
    public XmlX13SpecificationTest() {
    }

    @Test
    public void testValidation() throws FileNotFoundException, JAXBException, IOException, SAXException {
        testValidation(X13Specification.RSA0);
        testValidation(X13Specification.RSA1);
        testValidation(X13Specification.RSA2);
        testValidation(X13Specification.RSA3);
        testValidation(X13Specification.RSA4);
        testValidation(X13Specification.RSA5);
        testValidation(X13Specification.RSAX11);
        testValidation(pspec);
    }
    
    @Test
    public void testMarshalling() {
        test(X13Specification.RSA0);
        test(X13Specification.RSA1);
        test(X13Specification.RSA2);
        test(X13Specification.RSA3);
        test(X13Specification.RSA4);
        test(X13Specification.RSA5);
        test(X13Specification.RSAX11);
        test(pspec);
    }

    private void test(X13Specification spec) {
        XmlX13Specification xspec = new XmlX13Specification();
        XmlX13Specification.MARSHALLER.marshal(spec, xspec);
        X13Specification nspec = new X13Specification();
        XmlX13Specification.UNMARSHALLER.unmarshal(xspec, nspec);
        assertTrue(spec.equals(nspec));
    }
    
    private void testValidation(X13Specification spec) throws FileNotFoundException, JAXBException, IOException, SAXException {
        XmlX13Specification xspec = new XmlX13Specification();
        XmlX13Specification.MARSHALLER.marshal(spec, xspec);
        JAXBContext jaxb = JAXBContext.newInstance(xspec.getClass());
        JAXBSource source = new JAXBSource(jaxb, xspec);
        Validator validator = Schemas.X13.newValidator();
        //validator.setErrorHandler(new TestErrorHandler());
        validator.validate(source);
    }

}
