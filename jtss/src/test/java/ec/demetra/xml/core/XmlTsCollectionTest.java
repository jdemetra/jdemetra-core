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
import ec.tss.TsCollection;
import ec.tss.TsFactory;
import ec.tstoolkit.MetaData;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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

/**
 *
 * @author Jean Palate
 */
public class XmlTsCollectionTest {
    
    private static final String FILE="c:\\localdata\\tscollection.xml";

    public XmlTsCollectionTest() {
    }

    @Test
    @Ignore
    public void testMarshal() throws FileNotFoundException, JAXBException, IOException {

        JAXBContext jaxb = JAXBContext.newInstance(XmlTsCollection.class);
        XmlTsCollection xcoll = new XmlTsCollection();
        TsCollection collection=TsFactory.instance.createTsCollection("test");
        MetaData md=new MetaData();
        md.put("test", "10");
        collection.set(md);
        collection.add(TsFactory.instance.createTs("p", null, Data.P));
        collection.add(TsFactory.instance.createTs("x", null, Data.X));
        XmlTsCollection.MARSHALLER.marshal(collection, xcoll);
        try (Writer writer = Files.newBufferedWriter(Paths.get(FILE), StandardCharsets.UTF_8)) {
            Marshaller marshaller = jaxb.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(xcoll, writer);
            writer.flush();
        }

        XmlTsCollection rslt = null;
        try (Reader reader = Files.newBufferedReader(Paths.get(FILE), StandardCharsets.UTF_8)) {
            Unmarshaller unmarshaller = jaxb.createUnmarshaller();
            rslt = (XmlTsCollection) unmarshaller.unmarshal(reader);
            TsCollection ncoll = XmlTsCollection.UNMARSHALLER.unmarshal(rslt);
            assertTrue(ncoll.getCount()==collection.getCount());
        }
    }

    @Test
    public void testValidation() throws FileNotFoundException, JAXBException, IOException, SAXException {

        JAXBContext jaxb = JAXBContext.newInstance(XmlTsCollection.class);
        XmlTsCollection xcoll = new XmlTsCollection();
        TsCollection collection=TsFactory.instance.createTsCollection("test");
        MetaData md=new MetaData();
        md.put("test", "10");
        collection.set(md);
        collection.add(TsFactory.instance.createTs("p", null, Data.P));
        collection.add(TsFactory.instance.createTs("x", null, Data.X));
        XmlTsCollection.MARSHALLER.marshal(collection, xcoll);
        JAXBSource source = new JAXBSource(jaxb, xcoll);
        Validator validator = Schemas.Core.newValidator();
        //validator.setErrorHandler(new TestErrorHandler());
        validator.validate(source);
    }
    
}
