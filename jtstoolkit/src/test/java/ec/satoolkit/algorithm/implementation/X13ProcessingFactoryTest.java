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
package ec.satoolkit.algorithm.implementation;

import ec.satoolkit.benchmarking.SaBenchmarkingResults;
import ec.satoolkit.ISeriesDecomposition;
import ec.satoolkit.x11.X11Results;
import ec.tstoolkit.information.Information;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.algorithm.SingleTsData;
import ec.satoolkit.GenericSaProcessingFactory;
import ec.satoolkit.x11.SeasonalFilterOption;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.IProcessing;
import data.Data;
import ec.satoolkit.x11.Mstatistics;
import java.util.List;
import java.util.Map.Entry;
import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.IProcDocument;
import ec.tstoolkit.information.StatisticalTest;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class X13ProcessingFactoryTest {

    private CompositeResults process;

    public X13ProcessingFactoryTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    //@Test
    public void testProcessing() {
        TsData s = Data.X;
        process = X13ProcessingFactory.process(s, X13Specification.RSA5);
    }

    //@Test
    public void testOutput() {
        if (process == null) {
            testProcessing();
        }
        Map<String, Class> dictionary = process.getDictionary();
        for (Entry<String, Class> entry : dictionary.entrySet()) {
            if (entry.getValue() == TsData.class) {
                System.out.println(entry.getKey());
            }
        }

        System.out.println();

        for (Entry<String, Class> entry : dictionary.entrySet()) {
            if (entry.getValue() != TsData.class) {
                System.out.print(entry.getKey());
                System.out.print("  ");
                System.out.println(entry.getValue().getName());
            }
        }
    }

    //@Test
    public void testGeneric() {
        // Create the specifications          
        X13Specification mySpec = X13Specification.RSA4.clone();
        // Allow benchmarking
        mySpec.getBenchmarkingSpecification().setEnabled(true);
        // Create the input (for example...)
        TsData input = Data.X;

        // The long way
        // Generate the processing, using the X13 factory
        IProcessing<TsData, ?> processing = new X13ProcessingFactory().generateProcessing(mySpec, null);
        IProcResults rslts = processing.process(input);

        // The short way
        rslts = X13ProcessingFactory.process(input, mySpec);

        TsData sa = rslts.getData("sa", TsData.class);
        System.out.println(sa);
        TsData sabench = rslts.getData("benchmarking.result", TsData.class);
        System.out.println(sabench);

        // All the possible results are defined in the dictionary outof "rslts"
        Map<String, Class> dictionary = rslts.getDictionary();
        for (Entry<String, Class> entry : dictionary.entrySet()) {
            System.out.println(entry.getKey());
        }
    }

    @Test
    public void testSpecific() {

        TsData ts = Data.X;
        // Create the specifications          
        X13Specification mySpec = X13Specification.RSA4.clone();
        // Change the default test for regression variables
        mySpec.getRegArimaSpecification().getRegression().setAICCDiff(2);
        // Set the length of the Henderson filter
        mySpec.getX11Specification().setHendersonFilterLength(17);
        // Set specific filters. Just to test
        SeasonalFilterOption[] filters = new SeasonalFilterOption[ts.getFrequency().intValue()];
        for (int i = 0; i < 2; ++i) {
            filters[i] = SeasonalFilterOption.S3X15;
        }
        for (int i = 2; i < filters.length; ++i) {
            filters[i] = SeasonalFilterOption.S3X5;
        }
        mySpec.getX11Specification().setSeasonalFilters(filters);
        // Allow benchmarking
        mySpec.getBenchmarkingSpecification().setEnabled(true);

        // Process Tramo-Seats
        CompositeResults rslts = X13ProcessingFactory.process(ts, mySpec);

        // Gets the different parts of the results and some of their specific output.
        SingleTsData input = rslts.get(IProcDocument.INPUT, SingleTsData.class);
        assertTrue(input != null);
        TsData original = input.getSeries();
        PreprocessingModel regarima = rslts.get(GenericSaProcessingFactory.PREPROCESSING, PreprocessingModel.class);
        assertTrue(regarima != null);
        TsData res = regarima.getFullResiduals();
        X11Results x11 = rslts.get(X13ProcessingFactory.DECOMPOSITION, X11Results.class);
        Mstatistics mstats = rslts.get(X13ProcessingFactory.MSTATISTICS, Mstatistics.class);
        assertTrue(x11 != null);
        double q = mstats.getQ();
        List<Information<TsData>> btables = x11.getInformation().deepSelect("b*", TsData.class);
        for (Information<TsData> info : btables) {
            System.out.println(info.name);
            System.out.println(info.value);
        }
        ISeriesDecomposition finalDecomposition = rslts.get(GenericSaProcessingFactory.FINAL, ISeriesDecomposition.class);
        assertTrue(finalDecomposition != null);
        SaBenchmarkingResults bench = rslts.get(GenericSaProcessingFactory.BENCHMARKING, SaBenchmarkingResults.class);
        assertTrue(bench != null);
        TsData target = bench.getTarget();

    }

    public TsData seasadj() {
        CompositeResults rslt = X13ProcessingFactory.process(Data.X, X13Specification.RSA0);
        return rslt != null ? rslt.getData("sa", TsData.class) : null;
    }

    @Test
    public void simpleTest() {
        IProcResults rslts = X13ProcessingFactory.process(Data.X, X13Specification.RSA4);
        TsData sa = rslts.getData("sa", TsData.class);
        TsData fcasts = rslts.getData("y_f", TsData.class);
        TsData d8 = rslts.getData("decomposition.d-tables.d8", TsData.class);
        StatisticalTest lb=rslts.getData("residuals.lb", StatisticalTest.class);
        assertTrue(sa != null);
        assertTrue(fcasts != null);
        assertTrue(d8 != null);
        assertTrue(lb != null);
    }

    @Test
    public void testSeasAdj() {
        System.out.println(seasadj());
    }
}
