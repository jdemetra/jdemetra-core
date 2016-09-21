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
package xml;

import ec.demetra.xml.core.XmlTsData;
import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.xml.sax.SAXException;

/**
 *
 * @author Jean Palate
 */
public class Schemas {
    public static final Schema Core, Calendars, Modelling, TramoSeats, X13;
    
    static{
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        factory.setErrorHandler(new SilentErrorHandler());
        Schema core = null;
        try {
            URL resource = Schemas.class.getResource("/core.xsd");
            core = factory.newSchema(resource);
        } catch (SAXException ex) {
        }
        Core = core;
        Schema calendars = null;
        try {
            URL resource = Schemas.class.getResource("/calendar.xsd");
            calendars = factory.newSchema(resource);
        } catch (SAXException ex) {
        }
        Calendars = calendars;
        Schema modelling = null;
        try {
            URL resource = XmlTsData.class.getResource("/regarima.xsd");
            modelling = factory.newSchema(resource);
        } catch (SAXException ex) {
        }
        Modelling = modelling;
        Schema trs = null;
        try {
            URL resource = XmlTsData.class.getResource("/tramoseats.xsd");
            trs = factory.newSchema(resource);
        } catch (SAXException ex) {
        }
        TramoSeats = trs;
        Schema x13 = null;
        try {
            URL resource = XmlTsData.class.getResource("/x13.xsd");
            x13 = factory.newSchema(resource);
        } catch (SAXException ex) {
        }
        X13 = x13;
        
    }
}
