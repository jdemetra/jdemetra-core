/*
 * Copyright 2016 National Bank of Belgium
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
package demetra.ssf.ckms;

/**
 * 
 * @author Jean Palate
 */
//@Development(status = Development.Status.Alpha)
//public class FastArrayState extends BaseArrayState {
//
//    /**
//     *
//     */
//    public DataBlock L;
//
//    /**
//     * 
//     * @param state
//     */
//    public FastArrayState(final FastArrayState state)
//    {
//	super(state);
//	L = state.L.deepClone();
//    }
//
//    /**
//     * 
//     * @param n
//     * @param hasdata
//     */
//    public FastArrayState(final int n, final boolean hasdata)
//    {
//	super(n, hasdata);
//	L = new DataBlock(n);
//    }
//
//    /**
//     * 
//     * @param ssf
//     * @param state
//     * @param pos
//     */
//    public FastArrayState(final ISsf ssf, final State state, final int pos)
//    {
//	super(ssf.getStateDim(), true);
//	int dim = ssf.getStateDim();
//	L = new DataBlock(dim);
//	A.copy(state.A);
//	double var = ssf.ZVZ(pos, state.P.subMatrix());
//	r = Math.sqrt(var);
//	// K0 = TPZ' / var
//	ssf.ZM(pos, state.P.subMatrix(), K);
//	ssf.TX(pos, K);
//
//	// L0: computes next iteration. TVT'-KK'*var + Q -V = - L(var)^-1 L'
//	Matrix V = state.P, TVT = V.clone();
//	ssf.TVT(pos, TVT.subMatrix());
//	Matrix Q = new Matrix(dim, dim);
//	ssf.fullQ(pos, Q.subMatrix());
//	V.sub(Q);
//	V.sub(TVT);
//	K.mul(1 / r);
//	for (int i = 0; i < dim; ++i) {
//	    double kv = K.get(i);
//	    if (kv != 0) {
//		V.add(i, i, kv * K.get(i));
//
//		for (int j = 0; j < i; ++j) {
//		    V.add(i, j, kv * K.get(j));
//		    V.add(j, i, kv * K.get(j));
//		}
//	    }
//	}
//
//	int imax = 0;
//	double lmax = V.get(0, 0);
//	for (int i = 1; i < dim; ++i) {
//	    double lcur = V.get(i, i);
//	    if (lcur > lmax) {
//		imax = i;
//		lmax = lcur;
//	    }
//	}
//	if (lmax <= 0)
//	    throw new SsfException(SsfException.FASTFILTER);
//	L.copy(V.column(imax));
//	L.mul(Math.sqrt(1 / lmax));
//    }
//
//    /**
//     * 
//     * @param state
//     */
//    public void copy(final FastArrayState state)
//    {
//	super.copy(state);
//	L = state.L.deepClone();
//    }
//}
