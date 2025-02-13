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

import data.Data;
import ec.tss.Ts;
import ec.tss.TsFactory;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.timeseries.simplets.TsData;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;
import xml.Schemas;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.validation.Validator;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 *
 * @author Jean Palate
 */
public class XmlTsTest {
    
    private static final String FILE="c:\\localdata\\ts.xml";

    public XmlTsTest() {
    }

    @Test
    @Ignore
    public void testMarshal() throws FileNotFoundException, JAXBException, IOException {

        JAXBContext jaxb = JAXBContext.newInstance(ec.demetra.xml.core.XmlTsCollection.class);
        Ts s=TsFactory.instance.createTs("p", null, Data.P);
        MetaData md=new MetaData();
        md.put("test", "10");
        s.set(md);
        XmlTs xs=new XmlTs();
        ec.demetra.xml.core.XmlTs.MARSHALLER.marshal(s, xs);
        try (Writer writer = Files.newBufferedWriter(Paths.get(FILE), StandardCharsets.UTF_8)) {
            Marshaller marshaller = jaxb.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(xs, writer);
            writer.flush();
        }

        ec.demetra.xml.core.XmlTs rslt = null;
        try (Reader reader = Files.newBufferedReader(Paths.get(FILE), StandardCharsets.UTF_8)) {
            Unmarshaller unmarshaller = jaxb.createUnmarshaller();
            rslt = (ec.demetra.xml.core.XmlTs) unmarshaller.unmarshal(reader);
            TsData unmarshal = ec.demetra.xml.core.XmlTs.UNMARSHALLER.unmarshal(rslt);
        }
    }
    
    @Test
    public void testValidation() throws FileNotFoundException, JAXBException, IOException, SAXException {

        JAXBContext jaxb = JAXBContext.newInstance(XmlTsCollection.class);
        Ts s=TsFactory.instance.createTs("p", null, Data.P);
        MetaData md=new MetaData();
        md.put("test", "10");
        s.set(md);
        XmlTs xs=new XmlTs();
        ec.demetra.xml.core.XmlTs.MARSHALLER.marshal(s, xs);
        JAXBSource source = new JAXBSource(jaxb, xs);
        Validator validator = Schemas.Core.newValidator();
        //validator.setErrorHandler(new TestErrorHandler());
        validator.validate(source);
    }
    
}

