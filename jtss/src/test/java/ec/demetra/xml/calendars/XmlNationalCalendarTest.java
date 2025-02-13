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

import ec.tstoolkit.timeseries.DayOfWeek;
import ec.tstoolkit.timeseries.Month;
import ec.tstoolkit.timeseries.calendars.*;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.bind.*;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 *
 * @author Jean Palate
 */
public class XmlNationalCalendarTest {

    public static final String FILE = "c:\\localdata\\calendar.xml";

    public XmlNationalCalendarTest() {
    }


    @Test
    @Ignore
    public void testFile() throws JAXBException, SAXException, IOException, Exception {

        NationalCalendar cal = new NationalCalendar();
        cal.add(new FixedDay(12, Month.April));
        cal.add(new FixedWeekDay(2, DayOfWeek.Friday, Month.April));
        cal.add(new EasterRelatedDay(-12));
        cal.add(new SpecialCalendarDay(DayEvent.Ascension, 0));
        NationalCalendarProvider provider=new NationalCalendarProvider(cal);
        
        XmlNationalCalendar xcal = XmlNationalCalendar.getAdapter().marshal(provider);
        xcal.setName("calendar.test");
        XmlFixedDay fday = new XmlFixedDay();
        fday.setMonth(Month.March);
        fday.setDay(21);
        XmlSpecialDayEvent sday = new XmlSpecialDayEvent();
        sday.setDay(fday);
        xcal.getSpecialDayEvent().add(sday);
        JAXBContext jaxb = JAXBContext.newInstance(xcal.getClass());
        try (Writer writer = Files.newBufferedWriter(Paths.get(FILE), StandardCharsets.UTF_8)) {
            Marshaller marshaller = jaxb.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//            marshaller.marshal(xcal, writer);
            marshaller.marshal(new JAXBElement(QName.valueOf("test"), XmlNationalCalendar.class, xcal), writer);
            writer.flush();
        }

        ec.demetra.xml.calendars.XmlNationalCalendar rslt;
        try (Reader reader = Files.newBufferedReader(Paths.get(FILE), StandardCharsets.UTF_8)) {
            Source source = new StreamSource(reader);
            Unmarshaller unmarshaller = jaxb.createUnmarshaller();
//            rslt=(XmlNationalCalendar) unmarshaller.unmarshal(reader);
            JAXBElement<XmlNationalCalendar> jrslt =   unmarshaller.unmarshal(source, XmlNationalCalendar.class);
            rslt=jrslt.getValue();
            //           assertTrue(rslt.create().equals(tspec));
        }
        NationalCalendarProvider nprovider = XmlNationalCalendar.getAdapter().unmarshal(rslt);
    }
}
