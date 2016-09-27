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

import ec.demetra.xml.core.XmlParameter;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.DayClustering;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
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
import xml.TestValidationEventHandler;

/**
 *
 * @author Jean Palate
 */
public class XmlRegressionTest {

    private static final String FILE = "c:\\localdata\\regression.xml";

    private XmlRegression reg;

    public XmlRegressionTest() {
        // create the regression
        XmlAdditiveOutlier xao = new XmlAdditiveOutlier();
        xao.setPosition(Day.toDay());
        XmlSeasonalOutlier xso = new XmlSeasonalOutlier();
        xso.setPosition(Day.toDay().minus(5000));

        reg = new XmlRegression();
        XmlRegressionItem ao = new XmlRegressionItem();
        ao.variable = xao;
        ao.coefficient= new XmlParameter(1000, ParameterType.Fixed);
        reg.getItems().add(ao);
        XmlRegressionItem so = new XmlRegressionItem();
        so.variable = xso;
        reg.getItems().add(so);

        XmlGenericTradingDays xtd = new XmlGenericTradingDays();
        xtd.contrasts = true;
        xtd.DayClustering = DayClustering.TD4.getGroupsDefinition();
        XmlVariableWindow xwnd = new XmlVariableWindow();
        xwnd.From = new TsPeriod(TsFrequency.Yearly, 2000, 0).firstday();
        xwnd.To = new TsPeriod(TsFrequency.Yearly, 2010, 0).lastday();
        xtd.getModifiers().add(xwnd);

        XmlRegressionItem td = new XmlRegressionItem();
        td.variable = xtd;
        reg.getItems().add(td);
    }

    @Test
    @Ignore
    public void testMarshal() throws FileNotFoundException, JAXBException, IOException {

        JAXBContext jaxb = XmlRegression.context();
        FileOutputStream ostream = new FileOutputStream(FILE);
        try (OutputStreamWriter writer = new OutputStreamWriter(ostream, StandardCharsets.UTF_8)) {
            Marshaller marshaller = jaxb.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(reg, writer);
            writer.flush();
        }

        XmlRegression rslt = null;
        FileInputStream istream = new FileInputStream(FILE);
        try (InputStreamReader reader = new InputStreamReader(istream, StandardCharsets.UTF_8)) {
            Unmarshaller unmarshaller = jaxb.createUnmarshaller();
            unmarshaller.setSchema(Schemas.Modelling);
            unmarshaller.setEventHandler(new TestValidationEventHandler());
            rslt = (XmlRegression) unmarshaller.unmarshal(reader);
            for (XmlRegressionItem item : rslt.getItems()){
                ITsVariable tsvar = item.variable.toTsVariable();
                assertTrue(tsvar != null);
            }
        }
    }

    @Test
    public void testValidation() throws FileNotFoundException, JAXBException, IOException, SAXException {

        JAXBContext jaxb = XmlRegression.context();
        JAXBSource source = new JAXBSource(jaxb, reg);
        Validator validator = Schemas.Modelling.newValidator();
        //validator.setErrorHandler(new TestErrorHandler());
        validator.validate(source);
    }
}
