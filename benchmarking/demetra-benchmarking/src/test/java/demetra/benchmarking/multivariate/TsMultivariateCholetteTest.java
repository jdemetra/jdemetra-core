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

import demetra.benchmarking.spi.MultivariateCholetteAlgorithm;
import demetra.benchmarking.multivariate.MultivariateCholetteSpecification.ContemporaneousConstraintDescriptor;
import demetra.benchmarking.multivariate.MultivariateCholetteSpecification.TemporalConstraintDescriptor;
import demetra.data.DoubleSequence;
import demetra.timeseries.RegularDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.simplets.TsData;
import ec.benchmarking.simplets.TsMultiBenchmarking;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class TsMultivariateCholetteTest {

    @Test
    public void testTable() {
 
        Map<String, TsData> input=new HashMap<>();
        TsData s11 = randomM(120);
        input.put("s11", s11);
         TsData s12 = randomM(120);
        input.put("s12", s12);
        TsData s21 = randomM(120);
        input.put("s21", s21);
        TsData s22 = randomM(120);
        input.put("s22", s22);

        TsData s_1 = randomM(120);
        input.put("s_1", s_1);
        TsData s_2 = randomM(120);
        input.put("s_2", s_2);
        TsData s2_ = randomM(120);
        input.put("s2_", s2_);

        MultivariateCholetteSpecification spec1=new MultivariateCholetteSpecification();
        MultivariateCholetteSpecification spec2=new MultivariateCholetteSpecification();
        spec2.setLambda(.8);
        spec2.setRho(.8);
        ContemporaneousConstraintDescriptor c1 = ContemporaneousConstraintDescriptor.parse("s_1=s11+s21");
        spec1.add(c1);
        spec2.add(c1);
        ContemporaneousConstraintDescriptor c2 = ContemporaneousConstraintDescriptor.parse("s_2=s12+s22");
        spec1.add(c2);
        spec2.add(c2);
        ContemporaneousConstraintDescriptor c3 = ContemporaneousConstraintDescriptor.parse("s2_=s21+s22");
        spec1.add(c3);
        spec2.add(c3);
        
        TsData S22 = randomY(10);
        input.put("S22", S22);

        TemporalConstraintDescriptor c4 = TemporalConstraintDescriptor.parse("S22=sum(s22)");
        spec2.add(c4);
        
        Map<String, TsData> rslt1 = TsMultivariateCholette.benchmark(input, spec1);
        Map<String, TsData> rslt2 = TsMultivariateCholette.benchmark(input, spec2);
        assertTrue(rslt1.size() == 4);
        assertTrue(rslt2.size() == 4);
    }

    private TsData randomM(int len) {
        Random rnd=new Random();
        double[] data =new double[len];
        for (int i=0; i<len; ++i){
            data[i]=rnd.nextDouble()*5+10;
        }
        return TsData.ofInternal(TsPeriod.monthly(1980, 1), data);
    }
    
    private TsData randomY(int len) {
        Random rnd=new Random();
        double[] data =new double[len];
        for (int i=0; i<len; ++i){
            data[i]=rnd.nextDouble()*20+120;
        }
        return TsData.ofInternal(TsPeriod.yearly(1980), data);
    }
    
}
