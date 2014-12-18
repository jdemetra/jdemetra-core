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
import ec.tstoolkit.maths.matrices.LowerTriangularMatrix;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.MatrixException;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.BeforeClass;

/**
 *
 * @author pcuser
 */
public class SequentialProcessingTest {

    public SequentialProcessingTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testBasicSequentialProcessing() {

        // This simple sequential processing will compute the inverse of the X'X product of a matrix X.
        // The first step will be the product X'X
        // The second step will be the cholesky decomposition L so that LL'=X'X
        // The third step will be L^-1=K
        // The last step will be K'K, the final result
        // of course, in practice, we never will use such a solution for simple processing.

        SequentialProcessing<Matrix> all = new SequentialProcessing<>();
        IProcessingNode<Matrix> step1 = new IProcessingNode<Matrix>() {

            @Override
            public String getName() {
                return "step1";
            }

            @Override
            public String getPrefix() {
                return "xtx";
            }

            @Override
            public Status process(Matrix input, Map<String, IProcResults> results) {
                InformationSet rslt = new InformationSet();
                rslt.add("value", SymmetricMatrix.XtX(input));
                results.put(getName(), new ProxyResults(rslt, null));
                return Status.Valid;
            }
        };
        IProcessingNode<Matrix> step2 = new IProcessingNode<Matrix>() {

            @Override
            public String getName() {
                return "step2";
            }

            @Override
            public String getPrefix() {
                return "l";
            }

            @Override
            public Status process(Matrix input, Map<String, IProcResults> results) {
                // retrieve the result of the first step.
                Matrix xtx = results.get("step1").getData("value", Matrix.class);
                if (xtx == null) {
                    return Status.Invalid;
                }
                InformationSet rslt = new InformationSet();
                Matrix l = xtx.clone();
                try {
                    SymmetricMatrix.lcholesky(l);
                    rslt.add("value", l);
                    return Status.Valid;
                }
                catch (MatrixException err) {
                    rslt.addError(err.getMessage());
                    return Status.Invalid;
                }
                finally {
                    results.put(getName(), new ProxyResults(rslt, null));
                }
            }
        };

        IProcessingNode<Matrix> step3 = new IProcessingNode<Matrix>() {

            @Override
            public String getName() {
                return "step3";
            }

            @Override
            public String getPrefix() {
                return "il";
            }

            @Override
            public Status process(Matrix input, Map<String, IProcResults> results) {
                // retrieve the result of the first step.
                Matrix l = results.get("step2").getData("value", Matrix.class);
                if (l == null) {
                    return Status.Invalid;
                }
                InformationSet rslt = new InformationSet();
                try {
                    Matrix il = LowerTriangularMatrix.inverse(l);
                    rslt.add("value", il);
                    return Status.Valid;
                }
                catch (MatrixException err) {
                    rslt.addError(err.getMessage());
                    return Status.Invalid;
                }
                finally {
                    results.put(getName(), new ProxyResults(rslt, null));
                }

            }
        };

        IProcessingNode<Matrix> step4 = new IProcessingNode<Matrix>() {

           @Override
            public String getName() {
                return "step4";
            }

            @Override
            public String getPrefix() {
                return "solution";
            }

            @Override
            public Status process(Matrix input, Map<String, IProcResults> results) {
                // retrieve the result of the first step.
                Matrix il = results.get("step3").getData("value", Matrix.class);
                if (il == null) {
                    return Status.Invalid;
                }
                InformationSet rslt = new InformationSet();
                try {
                    rslt.add("value", SymmetricMatrix.XtX(il));
                    return Status.Valid;
                }
                catch (MatrixException err) {
                    rslt.addError(err.getMessage());
                    return Status.Invalid;
                }
                finally {
                    results.put(getName(), new ProxyResults(rslt, null));
                }
            }
        };
        all.add(step1);
        all.add(step2);
        all.add(step3);
        all.add(step4);

        Matrix M = new Matrix(100, 10);
        M.randomize();
        CompositeResults process = all.process(M);

        for (String key : process.getDictionary().keySet()) {
            System.out.println(key);
        }
        
        // direct computation
        Matrix R=SymmetricMatrix.inverse(SymmetricMatrix.XtX(M));
        assert(R.minus(process.getData("solution.value", Matrix.class)).nrm2() < 1e-9);

    }
}
