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

import ec.tstoolkit.algorithm.IProcessing.Status;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.ProxyResults;
import ec.tstoolkit.maths.matrices.Matrix;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author pcuser
 */
public class ParallelProcessingNodeTest {

    public ParallelProcessingNodeTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testOnMatrix() {
        SequentialProcessing<Matrix> all = new SequentialProcessing<>();
        IProcessingNode<Matrix> step1 = new IProcessingNode<Matrix>() {

            @Override
            public String getName() {
                return "sum";
            }

            @Override
            public String getPrefix() {
                return null;
            }

            @Override
            public Status process(Matrix input, Map<String, IProcResults> results) {
                ParallelProcessingNode<Matrix> cmps = new ParallelProcessingNode<>(getName(), null);
                int n = input.getColumnsCount();
                for (int i = 0; i < n; ++i) {
                    cmps.add(createNode(i));
                }
                return cmps.process(input, results);
            }
        };

        all.add(step1);
        Matrix M = new Matrix(10000, 100);
        M.randomize();
        long s0 = System.currentTimeMillis();
        for (int i = 0; i < 1000; ++i) {
            for (int j = 0; j < M.getColumnsCount(); ++j) {
                double x = M.column(j).ssq();
            }
        }
        long s1 = System.currentTimeMillis();
        //System.out.println(s1 - s0);

        CompositeResults process = all.process(M);
//        long t0 = System.currentTimeMillis();
//        for (int i = 0; i < 100; ++i) {
//            process = all.process(M);
//        }
//        long t1 = System.currentTimeMillis();
//
//        System.out.println(t1 - t0);

        // direct computation
        for (int i = 0; i < M.getColumnsCount(); ++i) {
            assertTrue(Math.abs(M.column(i).ssq() - process.getData("column" + i + ".value", Double.class)) < 1e-9);
        }


    }

    private static IProcessingNode<Matrix> createNode(final int pos) {
        return new IProcessingNode<Matrix>() {

            @Override
            public String getName() {
                return "column" + pos;
            }

            @Override
            public String getPrefix() {
                return getName();
            }

            @Override
            public Status process(Matrix input, Map<String, IProcResults> results) {
                InformationSet tmp = new InformationSet();
                tmp.set("value", input.column(pos).ssq());
                results.put(getName(), new ProxyResults(tmp, null));
                return Status.Valid;
            }
        };
    }
}
