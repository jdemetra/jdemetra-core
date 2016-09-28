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
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataTable;
import java.util.List;
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
    public void testTramoSeatsRequest() {
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
            assertTrue(null != all.search("series" + (i + 1) + ".sa", TsData.class));
        }
    }
}
