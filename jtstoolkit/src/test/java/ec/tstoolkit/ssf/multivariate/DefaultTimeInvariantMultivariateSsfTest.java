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
package ec.tstoolkit.ssf.multivariate;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.ssf.DisturbanceSmoother;
import ec.tstoolkit.ssf.Smoother;
import ec.tstoolkit.ssf.SmoothingResults;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class DefaultTimeInvariantMultivariateSsfTest {

    public DefaultTimeInvariantMultivariateSsfTest() {
    }

    @Test
    public void testVar() {
        int V = 5, N = 300;
        DefaultTimeInvariantMultivariateSsf ssf = new DefaultTimeInvariantMultivariateSsf();
        ssf.initialize(2 * V, V, 2 * V, 2 * V);
        Matrix T = new Matrix(2 * V, 2 * V);
        SubMatrix t0 = T.subMatrix(0, V, 0, V);
        t0.diagonal().set(.9);
        t0.subDiagonal(-1).set(-.4);
        ssf.setT(T);

        Matrix Z = new Matrix(V, 2 * V);
        SubMatrix ZZ = Z.subMatrix(0, V, 0, V);
        ZZ.diagonal().set(3);
        ZZ.subDiagonal(1).set(2);
        ZZ.subDiagonal(2).set(1);
        ZZ.subDiagonal(-1).set(-1);
        Z.subDiagonal(V).set(1);
        ssf.setZ(Z);
        Matrix Q = new Matrix(2 * V, 2 * V);
        Q.diagonal().range(0, V).set(1);
        Q.diagonal().range(V, 2 * V).set(10);
        ssf.setQ(Q);
        Matrix P0 = Q.clone();
        ssf.setPf0(P0);
        Matrix d = new Matrix(V, N);
        d.randomize();
        d.sub(.5);
        for (int i = 0; i < N; i += 4) {
            d.set(1, i, Double.NaN);
        }
        long q0 = System.currentTimeMillis();
        SmoothingResults rslts = new SmoothingResults();
        Smoother smoother = new Smoother();
        smoother.setSsf(new M2uSsfAdapter(ssf, new FullM2uMap(V)));
        smoother.setCalcVar(true);
        smoother.process(new M2uData(d, null), rslts);
//        DisturbanceSmoother dsmoother=new DisturbanceSmoother();
//        dsmoother.setSsf(new M2uSsfAdapter(ssf, new FullM2uMap(V)));
//        dsmoother.process(new M2uData(d, null));
//        SmoothingResults rslts = dsmoother.calcSmoothedStates();
        Matrix C = new Matrix(d.getColumnsCount(), 2 * V);
        Matrix EC = new Matrix(d.getColumnsCount(), 2 * V);
        for (int i = 0; i < 2 * V; ++i) {
            C.column(i).copy(new DataBlock(rslts.component(i), 0, V * N, V));
            EC.column(i).copy(new DataBlock(rslts.componentStdev(i), 0, V * N, V));
        }
        long q1 = System.currentTimeMillis();
        System.out.println(q1 - q0);
        System.out.println(C);
        System.out.println(EC);
    }
}