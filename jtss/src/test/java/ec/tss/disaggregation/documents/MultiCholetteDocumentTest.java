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

package ec.tss.disaggregation.documents;

import ec.tss.Ts;
import ec.tss.TsFactory;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class MultiCholetteDocumentTest {

    public MultiCholetteDocumentTest() {
    }

    @Test
    public void testContemporaneous() {
        int N = 10;
        Matrix M = new Matrix(240, N * N);
        M.randomize();
        Ts[] s = new Ts[N * N];
        TsVariables vars=new TsVariables();
        TsPeriod start = new TsPeriod(TsFrequency.Monthly, 1980, 0);
        for (int i = 0; i < N * N; ++i) {
            TsData data=new TsData(start, M.column(i));
            vars.set("s" + i , new TsVariable(data));
        }
        // create the specs
        MultiCholetteSpecification spec = new MultiCholetteSpecification();
        // put unbinding constraints
        for (int i = 0; i < N; ++i) {
            StringBuilder str=new StringBuilder();
            str.append(.5 * N).append('=');
            int[] ids = new int[N];
            for (int j = 0; j < N; ++j) {
                str.append("+s").append(i + N * j);
            }
            spec.addConstraint(str.toString());
        }
        for (int i = 0; i < N - 1; ++i) {
            StringBuilder str=new StringBuilder();
            str.append(.5 * N).append('=');
            for (int j = 0; j < N; ++j) {
                str.append("+s").append(j + N * i);
            }
            spec.addConstraint(str.toString());
        }
        spec.addConstraint("0=s2+2*s6-s10-s18");
        spec.addConstraint("0=s4+2*s11-s18");

        //spec.getParameters().setRho(.8);
        MultiCholetteDocument doc = new MultiCholetteDocument();
        
        doc.setInput(vars);
        doc.setSpecification(spec);
        MultiBenchmarkingResults results = doc.getResults();
        for (int j = 0; j < N; ++j) {
            TsData S = null;
            for (int i = 0; i < N; ++i) {
                S = TsData.add(S, results.getData("benchmarked.s" + (i + N * j), TsData.class));
            }
            DescriptiveStatistics stat = new DescriptiveStatistics(S);
            assertTrue(Math.abs(stat.getAverage() - .5 * N) < 1e-6);
            assertTrue(stat.getStdev() < 1e-9);
//            System.out.println(stat.getStdev());
        }
        for (int j = 0; j < N; ++j) {
            TsData S = null;
            for (int i = 0; i < N; ++i) {
                S = TsData.add(S, results.getData("benchmarked.s" + (N * i + j), TsData.class));
            }
            DescriptiveStatistics stat = new DescriptiveStatistics(S);
            assertTrue(Math.abs(stat.getAverage() - .5 * N) < 1e-6);
            assertTrue(stat.getStdev() < 1e-9);
//            System.out.println(stat.getStdev());
        }
        TsData S = results.getData("benchmarked.s2", TsData.class);
        S = TsData.add(S, results.getData("benchmarked.s6", TsData.class).times(2));
        S = TsData.subtract(S, results.getData("benchmarked.s10", TsData.class));
        S = TsData.subtract(S, results.getData("benchmarked.s18", TsData.class));

        DescriptiveStatistics stat = new DescriptiveStatistics(S);
        assertTrue(Math.abs(stat.getAverage()) < 1e-6);
        assertTrue(stat.getStdev() < 1e-9);

//        System.out.println();
//        System.out.println(stat.getAverage());
//        System.out.println(stat.getStdev());
//
//        System.out.println();
//        System.out.println(results.getData("benchmarked.s8", TsData.class));
    }

    @Test
    public void testRedundantContemporaneous() {
         int N = 10;
        Matrix M = new Matrix(240, N * N);
        M.randomize();
        Ts[] s = new Ts[N * N];
        TsVariables vars=new TsVariables();
        TsPeriod start = new TsPeriod(TsFrequency.Monthly, 1980, 0);
        for (int i = 0; i < N * N; ++i) {
            TsData data=new TsData(start, M.column(i));
            vars.set("s" + i , new TsVariable(data));
        }
        // create the specs
        MultiCholetteSpecification spec = new MultiCholetteSpecification();
        // put unbinding constraints
        for (int i = 0; i < N; ++i) {
            StringBuilder str=new StringBuilder();
            str.append(.5 * N).append('=');
            int[] ids = new int[N];
            for (int j = 0; j < N; ++j) {
                str.append("+s").append(i + N * j);
            }
            spec.addConstraint(str.toString());
        }
        for (int i = 0; i < N ; ++i) {
            StringBuilder str=new StringBuilder();
            str.append(.5 * N).append('=');
            for (int j = 0; j < N; ++j) {
                str.append("+s").append(j + N * i);
            }
            spec.addConstraint(str.toString());
        }
        spec.addConstraint("0=s2+2*s6-s10-s18");
        spec.addConstraint("0=s4+2*s11-s18");

        //spec.getParameters().setRho(.8);
        MultiCholetteDocument doc = new MultiCholetteDocument();
        
        doc.setInput(vars);
        doc.setSpecification(spec);
        MultiBenchmarkingResults results = doc.getResults();
        for (int j = 0; j < N; ++j) {
            TsData S = null;
            for (int i = 0; i < N; ++i) {
                S = TsData.add(S, results.getData("benchmarked.s" + (i + N * j), TsData.class));
            }
            DescriptiveStatistics stat = new DescriptiveStatistics(S);
            assertTrue(Math.abs(stat.getAverage() - .5 * N) < 1e-6);
            assertTrue(stat.getStdev() < 1e-9);
//            System.out.println(stat.getStdev());
        }
        for (int j = 0; j < N; ++j) {
            TsData S = null;
            for (int i = 0; i < N; ++i) {
                S = TsData.add(S, results.getData("benchmarked.s" + (N * i + j), TsData.class));
            }
            DescriptiveStatistics stat = new DescriptiveStatistics(S);
            assertTrue(Math.abs(stat.getAverage() - .5 * N) < 1e-6);
            assertTrue(stat.getStdev() < 1e-9);
//            System.out.println(stat.getStdev());
        }
        TsData S = results.getData("benchmarked.s2", TsData.class);
        S = TsData.add(S, results.getData("benchmarked.s6", TsData.class).times(2));
        S = TsData.subtract(S, results.getData("benchmarked.s10", TsData.class));
        S = TsData.subtract(S, results.getData("benchmarked.s18", TsData.class));

        DescriptiveStatistics stat = new DescriptiveStatistics(S);
        assertTrue(Math.abs(stat.getAverage()) < 1e-6);
        assertTrue(stat.getStdev() < 1e-9);
        
        MultiCholetteDocument clone = doc.clone();
        assertTrue(clone.getResults() != null);
    }
}

