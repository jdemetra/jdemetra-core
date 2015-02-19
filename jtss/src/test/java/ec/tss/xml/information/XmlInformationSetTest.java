/*
 * Copyright 2013 National Bank of Belgium
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
package ec.tss.xml.information;

import data.Data;
import ec.benchmarking.simplets.Calendarization;
import ec.satoolkit.algorithm.implementation.X13ProcessingFactory;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.satoolkit.x13.X13Specification;
import ec.tss.disaggregation.documents.CalendarizationDocument;
import ec.tss.disaggregation.documents.CalendarizationSpecification;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.SequentialProcessing;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetHelper;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.Month;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author pcuser
 */
public class XmlInformationSetTest {

    public XmlInformationSetTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testXmlConversion() {
        InformationSet info = new InformationSet();
        Matrix M = new Matrix(5, 5);
        M.randomize();
        TsData ts = Data.X;
        info.add("m", M);
        info.add("ts", ts);

        XmlInformationSet xmlinfo = new XmlInformationSet();
        xmlinfo.copy(info);
        // back to information set
        InformationSet ninfo = xmlinfo.create();
        assert (info.get("m").equals(ninfo.get("m")));
        assert (info.get("ts").equals(ninfo.get("ts")));
    }

    //@Test
    public void testXmlSerialization() throws JAXBException, FileNotFoundException, IOException {
        InformationSet info = new InformationSet();
        Matrix M = new Matrix(5, 5);
        M.randomize();
        TsData ts = Data.X;
        info.add("m", M);
        info.add("ts", ts);

        XmlInformationSet xmlinfo = new XmlInformationSet();
        xmlinfo.copy(info);
        JAXBContext jaxb = JAXBContext.newInstance(XmlInformationSet.class);
        FileOutputStream stream = new FileOutputStream("d:\\test.xml");
        try (OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
            Marshaller marshaller = jaxb.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(xmlinfo, writer);
            writer.flush();
        }
    }

    //  @Test
    public void testStringsSerialization() throws JAXBException, FileNotFoundException, IOException {
        InformationSet info = new InformationSet();
        String[] strings = {"Hello", "World", "!", "TEST"};
        info.add("m", strings);

        XmlInformationSet xmlinfo = new XmlInformationSet();
        xmlinfo.copy(info);
        JAXBContext jaxb = JAXBContext.newInstance(XmlInformationSet.class);
        FileOutputStream stream = new FileOutputStream("C:\\LocalData\\test.xml");
        try (OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
            Marshaller marshaller = jaxb.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(xmlinfo, writer);
            writer.flush();
        }
    }

    // @Test
    public void testXmlSerialization2() throws JAXBException, FileNotFoundException, IOException {
        InformationSet info = new InformationSet();
        info.add("strings", new String[]{"0.3", "0.432", "93.9393", "0", "234.00", ".039484"});

        XmlInformationSet xmlinfo = new XmlInformationSet();
        xmlinfo.copy(info);
        JAXBContext jaxb = JAXBContext.newInstance(XmlInformationSet.class);
        FileOutputStream stream = new FileOutputStream("d:\\testj.xml");
        try (OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
            Marshaller marshaller = jaxb.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(xmlinfo, writer);
            writer.flush();
        }
    }

    //@Test
    public void testArrayOfDoubles() throws JAXBException, FileNotFoundException, IOException {
        InformationSet info = new InformationSet();
        info.add("doubles", new double[]{0.3, 0.432, 93.9393});
        info.add("stest", new String[]{"1", "2", "3,4"});

        XmlInformationSet xmlinfo = new XmlInformationSet();
        xmlinfo.copy(info);
        JAXBContext jaxb = JAXBContext.newInstance(XmlInformationSet.class);
        FileOutputStream stream = new FileOutputStream("d:\\testj.xml");
        try (OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
            Marshaller marshaller = jaxb.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(xmlinfo, writer);
            writer.flush();
        }
    }

//    @Test
    public void testTramoSpecSerialization() throws JAXBException, FileNotFoundException, IOException {

        InformationSet info = new InformationSet();
        TramoSpecification spec = new TramoSpecification();
        info.add("SPEC", spec.write(true));

        XmlInformationSet xmlinfo = new XmlInformationSet();
        xmlinfo.copy(info);

        JAXBContext jaxb = JAXBContext.newInstance(XmlInformationSet.class);
        FileOutputStream stream = new FileOutputStream("C:\\LocalData\\test.xml");
        try (OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
            Marshaller marshaller = jaxb.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(xmlinfo, writer);
            writer.flush();
        }
    }

//   @Test
    public void testTramoSeatsSpecSerialization() throws JAXBException, FileNotFoundException, IOException {

        InformationSet info = new InformationSet();
        TramoSeatsSpecification spec = new TramoSeatsSpecification();
        info.add(spec.toString(), spec.write(true));

        XmlInformationSet xmlinfo = new XmlInformationSet();
        xmlinfo.copy(info);

        JAXBContext jaxb = JAXBContext.newInstance(XmlInformationSet.class);
        FileOutputStream stream = new FileOutputStream("C:\\LocalData\\testTramoSeats.xml");
        try (OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
            Marshaller marshaller = jaxb.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(xmlinfo, writer);
            writer.flush();
        }
    }

