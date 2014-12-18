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

package ec.tstoolkit.algorithm;

import ec.tstoolkit.information.InformationSet;
import data.Data;
import ec.benchmarking.simplets.TsDenton;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 *
 * @author Jean Palate
 */
public class BatchProcessingTest {
    
    public BatchProcessingTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    //@Test
    public void test() {
        /*
         * The test will chain a simple seasonal adjustment with denton benchmarking
         * The first step produces the Sa series
         * The second step will make the benchmarking
         * The link is based on named output
         */
        BatchProcessing batch=new BatchProcessing();
        
        SequentialProcessing<TsData> sa = TramoSeatsProcessingFactory.instance.generateProcessing(TramoSeatsSpecification.RSA4, null);
        batch.add(new BatchProcessing.Node<>(
                "step0",
                new BatchProcessing.InputLink<>("series", TsData.class),
                sa
                ));
        
        IProcessing<CompositeResults, SingleTsData> denton=new IProcessing<CompositeResults, SingleTsData>() {

            @Override
            public SingleTsData process(CompositeResults input) {
                TsDenton denton =new TsDenton();
                TsData sa=input.getData("sa", TsData.class);
                TsData ycal=input.getData("ycal", TsData.class);
                TsData rslt = denton.process(sa, ycal.changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true));
                return new SingleTsData("sa_bench", rslt);
            }
        };
        
        batch.add(new BatchProcessing.Node<>(
                "step1",
                new BatchProcessing.OutputLink<>("step0", CompositeResults.class),
                denton
                ));
        
        InformationSet input=new InformationSet();
        input.set("series", Data.X);
        CompositeResults rslt = batch.process(input);
        TsData xsa=rslt.getData("step0.sa", TsData.class);
        TsData xsabench=rslt.getData("step1.sa_bench", TsData.class);
        System.out.println(xsa);
        System.out.println(xsabench);
        Map<String, Class> dictionary = rslt.getDictionary();
        for (String s: dictionary.keySet()){
            System.out.println(s);
        }
    }
}


