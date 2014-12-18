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
package ec.tstoolkit.ssf;

import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class FastState extends BaseOrdinaryState {

    /**
     *
     */
    public DataBlock L;

    /**
     * 
     * @param state
     */
    public FastState(final FastState state) {
        super(state);
        L = state.L.deepClone();
    }

    /**
     * 
     * @param n
     * @param hasdata
     */
    public FastState(final int n, final boolean hasdata) {
        super(n, hasdata);
        L = new DataBlock(n);
    }

    /**
     * 
     * @param ssf
     * @param state
     * @param pos
     */
    public FastState(final ISsf ssf, final State state, final int pos) {
        super(ssf.getStateDim(), true);
        int dim = ssf.getStateDim();
        L = new DataBlock(dim);
        A.copy(state.A);
        f = ssf.ZVZ(pos, state.P.subMatrix());
        // K0 = TPZ' / var
        ssf.ZM(pos, state.P.subMatrix(), C);
        ssf.TX(pos, C);

        // L0: computes next iteration. TVT'-KK'*var + Q -V = - L(var)^-1 L'
        Matrix P = state.P, TVT = P.clone();
        ssf.TVT(pos, TVT.subMatrix());
        Matrix Q = new Matrix(dim, dim);
        ssf.fullQ(pos, Q.subMatrix());
        TVT.add(Q);
        TVT.sub(P);
        for (int i = 0; i < dim; ++i) {
            double kv = -C.get(i) / f;
            if (kv != 0) {
                TVT.add(i, i, kv * C.get(i));

                for (int j = 0; j < i; ++j) {
                    TVT.add(i, j, kv * C.get(j));
                    TVT.add(j, i, kv * C.get(j));
                }
            }
        }

        TVT.mul(-f);
        int imax = 0;
        double lmax = TVT.get(0, 0);
        for (int i = 1; i < dim; ++i) {
            double lcur = TVT.get(i, i);
            if (lcur > lmax) {
                imax = i;
                lmax = lcur;
            }
        }
        if (lmax > 0) {
            //    throw new SsfException(SsfException.InvalidFastFilter);
            L.copy(TVT.column(imax));
            L.mul(Math.sqrt(1 / lmax));
        }
        else if (!TVT.isZero(1e-6)) {
            throw new SsfException(SsfException.FASTFILTER);
        }else
            L.set(0);
    }

    /**
     * 
     * @param state
     */
    public void copy(final FastState state) {
        super.copy(state);
        L = state.L.deepClone();
    }
}
