/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.benchmarking.multivariate;

import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsData;
import static demetra.timeseries.simplets.TsDataToolkit.distance;
import ec.benchmarking.simplets.TsMultiBenchmarking;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import static org.junit.Assert.assertTrue;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Jean Palate
 */
public class MultivariateCholetteTest {
    
    @Test
    public void testTable() {
        
        Map<String, TsData> input = new HashMap<>();
        TsData s11 = randomM(120, 0);
        input.put("s11", s11);
        TsData s12 = randomM(120, 1);
        input.put("s12", s12);
        TsData s21 = randomM(120, 2);
        input.put("s21", s21);
        TsData s22 = randomM(120, 3);
        input.put("s22", s22);
        
        TsData s_1 = randomM(120, 4);
        input.put("s_1", s_1);
        TsData s_2 = randomM(120, 5);
        input.put("s_2", s_2);
        TsData s2_ = randomM(120, 6);
        input.put("s2_", s2_);
        
        ContemporaneousConstraint c1 = ContemporaneousConstraint.parse("s_1=s11+s21");
        ContemporaneousConstraint c2 = ContemporaneousConstraint.parse("s_2=s12+s22");
        ContemporaneousConstraint c3 = ContemporaneousConstraint.parse("s2_=s21+s22");
        TsData S22 = randomY(10, 7);
        input.put("S22", S22);
        
        TemporalConstraint c4 = TemporalConstraint.parse("S22=sum(s22)");
        MultivariateCholetteSpec.MultivariateCholetteSpecBuilder builder = MultivariateCholetteSpec.builder()
                .lambda(1)
                .rho(1)
                .contemporaneousConstraint(c1)
                .contemporaneousConstraint(c2)
                .contemporaneousConstraint(c3);
        
        MultivariateCholetteSpec spec1 = builder.build();
        
        MultivariateCholetteSpec spec2 = builder
                .lambda(.5)
                .temporalConstraint(c4)
                .build();
        
        Map<String, TsData> rslt1 = MultivariateCholette.benchmark(input, spec1);
        assertTrue(rslt1.size() == 4);

//        System.out.println(s11.values());
//        System.out.println(s12.values());
//        System.out.println(s21.values());
//        System.out.println(s22.values());
//        System.out.println(s_1.values());
//        System.out.println(s_2.values());
//        System.out.println(s2_.values());
//
//        System.out.println(rslt1.get("s11").values());
//        System.out.println(rslt1.get("s12").values());
//        System.out.println(rslt1.get("s21").values());
//        System.out.println(rslt1.get("s22").values());
        Map<String, TsData> rslt2 = MultivariateCholette.benchmark(input, spec2);
        assertTrue(rslt2.size() == 4);
        
        assertTrue(distance(s_1, TsData.add(rslt1.get("s11"), rslt1.get("s21"))) < 1e-9);
        assertTrue(distance(s_2, TsData.add(rslt1.get("s12"), rslt1.get("s22"))) < 1e-9);
        assertTrue(distance(s2_, TsData.add(rslt1.get("s21"), rslt1.get("s22"))) < 1e-9);
        assertTrue(distance(s_1, TsData.add(rslt2.get("s11"), rslt2.get("s21"))) < 1e-9);
        assertTrue(distance(s_2, TsData.add(rslt2.get("s12"), rslt2.get("s22"))) < 1e-9);
        assertTrue(distance(s2_, TsData.add(rslt2.get("s21"), rslt2.get("s22"))) < 1e-9);
    }
    
    @Test
    @Ignore
    public void testOldTable() {
        TsMultiBenchmarking bench = new TsMultiBenchmarking();
        ec.tstoolkit.timeseries.simplets.TsData s11 = oldRandomM(120, 0);
        bench.addInput("s11", s11);
        ec.tstoolkit.timeseries.simplets.TsData s12 = oldRandomM(120, 1);
        bench.addInput("s12", s12);
        ec.tstoolkit.timeseries.simplets.TsData s21 = oldRandomM(120, 2);
        bench.addInput("s21", s21);
        ec.tstoolkit.timeseries.simplets.TsData s22 = oldRandomM(120, 3);
        bench.addInput("s22", s22);
        
        ec.tstoolkit.timeseries.simplets.TsData s_1 = oldRandomM(120, 4);
        bench.addInput("s_1", s_1);
        ec.tstoolkit.timeseries.simplets.TsData s_2 = oldRandomM(120, 5);
        bench.addInput("s_2", s_2);
        ec.tstoolkit.timeseries.simplets.TsData s2_ = oldRandomM(120, 6);
        bench.addInput("s2_", s2_);
        
        bench.setLambda(0.5);
        bench.setRho(1);
        ec.benchmarking.simplets.TsMultiBenchmarking.ContemporaneousConstraintDescriptor c1 = ec.benchmarking.simplets.TsMultiBenchmarking.ContemporaneousConstraintDescriptor.parse("s_1=s11+s21");
        bench.addContemporaneousConstraint(c1);
        ec.benchmarking.simplets.TsMultiBenchmarking.ContemporaneousConstraintDescriptor c2 = ec.benchmarking.simplets.TsMultiBenchmarking.ContemporaneousConstraintDescriptor.parse("s_2=s12+s22");
        bench.addContemporaneousConstraint(c2);
        ec.benchmarking.simplets.TsMultiBenchmarking.ContemporaneousConstraintDescriptor c3 = ec.benchmarking.simplets.TsMultiBenchmarking.ContemporaneousConstraintDescriptor.parse("s2_=s21+s22");
        bench.addContemporaneousConstraint(c3);
        
        ec.tstoolkit.timeseries.simplets.TsData S22 = oldRandomY(10, 7);
        bench.addInput("S22", S22);
        
        ec.benchmarking.simplets.TsMultiBenchmarking.TemporalConstraintDescriptor c4 = ec.benchmarking.simplets.TsMultiBenchmarking.TemporalConstraintDescriptor.parse("S22=sum(s22)");
        bench.addTemporalConstraint(c4);
        bench.process();
        System.out.println("old bench");
        ec.tstoolkit.timeseries.simplets.TsDataTable table = new ec.tstoolkit.timeseries.simplets.TsDataTable();
        table.add(bench.getResult("s11"), bench.getResult("s12"), bench.getResult("s21"), bench.getResult("s22"));
        System.out.println(table);
    }
    
    private TsData randomM(int len, int seed) {
        Random rnd = new Random(seed);
        double[] data = new double[len];
        for (int i = 0; i < len; ++i) {
            data[i] = rnd.nextDouble() * 5 + 10;
        }
        return TsData.ofInternal(TsPeriod.monthly(1980, 1), data);
    }
    
    private TsData randomY(int len, int seed) {
        Random rnd = new Random(seed);
        double[] data = new double[len];
        for (int i = 0; i < len; ++i) {
            data[i] = rnd.nextDouble() * 20 + 120;
        }
        return TsData.ofInternal(TsPeriod.yearly(1980), data);
    }
    
    private ec.tstoolkit.timeseries.simplets.TsData oldRandomM(int len, int seed) {
        Random rnd = new Random(seed);
        double[] data = new double[len];
        for (int i = 0; i < len; ++i) {
            data[i] = rnd.nextDouble() * 5 + 10;
        }
        return new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1980, 0, data, false);
    }
    
    private ec.tstoolkit.timeseries.simplets.TsData oldRandomY(int len, int seed) {
        Random rnd = new Random(seed);
        double[] data = new double[len];
        for (int i = 0; i < len; ++i) {
            data[i] = rnd.nextDouble() * 20 + 120;
        }
        return new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Yearly, 1980, 0, data, false);
    }
    
}
