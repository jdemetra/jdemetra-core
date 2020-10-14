/*
 * Copyright 2016 National Bank copyOf Belgium
 *  
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions copyOf the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy copyOf the Licence at:
 *  
 * http://ec.europa.eu/idabc/eupl
 *  
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.ssf.akf;

import jdplus.data.DataBlock;
import jdplus.data.LogSign;
import jdplus.likelihood.DeterminantalTerm;
import jdplus.math.matrices.decomposition.ElementaryTransformations;
import jdplus.math.matrices.LowerTriangularMatrix;
import jdplus.ssf.State;
import jdplus.ssf.likelihood.DiffuseLikelihood;
import jdplus.math.matrices.Matrix;

/**
 *
 * @author Jean Palate
 */
public class QAugmentation {

    // Q is related to the cholesky factor of the usual "Q matrix" of De Jong.
    // Q(dj) = |S^-1   -s|
    //         |-s'  q|
    // Q = |a 0|
    //     |b c|
    // so that we have:
    // q = b * b' + c * c
    // S^-1 = a * a' 
    // -s = a * b'
    // s' * S^-1 * s = b * a' * S^-1 * a * b' = b * b'
    // q - s' * S^-1 * s = c * c
    // s' * S^-1 = b * a' * S^-1 = b * a^-1 
    private Matrix Q, B;
    private int n, nd;
    private DeterminantalTerm det = new DeterminantalTerm();

    public void prepare(final int nd, final int nvars) {
        clear();
        this.nd = nd;
        Q = Matrix.make(nd + 1, nd + 1 + nvars);
    }

    public void clear() {
        n = 0;
        Q = null;
        det.clear();
    }
    
    public int getDegreesofFreedom(){
        return n-nd;
    }

//    public void update(Matrix E, DataBlock2 U) {
//        Q.subMatrix(0, nd, nd + 1, nd + 1 + nvars).copy(E.subMatrix());
//        Q.row(nd).range(nd + 1, nd + 1 + nvars).copy(U);
//        ec.tstoolkit.maths.matrices.ElementaryTransformations.fastGivensTriangularize(Q.subMatrix());
//    }
//
    public void update(AugmentedUpdateInformation pe) {
        double v = pe.getVariance();
        if (v == 0)
            return; // redundant constraint
        ++n;
        double e = pe.get();
        det.add(v);
        DataBlock col = Q.column(nd + 1);
        double se = Math.sqrt(v);
        col.range(0, nd).setAY(1 / se, pe.E());
        col.set(nd, e / se);
        ElementaryTransformations.fastGivensTriangularize(Q);
    }

    public Matrix a() {
        return Q.extract(0, nd, 0, nd);
    }

    public DataBlock b() {
        return Q.row(nd).range(0, nd);
    }

    public double c() {
        return Q.get(nd, nd);
    }
    
    /**
     * Gets the matrix copyOf the diffuse effects used for collapsing
 More exactly, we provide B*a^-1'
     * @return 
     */
    public Matrix B(){
        return B;
    }

    public DiffuseLikelihood likelihood(boolean scalingfactor) {
        double cc = c();
        cc *= cc;
        LogSign dsl = LogSign.of(a().diagonal());
        double dcorr = 2 * dsl.getValue();
        return DiffuseLikelihood.builder(n, nd)
                .ssqErr(cc)
                .logDeterminant(det.getLogDeterminant())
                .diffuseCorrection(dcorr)
                .concentratedScalingFactor(scalingfactor)
                .build();
    }

    public boolean canCollapse() {
        return isPositive(Q.diagonal().drop(0, 1));
    }

    public boolean collapse(AugmentedState state) {
        if (!isPositive(Q.diagonal().drop(0, 1))) {
            return false;
        }

        // update the state vector
        B =state.B().deepClone();
        int d = B.getColumnsCount();
        Matrix S = a().deepClone();
        // aC'=B' <-> Ca'=B <-> C=B*a'^-1
        LowerTriangularMatrix.solveXLt(S, B);
        for (int i = 0; i < d; ++i) {
            DataBlock col = B.column(i);
            state.a().addAY(-Q.get(d, i), col);
            state.P().addXaXt(1, col);
        }
        state.dropAllConstraints();
        return true;
    }
    // TODO Update with Java 8

    public static boolean isPositive(DataBlock q) {
        for (int i = 0; i < q.length(); ++i) {
            if (q.get(i) < State.ZERO) {
                return false;
            }
        }
        return true;
    }
    

}
