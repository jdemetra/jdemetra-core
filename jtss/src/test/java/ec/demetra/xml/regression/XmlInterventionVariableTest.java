/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.demetra.xml.regression;

import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.Month;
import ec.tstoolkit.timeseries.regression.InterventionVariable;
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.bind.*;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

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
        InterventionVariable ivar = new InterventionVariable();
        ivar.setDelta(.9);
        Day d0 = new Day(1999, Month.April, 3);
        Day d1 = new Day(2005, Month.June, 3);
        ivar.add(d0, d0);
        ivar.add(d1, d1.plus(90));
        XmlInterventionVariable xvar = XmlInterventionVariable.getAdapter().marshal(ivar);

        try (Writer writer = Files.newBufferedWriter(Paths.get(FILE), StandardCharsets.UTF_8)) {
            Marshaller marshaller = jaxb.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//            marshaller.marshal(xvar, writer);
            marshaller.marshal(new JAXBElement(QName.valueOf("test"), XmlInterventionVariable.class, xvar), writer);
            writer.flush();
        }

        XmlInterventionVariable rslt = null;
        try (Reader reader = Files.newBufferedReader(Paths.get(FILE), StandardCharsets.UTF_8)) {
            Source source = new StreamSource(reader);
            Unmarshaller unmarshaller = jaxb.createUnmarshaller();
//            rslt = (XmlInterventionVariable) unmarshaller.unmarshal(reader);
            JAXBElement<XmlInterventionVariable> jrslt =   unmarshaller.unmarshal(source, XmlInterventionVariable.class);
            rslt=jrslt.getValue();
            InterventionVariable nvar = XmlInterventionVariable.getAdapter().unmarshal(rslt);
        }
    }

}
