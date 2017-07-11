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
import ec.demetra.xml.core.XmlTs;
import ec.demetra.xml.core.XmlTsData;
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
public class XmlX13RequestsTest {
    
    public XmlX13RequestsTest() {
    }

    @Test
    public void testValidation() throws FileNotFoundException, JAXBException, IOException, SAXException {
        int N = 5;
        XmlX13Requests requests = new XmlX13Requests();
        requests.setFlat(true);
        requests.setParallelProcessing(false);
        for (int i = 0; i < N; ++i) {
            XmlX13AtomicRequest cur = new XmlX13AtomicRequest();
            cur.defaultSpecification = "RSA4c";
            cur.series = new XmlTs();
            XmlTsData.MARSHALLER.marshal(Data.P, cur.series);
            requests.getItems().add(cur);
        }
        JAXBContext jaxb = JAXBContext.newInstance(requests.getClass());
        JAXBSource source = new JAXBSource(jaxb, requests);
        Validator validator = Schemas.X13.newValidator();
        //validator.setErrorHandler(new TestErrorHandler());
        validator.validate(source);
    }
    
}
