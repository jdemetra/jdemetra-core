/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.toolkit.io.xml.legacy.regression;

import demetra.timeseries.regression.InterventionVariable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Month;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.junit.Test;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class XmlInterventionVariableTest {

    private static final String FILE = "c:\\localdata\\intervention.xml";

    public XmlInterventionVariableTest() {
    }

    @Test
    @Ignore
    public void testFile() throws FileNotFoundException, JAXBException, IOException {

        JAXBContext jaxb = JAXBContext.newInstance(XmlInterventionVariable.class);
        InterventionVariable ivar = 
                InterventionVariable.builder()
                .deltaSeasonal(.9)
                .add(LocalDate.of(1999, Month.APRIL,4).atStartOfDay(), LocalDate.of(2005, Month.JUNE, 4).atStartOfDay())
                .build();
        XmlInterventionVariable xvar = XmlInterventionVariable.getAdapter().marshal(ivar);

        FileOutputStream ostream = new FileOutputStream(FILE);
        try (OutputStreamWriter writer = new OutputStreamWriter(ostream, StandardCharsets.UTF_8)) {
            Marshaller marshaller = jaxb.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//            marshaller.marshal(xvar, writer);
            marshaller.marshal(new JAXBElement(QName.valueOf("test"), XmlInterventionVariable.class, xvar), writer);
            writer.flush();
        }

        XmlInterventionVariable rslt = null;
        FileInputStream istream = new FileInputStream(FILE);
        try (InputStreamReader reader = new InputStreamReader(istream, StandardCharsets.UTF_8)) {
            Source source = new StreamSource(reader);
            Unmarshaller unmarshaller = jaxb.createUnmarshaller();
//            rslt = (XmlInterventionVariable) unmarshaller.unmarshal(reader);
            JAXBElement<XmlInterventionVariable> jrslt =   unmarshaller.unmarshal(source, XmlInterventionVariable.class);
            rslt=jrslt.getValue();
            InterventionVariable nvar = XmlInterventionVariable.getAdapter().unmarshal(rslt);
        }
    }

}
