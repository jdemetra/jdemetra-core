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
package ec.tss.xml.regression;

import ec.tss.xml.information.XmlInformationSet;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.regression.OutlierType;
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
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class XmlVariablesTest {
    
    public XmlVariablesTest() {
    }

    @Test
    public void testMarshal() throws FileNotFoundException, JAXBException, IOException {
        JAXBContext jaxb = JAXBContext.newInstance(XmlVariables.class, XmlGenericTradingDays.class, XmlOutlier.class);
        
        XmlOutlier xout=new XmlOutlier();
        xout.position=Day.toDay();
        xout.prespecified=true;
        xout.type=OutlierType.AO;
        
        XmlGenericTradingDays xtd=new XmlGenericTradingDays();
         
        XmlVariables xvar=new XmlVariables();
        xvar.vars.add(xout);
        xvar.vars.add(xtd);
        
        FileOutputStream ostream = new FileOutputStream("c:\\localdata\\test.xml");
        try (OutputStreamWriter writer = new OutputStreamWriter(ostream, StandardCharsets.UTF_8)) {
            Marshaller marshaller = jaxb.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(xvar, writer);
            writer.flush();
        }
         
        XmlVariables rslt = null;
        FileInputStream istream = new FileInputStream("c:\\localdata\\test.xml");
        try (InputStreamReader reader = new InputStreamReader(istream, StandardCharsets.UTF_8)) {
            Unmarshaller unmarshaller = jaxb.createUnmarshaller();
            rslt = (XmlVariables) unmarshaller.unmarshal(reader);
        }
    }
}
