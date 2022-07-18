/*
 * Copyright 2022 National Bank of Belgium
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
package jdplus.ssf.utility;

import jdplus.data.DataBlock;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.GeneralMatrix;
import jdplus.math.matrices.MatrixNorms;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.SsfException;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class DynamicsCoherence {

    /**
     * shortcut for time invariant state components
     *
     * @param dyn
     * @param dim
     */
    public void check(ISsfDynamics dyn, int dim) {
        check(dyn, dim, 0, 1);
    }

    /**
     * Checks the coherence of the different functions provided in a Ssf
     * dynamics implementation
     *
     * @param dyn The tested dynamics
     * @param dim The dimension of the state block
     * @param start The starting position (included)
     * @param end The ending position(excluded)
     */
    public void check(ISsfDynamics dyn, int dim, int start, int end) {
        for (int j = start; j < end; ++j) {
            FastMatrix T = FastMatrix.square(dim);
            dyn.T(j, T);
            FastMatrix M = Randoms.randomMatrix(dim);
            FastMatrix P = Randoms.randomSymmetricMatrix(dim);
            DataBlock v = Randoms.randomArray(dim);

            // Tx
            DataBlock w = v.deepClone();
            dyn.TX(j, w);
            DataBlock tx = DataBlock.make(dim);
            tx.product(T.rowsIterator(), v);
            if (tx.distance(w) > 1e-9) {
                throw new SsfException(SsfException.MODEL);
            }

            // TM
            FastMatrix TM = GeneralMatrix.AB(T, M);
            FastMatrix m = M.deepClone();
            dyn.TM(j, m);
            if (MatrixNorms.frobeniusNorm(TM.minus(m)) > 1e-9) {
                throw new SsfException(SsfException.MODEL);
            }

            // MT
            FastMatrix MT = GeneralMatrix.AB(M, T);
            m = M.deepClone();
            dyn.MT(j, m);
            if (MatrixNorms.frobeniusNorm(MT.minus(m)) > 1e-9) {
                throw new SsfException(SsfException.MODEL);
            }

            // xT
            w = v.deepClone();
            dyn.XT(j, w);
            DataBlock xt = DataBlock.make(dim);
            xt.product(v, T.columnsIterator());
            if (xt.distance(w) > 1e-9) {
                throw new SsfException(SsfException.MODEL);
            }
            
            // TVT
            FastMatrix TVT = GeneralMatrix.ABt(GeneralMatrix.AB(T, P), T);
            FastMatrix p = P.deepClone();
            dyn.TVT(j, p);
            if (MatrixNorms.frobeniusNorm(TVT.minus(p)) > 1e-9) {
                throw new SsfException(SsfException.MODEL);
            }
            
            // check transition operations
            if (dyn.hasInnovations(j)) {
                FastMatrix V = FastMatrix.square(dim);
                dyn.V(j, V);
                FastMatrix S = FastMatrix.make(dim, dyn.getInnovationsDim());
                dyn.S(j, S);
                // V =SS'
                if (MatrixNorms.frobeniusNorm(V.minus(SymmetricMatrix.XXt(S))) > 1e-9) {
                    throw new SsfException(SsfException.MODEL);
                }
            }

        }
    }

}
