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
package ec.demetra.xml.calendars;

import ec.demetra.xml.core.XmlWeightedItem;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.Month;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;
import xml.Schemas;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 *
 * @author Jean Palate
 */
public class XmlCalendarsTest {

    public static final String FILE = "c:\\localdata\\calendars.xml";

    public XmlCalendarsTest() {
    }

    @Test
    public void testValidation() throws JAXBException, SAXException, IOException {

        XmlNationalCalendar xcal1 = new XmlNationalCalendar();
        xcal1.setName("calendar.test1");
        XmlFixedDay fday = new XmlFixedDay();
        fday.setMonth(Month.March);
        fday.setDay(21);
        XmlSpecialDayEvent sday = new XmlSpecialDayEvent();
        sday.setDay(fday);
        xcal1.getSpecialDayEvent().add(sday);

        XmlNationalCalendar xcal2 = new XmlNationalCalendar();
        xcal2.setName("calendar.test2");
        xcal2.getSpecialDayEvent().add(sday);

        XmlChainedCalendar xchained = new XmlChainedCalendar();
        xchained.setCalendarBreak(Day.toDay());
        xchained.setStartCalendar(xcal1.getName());
        xchained.setEndCalendar(xcal2.getName());
        xchained.setName("calendar.chained");

        XmlCompositeCalendar xcomposite = new XmlCompositeCalendar();
        xcomposite.getWeightedCalendar().add(new XmlWeightedItem(xcal1.getName()));
        xcomposite.getWeightedCalendar().add(new XmlWeightedItem(xcal2.getName()));
        xcomposite.getWeightedCalendar().add(new XmlWeightedItem(xchained.getName()));
        xcomposite.setName("calendar.composite");

        XmlCalendars xcals = new XmlCalendars();
        xcals.getCalendars().add(xcal1);
        xcals.getCalendars().add(xcal2);
        xcals.getCalendars().add(xchained);
        xcals.getCalendars().add(xcomposite);

        JAXBContext jaxb = JAXBContext.newInstance(xcals.getClass());
        JAXBSource source = new JAXBSource(jaxb, xcals);
        Validator validator = Schemas.Calendars.newValidator();
        //validator.setErrorHandler(new TestErrorHandler());
        validator.validate(source);
    }

    @Test
    @Ignore
    public void testFile() throws JAXBException, SAXException, IOException, Exception {

        XmlNationalCalendar xcal1 = new XmlNationalCalendar();
        xcal1.setName("calendar.test1");
         XmlFixedDay fday = new XmlFixedDay();
        fday.setMonth(Month.March);
        fday.setDay(21);
        XmlSpecialDayEvent sday = new XmlSpecialDayEvent();
        sday.setDay(fday);
        xcal1.getSpecialDayEvent().add(sday);

        XmlNationalCalendar xcal2 = new XmlNationalCalendar();
        xcal2.setName("calendar.test2");
        xcal2.getSpecialDayEvent().add(sday);

        XmlChainedCalendar xchained = new XmlChainedCalendar();
        xchained.setCalendarBreak(Day.toDay());
        xchained.setStartCalendar(xcal1.getName());
        xchained.setEndCalendar(xcal2.getName());
        xchained.setName("calendar.chained");

        XmlCompositeCalendar xcomposite = new XmlCompositeCalendar();
        xcomposite.getWeightedCalendar().add(new XmlWeightedItem(xcal1.getName()));
        xcomposite.getWeightedCalendar().add(new XmlWeightedItem(xcal2.getName()));
        xcomposite.getWeightedCalendar().add(new XmlWeightedItem(xchained.getName()));
        xcomposite.setName("calendar.composite");

        XmlCalendars xcals = new XmlCalendars();
        xcals.getCalendars().add(xcal1);
        xcals.getCalendars().add(xcal2);
        xcals.getCalendars().add(xchained);
        xcals.getCalendars().add(xcomposite);

        JAXBContext jaxb = JAXBContext.newInstance(xcals.getClass());
        try (Writer writer = Files.newBufferedWriter(Paths.get(FILE), StandardCharsets.UTF_8)) {
            Marshaller marshaller = jaxb.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(xcals, writer);
//            marshaller.marshal(new JAXBElement(QName.valueOf("test"), XmlNationalCalendar.class, xcal), writer);
            writer.flush();
        }
    }
}
