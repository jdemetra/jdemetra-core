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
package ec.tss.xml.algorithm;

import data.Data;
import ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory;
import ec.satoolkit.algorithm.implementation.X13ProcessingFactory;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.satoolkit.x13.X13Specification;
import ec.tss.xml.information.XmlInformationSet;
import ec.tstoolkit.algorithm.BatchProcessingFactory;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.algorithm.ProcessingManager;
import ec.tstoolkit.information.InformationSet;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class XmlBatchTest {

    public XmlBatchTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

//    @Test
    public void demoTramoSeats() {
        InformationSet info = new InformationSet();
        info.set("series", Data.X);

        ProcessingManager.getInstance().register(TramoSeatsProcessingFactory.instance, TramoSeatsSpecification.class);
        ProcessingManager.getInstance().register(X13ProcessingFactory.instance, X13Specification.class);

        BatchProcessingFactory fac = new BatchProcessingFactory();
        fac.add(new BatchProcessingFactory.Node<IProcSpecification>(
                "step0",
                TramoSeatsSpecification.RSA5,
                BatchProcessingFactory.LinkType.Input,
                "series"));
        fac.add(new BatchProcessingFactory.Node<IProcSpecification>(
                "step1",
                X13Specification.RSA5,
                BatchProcessingFactory.LinkType.Result,
                "step0.sa"));

        XmlBatch xbatch = new XmlBatch();
        xbatch.input = new XmlInformationSet();
        xbatch.input.copy(info);
        xbatch.copy(fac);

        //***********************************************
        // process from xml object
        for (int i = 0; i < 100; ++i) {
            CompositeResults rslt = xbatch.create().createProcessing(null).process(xbatch.input.create());
//        System.out.println(rslt.getData("step0.sa", TsData.class));
//        System.out.println(rslt.getData("step1.sa", TsData.class));
            Map<String, Class> dictionary = rslt.getDictionary();
            System.out.println(dictionary.size());
        }
//        for (String s : dictionary.keySet()){
//            System.out.println(s);
//        }
    }
}
