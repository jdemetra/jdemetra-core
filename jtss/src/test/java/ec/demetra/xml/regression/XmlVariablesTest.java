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
package ec.demetra.xml.regression;

import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.DayClustering;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class XmlVariablesTest {
    
    private static final String FILE="c:\\localdata\\variables.xml";

    public XmlVariablesTest() {
    }

    @Test
    @Ignore
    public void testMarshal() throws FileNotFoundException, JAXBException, IOException {

        List<Class> xmlClasses = TsVariableAdapters.getDefault().getXmlClasses();
        List<Class> xmlClasses2 = TsModifierAdapters.getDefault().getXmlClasses();
        xmlClasses.addAll(xmlClasses2);
        xmlClasses.add(XmlVariables.class);
        JAXBContext jaxb = JAXBContext.newInstance(xmlClasses.toArray(new Class[xmlClasses.size()]));

        XmlAdditiveOutlier xout = new XmlAdditiveOutlier();
        xout.setPosition(Day.toDay());

        XmlGenericTradingDays xtd = new XmlGenericTradingDays();
        xtd.contrasts = true;
        xtd.DayClustering = DayClustering.TD4.getGroupsDefinition();
        XmlVariables xvar = new XmlVariables();
        xvar.getVars().add(xout);
        xvar.getVars().add(xtd);
        XmlVariableWindow xwnd=new XmlVariableWindow();
        xwnd.From=new TsPeriod(TsFrequency.Yearly, 2000, 0).firstday();
        xwnd.To=new TsPeriod(TsFrequency.Yearly, 2010, 0).lastday();
        xtd.getModifiers().add(xwnd);

        try (Writer writer = Files.newBufferedWriter(Paths.get(FILE), StandardCharsets.UTF_8)) {
            Marshaller marshaller = jaxb.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(xvar, writer);
            writer.flush();
        }

        XmlVariables rslt = null;
        try (Reader reader = Files.newBufferedReader(Paths.get(FILE), StandardCharsets.UTF_8)) {
            Unmarshaller unmarshaller = jaxb.createUnmarshaller();
            rslt = (XmlVariables) unmarshaller.unmarshal(reader);
        }
        
        TsVariables vars = xvar.create();
        assertTrue(vars != null);
    }
}
