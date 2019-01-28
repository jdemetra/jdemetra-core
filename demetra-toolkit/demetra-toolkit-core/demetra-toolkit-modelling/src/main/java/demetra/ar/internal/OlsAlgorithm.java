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
package demetra.ar.internal;

import demetra.data.DataBlockIterator;
import demetra.design.AlgorithmImplementation;
import demetra.maths.matrices.Matrix;
import org.openide.util.lookup.ServiceProvider;
import demetra.data.DoubleSequence;
import demetra.leastsquares.QRSolvers;
import demetra.leastsquares.QRSolver;
import demetra.ar.AutoRegressiveEstimation;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = AutoRegressiveEstimation.class)
@AlgorithmImplementation(algorithm=AutoRegressiveEstimation.class)
public class OlsAlgorithm implements AutoRegressiveEstimation {

    private double[] y, a;

    @Override
    public boolean estimate(DoubleSequence Y, int nar) {
        y=Y.toArray();
        int n = y.length;
        
        Matrix M = Matrix.make(n-nar, nar);
        DataBlockIterator cols = M.columnsIterator();
        for (int i = 0; i < nar; ++i) {
            cols.next().copy(Y.drop(nar-i-1, n));
        }
        QRSolver solver = QRSolvers.fastSolver();
        DoubleSequence yc = Y.drop(nar, 0);
        if (!solver.solve(yc, M)) {
            return false;
        }
        DoubleSequence c = solver.coefficients();
        a=c.toArray();
        return true;
    }

    @Override
    public DoubleSequence coefficients() {
        return DoubleSequence.ofInternal(a);
    }

    @Override
    public DoubleSequence data() {
        return DoubleSequence.ofInternal(y);
    }
}
