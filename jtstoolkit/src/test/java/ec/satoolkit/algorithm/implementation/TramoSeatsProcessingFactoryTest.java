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

import data.Data;
import ec.satoolkit.GenericSaProcessingFactory;
import ec.satoolkit.ISeriesDecomposition;
import ec.satoolkit.benchmarking.SaBenchmarkingResults;
import ec.satoolkit.seats.SeatsResults;
import ec.satoolkit.seats.SeatsSpecification.EstimationMethod;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.IProcDocument;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.IProcessing;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.algorithm.SequentialProcessing;
import ec.tstoolkit.algorithm.SingleTsData;
import ec.tstoolkit.arima.Spectrum;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.StatisticalTest;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.EasterSpec.Type;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.ucarima.UcarimaModel;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class TramoSeatsProcessingFactoryTest {

    private CompositeResults process;

    public TramoSeatsProcessingFactoryTest() {
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
        process = TramoSeatsProcessingFactory.process(s, TramoSeatsSpecification.RSA4);
        TsData sa = process.getData("sa", TsData.class);

        SeatsResults seats = process.get("decomposition", SeatsResults.class);
        UcarimaModel ucm = seats.getUcarimaModel();
        Spectrum spectrum = ucm.getComponent(0).getSpectrum();
        double std = spectrum.get(2.19);
    }

    public TsData seasadj() {
        CompositeResults rslt = TramoSeatsProcessingFactory.process(Data.X, TramoSeatsSpecification.RSA0);
        return rslt != null ? rslt.getData("sa", TsData.class) : null;
    }

    //@Test
    public void testSeasAdj() {
        System.out.println(seasadj());
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
                System.out.println(entry.getKey());
            }
        }
        System.out.println();
        System.out.println(process.getData("s", TsData.class));
    }

    @Test
    public void testGeneric() {
        // Create the specifications          
        TramoSeatsSpecification mySpec = TramoSeatsSpecification.RSA4.clone();
        // Allow benchmarking
        mySpec.getBenchmarkingSpecification().setEnabled(true);
        // Create the ts (for example...)
        TsData input = Data.X;

        // The long way
        // Generate the processing, using the Tramo-Seats factory
        IProcessing<TsData, ?> processing = new TramoSeatsProcessingFactory().generateProcessing(mySpec, null);
        IProcResults rslts = processing.process(input);

        // The short way
        rslts = TramoSeatsProcessingFactory.process(input, mySpec);

        TsData sa = rslts.getData("sa", TsData.class);
        System.out.println(sa);
        TsData sabench = rslts.getData("benchmarking.result", TsData.class);
        System.out.println(sabench);
        StatisticalTest skewness = rslts.getData("residuals.skewness", StatisticalTest.class);
        System.out.println(skewness);
        // All the possible results are defined in the dictionary of "rslts"
//        Map<String, Class> dictionary = rslts.getDictionary();
//        for (Entry<String, Class> entry : dictionary.entrySet()) {
//            System.out.println(entry.getKey());
//        }
    }

    //@Test
    public void testSpecific() {
        // Create the specifications          
        TramoSeatsSpecification mySpec = TramoSeatsSpecification.RSA4.clone();
        // New Easter variable
        mySpec.getTramoSpecification().getRegression().getCalendar().getEaster().setOption(Type.IncludeEasterMonday);
        // Automatic detection of the "best" td specification (working days or trading days)
        mySpec.getTramoSpecification().getRegression().getCalendar().getTradingDays().setAutomatic(true);
        // Kalman smoother estimates
        mySpec.getSeatsSpecification().setMethod(EstimationMethod.KalmanSmoother);
        // Allow benchmarking
        mySpec.getBenchmarkingSpecification().setEnabled(true);

        // Process Tramo-Seats
        TsData ts = Data.X;
        CompositeResults rslts = TramoSeatsProcessingFactory.process(ts, mySpec);

        // Gets the different parts of the results and some of their specific output.
        SingleTsData input = rslts.get(IProcDocument.INPUT, SingleTsData.class);
        assertTrue(input != null);
        TsData original = input.getSeries();
        PreprocessingModel regarima = rslts.get(GenericSaProcessingFactory.PREPROCESSING, PreprocessingModel.class);
        assertTrue(regarima != null);
        TsData res = regarima.getFullResiduals();
        SeatsResults seats = rslts.get(GenericSaProcessingFactory.DECOMPOSITION, SeatsResults.class);
        assertTrue(seats != null);
        UcarimaModel ucm = seats.getUcarimaModel();
        ISeriesDecomposition finalDecomposition = rslts.get(GenericSaProcessingFactory.FINAL, ISeriesDecomposition.class);
        assertTrue(finalDecomposition != null);
        SaBenchmarkingResults bench = rslts.get(GenericSaProcessingFactory.BENCHMARKING, SaBenchmarkingResults.class);
        assertTrue(bench != null);
        TsData target = bench.getTarget();
    }

    //@Test
    public void testAirline() {
        // Create the specifications          
        TramoSeatsSpecification mySpec = TramoSeatsSpecification.RSA0.clone();
        int n = 10000;
        // Compute random monthly airline series (with th =-.6 and bth=-.8)
        List<TsData> rnd = Data.rndAirlines(n, 90, -.6, -.8);
        double[] th = new double[n], bth = new double[n];
        // Create the processing. The same processing will be used for all series
        IProcessing<TsData, ?> processing = new TramoSeatsProcessingFactory().generateProcessing(mySpec, null);
        for (int i = 0; i < n; ++i) {
            // Estimate the model for each radom series. Retrieve the parameters of the model
            IProcResults rslts = processing.process(rnd.get(i));
            th[i] = rslts.getData("arima.th(1)", Parameter.class).getValue();
            bth[i] = rslts.getData("arima.bth(1)", Parameter.class).getValue();
        }

        DescriptiveStatistics TH = new DescriptiveStatistics(th);
        DescriptiveStatistics BTH = new DescriptiveStatistics(bth);
        System.out.println();
        double step = .005;
        for (int i = 0; i < 100; ++i) {
            double l = -.5 - i * step;
            double h = l - step;
            System.out.print(TH.countBetween(h, l));
            System.out.print(" ");
            System.out.println(BTH.countBetween(h, l));
        }
    }

    //@Test
    public void testPerformances() {
        TsData s = Data.P;
        int nproc = 100;
        CompositeResults rslts = TramoSeatsProcessingFactory.process(s, TramoSeatsSpecification.RSA5);
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < nproc; ++i) {
            rslts = TramoSeatsProcessingFactory.process(s, TramoSeatsSpecification.RSA5);
        }
        long t1 = System.currentTimeMillis();
        System.out.println();
        System.out.println("RSA0 processing");
        System.out.println(t1 - t0);
        System.out.println();
        System.out.println(rslts.getData("sa", TsData.class));
    }

    //@Test
    public void testInsee() {
        TsData series = data.Data.M1;
        TsData reg1 = data.Data.M2;
        TsVariable variable = new TsVariable("reg1", reg1);
        TsVariables variables = new TsVariables();
        variables.set("reg1", variable);
        ProcessingContext proccontext = ProcessingContext.getActiveContext();
        proccontext.getTsVariableManagers().set("uservar", variables);
        TramoSeatsSpecification specification = TramoSeatsSpecification.RSA5.clone();
        TsVariableDescriptor descriptor = new TsVariableDescriptor("uservar.reg1");
        specification.getTramoSpecification().getRegression().setUserDefinedVariables(new TsVariableDescriptor[]{descriptor});

        TramoSeatsProcessingFactory factory = TramoSeatsProcessingFactory.instance;
        SequentialProcessing<TsData> sequence = factory.generateProcessing(specification, proccontext);
        IProcResults results = sequence.process(series);
        //InformationSet info = ec.tstoolkit.information.InformationSetHelper.fromProcResults(results);
        System.out.println("INSEE");
        //System.out.println(results.getData("sa", TsData.class));
        Map<String, Class> dictionary = results.getDictionary();
        System.out.println(dictionary);
//        for (Entry<String, Class> entry : dictionary.entrySet()) {
//            System.out.print(entry.getKey());
//            System.out.print('\t');
//            System.out.println(entry.getValue().getCanonicalName());
//        }
    }

//    @Test
    public void testMissing() {
        TramoSeatsSpecification xspec = TramoSeatsSpecification.RSA0.clone();
        xspec.getTramoSpecification().getEstimate().setTol(1e-15);
        CompositeResults r1 = TramoSeatsProcessingFactory.process(Data.P, xspec);
        PreprocessingModel p1 = r1.get("preprocessing", PreprocessingModel.class);
        System.out.println(p1.estimation.getArima());
        CompositeResults r2 = TramoSeatsProcessingFactory.process(Data.P.extend(50, 50), xspec);
        PreprocessingModel p2 = r2.get("preprocessing", PreprocessingModel.class);
        System.out.println(p2.estimation.getArima());
    }
}
