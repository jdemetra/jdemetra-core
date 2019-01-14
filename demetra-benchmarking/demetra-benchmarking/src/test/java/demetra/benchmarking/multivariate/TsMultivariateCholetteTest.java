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

import demetra.benchmarking.multivariate.MultivariateCholetteSpecification.ContemporaneousConstraintDescriptor;
import demetra.benchmarking.multivariate.MultivariateCholetteSpecification.TemporalConstraintDescriptor;
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
import static demetra.timeseries.simplets.TsDataToolkit.add;
import static demetra.timeseries.simplets.TsDataToolkit.add;

/**
 *
 * @author Jean Palate
 */
public class TsMultivariateCholetteTest {

    @Test
    public void testTable() {
 
        Map<String, TsData> input=new HashMap<>();
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

        MultivariateCholetteSpecification spec1=new MultivariateCholetteSpecification();
        MultivariateCholetteSpecification spec2=new MultivariateCholetteSpecification();
        spec1.setLambda(1);
        spec1.setRho(1);
        spec2.setLambda(0.5);
        spec2.setRho(1);
        ContemporaneousConstraintDescriptor c1 = ContemporaneousConstraintDescriptor.parse("s_1=s11+s21");
   ContemporaneousConstraintDescriptor c2 = ContemporaneousConstraintDescriptor.parse("s_2=s12+s22");
       ContemporaneousConstraintDescriptor c3 = ContemporaneousConstraintDescriptor.parse("s2_=s21+s22");
        spec1.setContemporaneousConstraints(new ContemporaneousConstraintDescriptor[]{c1, c2, c3});
        spec2.setContemporaneousConstraints(new ContemporaneousConstraintDescriptor[]{c1, c2, c3});
         
        TsData S22 = randomY(10, 7);
        input.put("S22", S22);

        TemporalConstraintDescriptor c4 = TemporalConstraintDescriptor.parse("S22=sum(s22)");
        spec2.setTemporalConstraints(new TemporalConstraintDescriptor[]{c4});
        
        Map<String, TsData> rslt1 = TsMultivariateCholette.benchmark(input, spec1);
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

        Map<String, TsData> rslt2 = TsMultivariateCholette.benchmark(input, spec2);
        assertTrue(rslt2.size() == 4);
        
        assertTrue(distance(s_1,add(rslt1.get("s11"), rslt1.get("s21")))<1e-9);
        assertTrue(distance(s_2,add(rslt1.get("s12"), rslt1.get("s22")))<1e-9);
        assertTrue(distance(s2_,add(rslt1.get("s21"), rslt1.get("s22")))<1e-9);
        assertTrue(distance(s_1,add(rslt2.get("s11"), rslt2.get("s21")))<1e-9);
        assertTrue(distance(s_2,add(rslt2.get("s12"), rslt2.get("s22")))<1e-9);
        assertTrue(distance(s2_,add(rslt2.get("s21"), rslt2.get("s22")))<1e-9);
    }

    @Test
    @Ignore
    public void testOldTable() {
        TsMultiBenchmarking bench=new TsMultiBenchmarking();
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
        ec.tstoolkit.timeseries.simplets.TsDataTable table=new ec.tstoolkit.timeseries.simplets.TsDataTable();
        table.add(bench.getResult("s11"), bench.getResult("s12"), bench.getResult("s21"), bench.getResult("s22"));
        System.out.println(table);
    }

    private TsData randomM(int len, int seed) {
        Random rnd=new Random(seed);
        double[] data =new double[len];
        for (int i=0; i<len; ++i){
            data[i]=rnd.nextDouble()*5+10;
        }
        return TsData.ofInternal(TsPeriod.monthly(1980, 1), data);
    }
    
    private TsData randomY(int len, int seed) {
        Random rnd=new Random(seed);
        double[] data =new double[len];
        for (int i=0; i<len; ++i){
            data[i]=rnd.nextDouble()*20+120;
        }
        return TsData.ofInternal(TsPeriod.yearly(1980), data);
    }

    private ec.tstoolkit.timeseries.simplets.TsData oldRandomM(int len, int seed) {
       Random rnd=new Random(seed);
        double[] data =new double[len];
        for (int i=0; i<len; ++i){
            data[i]=rnd.nextDouble()*5+10;
        }
        return new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1980, 0, data, false);
    }

    private ec.tstoolkit.timeseries.simplets.TsData oldRandomY(int len, int seed) {
        Random rnd=new Random(seed);
        double[] data =new double[len];
        for (int i=0; i<len; ++i){
            data[i]=rnd.nextDouble()*20+120;
        }
        return new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Yearly, 1980, 0, data, false);
    }
    
}
