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
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.SsfException;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class LoadingCoherence {

    public void check(ISsfLoading l, int dim) {
        check(l, dim, 0, 1);
    }

    public void check(ISsfLoading l, int dim, int start, int end) {
        for (int j = start; j < end; ++j) {
            DataBlock z = DataBlock.make(dim);
            l.Z(j, z);
            FastMatrix M = Randoms.randomMatrix(dim);
            FastMatrix P = Randoms.randomSymmetricMatrix(dim);
            DataBlock v = Randoms.randomArray(dim);

            // Zx
            double zx = l.ZX(j, v);
            if (Math.abs(zx - z.dot(v)) > 1e-9) {
                throw new SsfException(SsfException.MODEL);
            }

            // ZM
            DataBlock zm = DataBlock.make(M.getColumnsCount());
            zm.product(z, M.columnsIterator());
            DataBlock w = DataBlock.make(M.getColumnsCount());
            l.ZM(j, M, w);
            if (zm.distance(w) > 1e-9) {
                throw new SsfException(SsfException.MODEL);
            }

            // X+Zd
            w = v.deepClone();
            l.XpZd(j, w, 3.3);

            DataBlock w2 = v.deepClone();
            w2.addAY(3.3, z);
            if (w2.distance(w) > 1e-9) {
                throw new SsfException(SsfException.MODEL);
            }

            // ZVZ'
            DataBlock zp = DataBlock.make(P.getColumnsCount());
            zp.product(z, P.columnsIterator());
            if (Math.abs(zp.dot(z) - l.ZVZ(j, P)) > 1e-9) {
                throw new SsfException(SsfException.MODEL);
            }

            // P+ZdZ'
            FastMatrix p = P.deepClone();
            l.VpZdZ(j, p, 3.3);
            FastMatrix q = P.deepClone();
            GeneralMatrix.aXYt_p_A(3.3, z, z, q);
            if (MatrixNorms.frobeniusNorm(q.minus(p)) > 1e-9) {
                throw new SsfException(SsfException.MODEL);
            }
        }
    }

}