    //@Test
    public void testX13SpecSerialization() throws JAXBException, FileNotFoundException, IOException {

        InformationSet info = new InformationSet();
        X13Specification spec = new X13Specification();
        info.add(spec.toString(), spec.write(true));

        XmlInformationSet xmlinfo = new XmlInformationSet();
        xmlinfo.copy(info);

        JAXBContext jaxb = JAXBContext.newInstance(XmlInformationSet.class);
        FileOutputStream stream = new FileOutputStream("d:\\test.xml");
        try (OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
            Marshaller marshaller = jaxb.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(xmlinfo, writer);
            writer.flush();
        }
    }

    //@Test
    public void testCalendarizationSerialization() throws JAXBException, FileNotFoundException, IOException {

        InformationSet info = new InformationSet();
        CalendarizationDocument doc = new CalendarizationDocument();
        CalendarizationSpecification spec = new CalendarizationSpecification();
        spec.setAggFrequency(TsFrequency.Monthly);
        spec.setWeights(new double[]{1, 1, 1, 1, 1, 1, 1});
        doc.setSpecification(spec);
        List<Calendarization.PeriodObs> observations = new ArrayList<>();
        observations.add(new Calendarization.PeriodObs(new Day(2009, Month.February, 17), new Day(2009, Month.March, 16), 9000));
        observations.add(new Calendarization.PeriodObs(new Day(2009, Month.March, 17), new Day(2009, Month.April, 13), 5000));
        observations.add(new Calendarization.PeriodObs(new Day(2009, Month.April, 14), new Day(2009, Month.May, 11), 9500));
        observations.add(new Calendarization.PeriodObs(new Day(2009, Month.May, 12), new Day(2009, Month.June, 8), 7000));
        doc.setInput(observations);

        info.add("calendarization", doc.write(true));

        XmlInformationSet xmlinfo = new XmlInformationSet();
        xmlinfo.copy(info);

        JAXBContext jaxb = JAXBContext.newInstance(XmlInformationSet.class);
        FileOutputStream stream = new FileOutputStream("C:\\LocalData\\test.xml");
        try (OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
            Marshaller marshaller = jaxb.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(xmlinfo, writer);
            writer.flush();
        }
    }

    //@Test
    public void testCalendarizationUnSerialization() throws JAXBException, FileNotFoundException, IOException {

        JAXBContext jaxb = JAXBContext.newInstance(XmlInformationSet.class);
        FileInputStream stream = new FileInputStream("C:\\LocalData\\test.xml");
        XmlInformationSet xmlinfo = (XmlInformationSet) jaxb.createUnmarshaller().unmarshal(stream);
        InformationSet set = xmlinfo.create();
        set = set.get("calendarization", InformationSet.class);
        CalendarizationDocument doc = new CalendarizationDocument();
        doc.read(set);
    }

    //@Test
    public void testCompositeResults() throws JAXBException, FileNotFoundException, IOException {
        // X13 processing
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(X13Specification.RSA4);
        CompositeResults rslt = processing.process(Data.P);

        // complete serialization
        InformationSet info = InformationSetHelper.fromProcResults(rslt);
        XmlInformationSet xmlinfo = new XmlInformationSet();
        xmlinfo.copy(info);

        JAXBContext jaxb = JAXBContext.newInstance(XmlInformationSet.class);
        FileOutputStream stream = new FileOutputStream("C:\\LocalData\\testx13.xml");
        try (OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
            Marshaller marshaller = jaxb.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(xmlinfo, writer);
            writer.flush();
        }

        // partial serialization. only the main decomposition and the mstatistics
        String[] filters = new String[]{"s", "sa", "t", "i", "s_f", "sa_f", "t_f", "i_f", "mstat*"};
        HashSet<String> set = new HashSet<>(Arrays.asList(filters));
        InformationSet infop = InformationSetHelper.fromProcResults(rslt, set);
        XmlInformationSet xmlinfop = new XmlInformationSet();
        xmlinfop.copy(infop);

        stream = new FileOutputStream("C:\\LocalData\\testx13p.xml");
        try (OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
            Marshaller marshaller = jaxb.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(xmlinfop, writer);
            writer.flush();
        }
    }
}
