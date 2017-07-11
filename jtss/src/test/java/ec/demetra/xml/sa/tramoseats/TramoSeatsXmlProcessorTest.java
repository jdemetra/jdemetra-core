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
package ec.demetra.xml.sa.tramoseats;

import data.Data;
import ec.demetra.xml.core.XmlInformationSet;
import ec.demetra.xml.core.XmlTs;
import ec.demetra.xml.core.XmlTsData;
import ec.demetra.xml.processing.XmlProcessingContext;
import ec.demetra.xml.regression.XmlRegression;
import ec.demetra.xml.regression.XmlRegressionItem;
import ec.demetra.xml.regression.XmlStaticTsVariable;
import ec.demetra.xml.regression.XmlTsVariables;
import ec.demetra.xml.regression.XmlUserVariable;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.timeseries.Month;
import ec.tstoolkit.timeseries.calendars.DayEvent;
import ec.tstoolkit.timeseries.calendars.FixedDay;
import ec.tstoolkit.timeseries.calendars.NationalCalendar;
import ec.tstoolkit.timeseries.calendars.NationalCalendarProvider;
import ec.tstoolkit.timeseries.calendars.SpecialCalendarDay;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class TramoSeatsXmlProcessorTest {

    public TramoSeatsXmlProcessorTest() {
    }

    @Test
    public void testTramoRequest() {
        XmlTramoRequest request = new XmlTramoRequest();
        request.defaultSpecification = "TRfull";
        request.series = new XmlTs();
        XmlTsData.MARSHALLER.marshal(Data.X, request.series);
        request.getOutputFilter().add("arima.*");
        request.getOutputFilter().add("likelihood.*");
        request.getOutputFilter().add("residuals.*");
        request.getOutputFilter().add("*_f");
        TramoSeatsXmlProcessor processor = new TramoSeatsXmlProcessor();
        XmlInformationSet rslt = processor.process(request);

        InformationSet all = rslt.create();
//        List<String> dictionary = all.getDictionary();
//        for (String s: dictionary){
//            System.out.println(s);
//        }
        assertTrue(all.get("y_f", TsData.class) != null);
    }

    @Test
    public void testTramoRequests() {
//        long t0 = System.currentTimeMillis();
        int N = 10;
        XmlTramoRequests requests = new XmlTramoRequests();
        for (int i = 0; i < N; ++i) {
            XmlTramoAtomicRequest cur = new XmlTramoAtomicRequest();
            cur.defaultSpecification = "TRfull";
            cur.series = new XmlTs();
            XmlTsData.MARSHALLER.marshal(Data.P, cur.series);
            requests.getItems().add(cur);
        }
        //requests.getOutputFilter().add("arima.*");
        requests.getOutputFilter().add("likelihood.*");
        //requests.getOutputFilter().add("residuals.*");
        //requests.getOutputFilter().add("*_f");
        TramoSeatsXmlProcessor processor = new TramoSeatsXmlProcessor();
        XmlInformationSet rslt = processor.process(requests);
//        long t1 = System.currentTimeMillis();
//        System.out.println(t1 - t0);

        InformationSet all = rslt.create();
        for (int i = 0; i < N; ++i) {
            assertTrue(null != all.search("series" + (i + 1) + ".likelihood.aic", Double.class));
        }
    }

    @Test
    public void testTramoSeatsRequests() {
//        long t0 = System.currentTimeMillis();
        int N = 10;
        XmlTramoSeatsRequests requests = new XmlTramoSeatsRequests();
        requests.setFlat(true);
        for (int i = 0; i < N; ++i) {
            XmlTramoSeatsAtomicRequest cur = new XmlTramoSeatsAtomicRequest();
            cur.defaultSpecification = "RSAfull";
            cur.series = new XmlTs();
            XmlTsData.MARSHALLER.marshal(Data.P, cur.series);
            requests.getItems().add(cur);
        }
        TramoSeatsXmlProcessor processor = new TramoSeatsXmlProcessor();
        XmlInformationSet rslt = processor.process(requests);
//        long t1 = System.currentTimeMillis();
//        System.out.println(t1 - t0);
        InformationSet all = rslt.create();
        for (int i = 0; i < N; ++i) {
            assertTrue(null != all.search("series" + (i + 1) + ".final.sa", TsData.class));
        }
    }

    @Test
    public void testAdvancedRequests() {
//        long t0 = System.currentTimeMillis();
        int N = 10;
        XmlTramoSeatsRequests requests = new XmlTramoSeatsRequests();
        requests.setFlat(true);
        for (int i = 0; i < N; ++i) {
            XmlTramoSeatsAtomicRequest cur = new XmlTramoSeatsAtomicRequest();
            cur.specification=advanced();
            cur.series = new XmlTs();
            XmlTsData.MARSHALLER.marshal(Data.P, cur.series);
            requests.getItems().add(cur);
        }
        requests.context=context();
        TramoSeatsXmlProcessor processor = new TramoSeatsXmlProcessor();
        requests.setParallelProcessing(false);
        XmlInformationSet rslt = processor.process(requests);
//        long t1 = System.currentTimeMillis();
//        System.out.println(t1 - t0);
        InformationSet all = rslt.create();
        for (int i = 0; i < N; ++i) {
            assertTrue(null != all.search("series" + (i + 1) + ".final.sa", TsData.class));
        }
    }
    
    @Test
    public void testAdvancedRequest() {
        XmlTramoSeatsRequest request = new XmlTramoSeatsRequest();
        request.specification = TramoSeatsXmlProcessorTest.advanced();
        request.context=TramoSeatsXmlProcessorTest.context();
        request.series = new XmlTs();
        XmlTsData.MARSHALLER.marshal(Data.X, request.series);
        request.getOutputFilter().add("regression.*");
         TramoSeatsXmlProcessor processor = new TramoSeatsXmlProcessor();
         XmlInformationSet rslt = processor.process(request);
         assertTrue(rslt != null);
    }
    static XmlTramoSeatsSpecification advanced(){
        TramoSeatsSpecification spec=TramoSeatsSpecification.RSAfull.clone();
        spec.getTramoSpecification().getRegression().getCalendar().getTradingDays().setHolidays("Belgium");
        XmlTramoSeatsSpecification xml=new XmlTramoSeatsSpecification();
        XmlTramoSeatsSpecification.MARSHALLER.marshal(spec, xml);
        ec.demetra.xml.sa.x13.XmlRegressionSpec regression = new ec.demetra.xml.sa.x13.XmlRegressionSpec();
        XmlRegression variables = new XmlRegression();
        List<XmlRegressionItem> items = variables.getItems();
        XmlRegressionItem xvar = new XmlRegressionItem();
        XmlUserVariable xuser = new XmlUserVariable();
        xuser.setVariable("vars.reg1");
        xuser.setEffect(TsVariableDescriptor.UserComponentType.Irregular);
        xvar.setVariable(xuser);
        items.add(xvar);
        regression.setVariables(variables);
        XmlRegressionSpec rspec=new XmlRegressionSpec();
        rspec.setVariables(variables);
        xml.preprocessing.setRegression(rspec);
        return xml;
    }
    
    static XmlProcessingContext context(){
        ProcessingContext context=new ProcessingContext();
        NationalCalendar calendar=new NationalCalendar();
        calendar.add(new FixedDay(20, Month.July));
        calendar.add(new FixedDay(10, Month.October));
        calendar.add(new SpecialCalendarDay(DayEvent.NewYear, 0));
        calendar.add(new SpecialCalendarDay(DayEvent.EasterMonday, 0));
        calendar.add(new SpecialCalendarDay(DayEvent.Ascension, 0));
        calendar.add(new SpecialCalendarDay(DayEvent.WhitMonday, 0));
        calendar.add(new SpecialCalendarDay(DayEvent.MayDay, 0));
        calendar.add(new SpecialCalendarDay(DayEvent.Assumption, 0));
        calendar.add(new SpecialCalendarDay(DayEvent.AllSaintsDay, 0));
        calendar.add(new SpecialCalendarDay(DayEvent.Christmas, 0));
        context.getGregorianCalendars().set("Belgium", new NationalCalendarProvider(calendar));
        XmlProcessingContext xc=new XmlProcessingContext();
        XmlProcessingContext.MARSHALLER.marshal(context, xc);
        XmlStaticTsVariable var = new XmlStaticTsVariable();
        var.setName("reg1");
        XmlTsData data = new XmlTsData();
        double[] r = new double[600];
        Random rnd = new Random();
        for (int i = 0; i < r.length; ++i) {
            r[i] = rnd.nextDouble();
        }
        data.setValues(r);
        data.setFrequency(12);
        data.setFirstPeriod(1);
        data.setFirstYear(1960);
        var.setTsData(data);
        XmlTsVariables xvars = new XmlTsVariables();
        xvars.setName("vars");
        xvars.getVariables().add(var);
        XmlProcessingContext.Variables vars = new XmlProcessingContext.Variables();
        vars.getGroup().add(xvars);
        xc.setVariables(vars);
        return xc;
    }
}
