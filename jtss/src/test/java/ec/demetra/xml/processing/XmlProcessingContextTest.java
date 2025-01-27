/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.demetra.xml.processing;

import data.Data;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.Month;
import ec.tstoolkit.timeseries.calendars.*;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.utilities.WeightedItem;
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
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class XmlProcessingContextTest {

    private static final String FILE = "c:\\localdata\\context.xml";
    private static final ProcessingContext context;
    
    static{
        context=new ProcessingContext();
        TsVariables vars=new TsVariables();
        vars.set("prod", new TsVariable(Data.P));
        context.getTsVariableManagers().set("test", vars);
        NationalCalendar cal1 = new NationalCalendar();
        FixedDay fday = new FixedDay(20, Month.March);
        cal1.add(fday);

        NationalCalendar cal2 = new NationalCalendar();
        EasterRelatedDay eday=new  EasterRelatedDay(-10);
        cal2.add(eday);
        context.getGregorianCalendars().set("cal1", new NationalCalendarProvider(cal1));
        context.getGregorianCalendars().set("cal2", new NationalCalendarProvider(cal2));

        ChainedGregorianCalendarProvider chained = new ChainedGregorianCalendarProvider("cal1", Day.toDay(), "cal2");
        context.getGregorianCalendars().set("chained", chained);

        CompositeGregorianCalendarProvider composite = new CompositeGregorianCalendarProvider();
        composite.add(new WeightedItem<>("cal1"));
        composite.add(new WeightedItem<>("cal2"));
        composite.add(new WeightedItem<>("chained", .5));
        context.getGregorianCalendars().set("composite", composite);
    }
    
    public XmlProcessingContextTest() {
    }

    @Test
    @Ignore
    public void testFile() throws FileNotFoundException, JAXBException, IOException {

        XmlProcessingContext xcnt = new XmlProcessingContext();
        XmlProcessingContext.MARSHALLER.marshal(context, xcnt);
        JAXBContext jaxb = JAXBContext.newInstance(xcnt.getClass());
        try (Writer writer = Files.newBufferedWriter(Paths.get(FILE), StandardCharsets.UTF_8)) {
            Marshaller marshaller = jaxb.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(xcnt, writer);
            writer.flush();
        }

        XmlProcessingContext rslt = null;
        try (Reader reader = Files.newBufferedReader(Paths.get(FILE), StandardCharsets.UTF_8)) {
            Unmarshaller unmarshaller = jaxb.createUnmarshaller();
            rslt = (XmlProcessingContext) unmarshaller.unmarshal(reader);
            ProcessingContext ncnt = new ProcessingContext();
            XmlProcessingContext.UNMARSHALLER.unmarshal(rslt, ncnt);
        }
    }

    @Test
    public void testValidation() throws FileNotFoundException, JAXBException, IOException, SAXException {

        XmlProcessingContext xcnt = new XmlProcessingContext();
        XmlProcessingContext.MARSHALLER.marshal(context, xcnt);
        JAXBContext jaxb = JAXBContext.newInstance(xcnt.getClass());
        JAXBSource source = new JAXBSource(jaxb, xcnt);
        Validator validator = Schemas.Processing.newValidator();
        //validator.setErrorHandler(new TestErrorHandler());
        validator.validate(source);
    }

}