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

import ec.tstoolkit.BaseException;
import ec.tstoolkit.data.SubArrayOfInt;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.ElementaryTransformations;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class ArrayFilter {

//    private ISsf m_ssf;
//    private Matrix m_X, m_L0;
//    private DataBlock m_W, m_W0;
//    private Matrix m_Res;
//    private int m_r, m_rdim;
//
//    /**
//     * 
//     */
//    public ArrayFilter() {
//    }
//
//    private void getModelInfo(final int pos) {
//        int rcount = m_ssf.getTransitionResCount();
//        if (m_rdim == m_r) {
//            m_ssf.Q(pos, m_Res.subMatrix());
//            SymmetricMatrix.lcholesky(m_Res, 1e-9);
//        } else {
//            Matrix Q = new Matrix(m_rdim, m_rdim);
//            m_ssf.Q(pos, Q.subMatrix());
//            SymmetricMatrix.lcholesky(Q, 1e-9);
//            Matrix W = new Matrix(rcount, m_rdim);
//            m_ssf.W(pos, W.subMatrix());
//            if (!m_ssf.hasR()) // HasR == false
//            {
//                m_Res.subMatrix().product(W.subMatrix(), Q.subMatrix());
//            } else {
//                int[] idx = new int[rcount];
//                m_ssf.R(0, SubArrayOfInt.create(idx));
//                int j = m_r - rcount;
//                m_Res.subMatrix(j, m_r, 0, m_rdim).product(W.subMatrix(),
//                        Q.subMatrix());
//                for (int i = 0; i < idx.length; ++i) {
//                    if (idx[i] == j + i) {
//                        break;
//                    }
//                    m_Res.row(idx[i]).copy(m_Res.row(j + i));
//                }
//            }
//        }
//    }
//
//    /**
//     * 
//     * @return
//     */
//    public ISsf getSsf() {
//        return m_ssf;
//    }
//
//    private void initMatrix() {
//        int nd = m_ssf.getNonStationaryDim();
//        int nc = 2 * m_r + nd;
//        if (nd == m_r) {
//            m_ssf.diffuseConstraints(m_L0.subMatrix());
//            m_W0.set(Double.POSITIVE_INFINITY);
//        } else {
//            Matrix p0 = new Matrix(m_r, m_r);
//            m_ssf.Pf0(p0.subMatrix());
//            SymmetricMatrix.lcholesky(p0, 0);
//
//            Matrix M = new Matrix(m_r, nc);
//            M.subMatrix(0, m_r, m_r, 2 * m_r).copy(p0.subMatrix());
//            if (nd > 0) {
//                m_ssf.diffuseConstraints(M.subMatrix(0, m_r, 2 * m_r, nc));
//            }
//
//            DataBlock W = new DataBlock(nc);
//            W.extract(m_r, m_r, 1).set(1);
//            if (nd > 0) {
//                W.extract(2 * m_r, nd, 1).set(Double.POSITIVE_INFINITY);
//            }
//            ExtendedFastGivens.process(M.subMatrix(), W);
//            m_L0.subMatrix().copy(M.subMatrix(0, m_r, 0, m_r));
//            m_W0.copy(W.extract(0, m_r, 1));
//        }
//    }
//
//    private void initSsf() {
//        m_r = m_ssf.getStateDim();
//        m_rdim = m_ssf.getTransitionResDim();
//
//        // Arrays:
//        int nc = m_r + m_rdim;
//        m_X = new Matrix(m_r + 1, nc);
//        m_W = new DataBlock(nc);
//        m_Res = new Matrix(m_r, m_rdim);
//
//        m_L0 = new Matrix(m_r, m_r);
//        m_W0 = new DataBlock(m_r);
//        initMatrix();
//
//        if (m_ssf.isTimeInvariant()) {
//            getModelInfo(0);
//        }
//    }
//
//    /**
//     * 
//     * @return
//     */
//    public boolean isDiffuse() {
//        for (int i = 0; i < m_W.getLength(); ++i) {
//            if (Double.isInfinite(m_W.get(i))) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private void preArray(final int pos) {
//        m_X.row(0).set(0);
//        SubMatrix L = m_X.subMatrix(1, 1 + m_r, 0, m_r);
//        m_ssf.ZM(pos, L, m_X.row(0).range(0, m_r));
//        DataBlockIterator lcols = L.columns();
//        DataBlock lcol = lcols.getCurrent();
//        do {
//            m_ssf.TX(pos, lcol);
//        } while (lcols.next());
//        m_X.subMatrix(1, 1 + m_r, m_r, m_r + m_rdim).copy(
//                m_Res.subMatrix());
//
//        m_W.range(m_r, m_r + m_rdim).set(1);
//    }
//
//    /**
//     * 
//     * @param data
//     * @return
//     */
//    public boolean process(final ISsfData data) {
//        SubMatrix L = m_X.subMatrix(1, 1 + m_r, 1, 1 + m_r);
//        L.copy(m_L0.subMatrix());
//        m_W.range(1, 1 + m_r).copy(m_W0);
//
//        int pos = 0;
//        do {
//            if (!m_ssf.isTimeInvariant()) {
//                getModelInfo(pos);
//            }
//            preArray(pos);
//            if (!ExtendedFastGivens.process(m_X.subMatrix(), m_W)) {
//                return false;
//            }
//            // save results;
//        } while (pos++ < data.getCount());
//        return true;
//    }
    private ISsf m_ssf;
    private Matrix m_X, m_Res;
    private int m_r, m_rdim;
    private ArrayState m_state;

    /**
     * 
     * @param value
     */
    public void setSsf(final ISsf value) {
        m_ssf = value;
        initSsf();
    }

    public boolean process(final ISsfData data, final IArrayFilteringResults rslts) {

        if (rslts != null) {
            rslts.prepare(m_ssf, data);
        }
        int pos = 0;
        do {
            if (!m_ssf.isTransitionResidualTimeInvariant()) {
                getModelInfo(pos);
            }
            // pred
            predict(pos, data);
            preArray(pos);

            if (!triangularize()) {
                return false;
            }
            //
            // save results;
            updateState(pos);
            if (rslts != null) {
                rslts.save(pos, m_state);
            }
        } while (pos++ < data.getCount());
        return true;
    }

    private boolean triangularize() {
        try {
            int r =m_X.getRowsCount(), c = m_X.getColumnsCount();
            SubMatrix L = m_X.subMatrix();
            do {
                //ElementaryTransformations.rowHouseholder(L);
                ElementaryTransformations.rowGivens(L);
                L = L.extract(1, r, 1, c);
                --r;
                --c;
            } while (!L.isEmpty());
            return true;
        } catch (BaseException err) {
            return false;
        }
    }

    private void initSsf() {
        m_r = m_ssf.getStateDim();
        m_rdim = m_ssf.getTransitionResDim();

        // Arrays:
        int nc = 1 + m_r + m_rdim;
        m_X = new Matrix(m_r + 1, nc);
        m_Res = new Matrix(m_r, m_rdim);

        Matrix p0 = new Matrix(m_r, m_r);
        m_ssf.Pf0(p0.subMatrix());
        SymmetricMatrix.lcholesky(p0, 0);

        SubMatrix V = m_X.subMatrix(1, m_r + 1, 1, m_r +1);
        V.copy(p0.subMatrix());

        if (m_ssf.isTransitionResidualTimeInvariant()) {
            getModelInfo(0);
        }

        m_state = new ArrayState(V, true);
    }

    // A = |0 Z*L 0|  The zero column is used to avoid the move of some results.
    //     |0 T*L S|  It should be checked that it is more efficient...
    //
    // A O = | X 0|
    //       | Y Z|
    //
    // AA' = | Z*L*L'*Z'      Z*L*L'*T'|
    //       | T*L*L'*Z' T*L*L'*T'+S*S'|
    //
    // AA' = | Z*P*Z'      Z*P*T'|
    //       | T*P*Z' T*P*T'+S*S'|
    //
    // AOO'A' = | XX' XY'    |
    //          | YX' YY'+ZZ'|
    //
    // XX' = ZPZ' -> X=e
    // YX' = TPZ' = K*e -> Y = K (=TPZ'(ZPZ')^-1/2)
    // YY' + ZZ' = T*P*T'+ Q -> ZZ' = Pnext
 
    private void preArray(final int pos) {
        int r1=m_r+1;
        DataBlock X=m_X.row(0).range(1, r1);
        X.set(0);
        m_X.column(0).set(0);
        SubMatrix Y = m_X.subMatrix(1, r1, 1, r1);
        m_ssf.ZM(pos, Y, X);
        DataBlockIterator lcols = Y.columns();
        DataBlock lcol = lcols.getData();
        do {
            m_ssf.TX(pos, lcol);
        } while (lcols.next());
        
        SubMatrix Z = m_X.subMatrix(1, r1, r1, r1+m_rdim);
       Z.copy(m_Res.subMatrix());
    }

    private void getModelInfo(final int pos) {
        // get the residuals
        // Qfull = r*W*Q*W'*r
        int rcount = m_ssf.getTransitionResCount();
        Matrix Q = new Matrix(m_rdim, m_rdim);
        m_ssf.Q(pos, Q.subMatrix());
        SymmetricMatrix.lcholesky(Q, 1e-9);
        Matrix W = new Matrix(rcount, m_rdim);
        m_ssf.W(pos, W.subMatrix());
        if (!m_ssf.hasR()) // HasR == false
        {
            m_Res.subMatrix().product(W.subMatrix(), Q.subMatrix());
        } else {
            int[] idx = new int[rcount];
            m_ssf.R(0, SubArrayOfInt.create(idx));
            int j = m_r - rcount;
            m_Res.subMatrix(j, m_r, 0, m_rdim).product(W.subMatrix(),
                    Q.subMatrix());
            for (int i = 0; i < idx.length; ++i) {
                if (idx[i] == j + i) {
                    break;
                }
                m_Res.row(idx[i]).copy(m_Res.row(j + i));
            }
        }
    }

    private void updateState(int pos) {
        // copy K:
        m_state.K.copy(m_X.column(0).drop(1, 0));
        double r = m_X.get(0, 0);
        m_state.r=r;

        // next state:
        // A = Ta + K v / r
        m_ssf.TX(pos, m_state.A);
        if (! m_state.isMissing())
            m_state.A.addAY(m_state.e/r, m_state.K);
    }

    private void predict(int pos, ISsfData data) {
        if (data.hasData()) {
            double y = data.get(pos);
            if (Double.isNaN(y)) {
                m_state.e = Double.NaN;
            } else {
                m_state.e = y - m_ssf.ZX(pos, m_state.A);
            }
        } else {
            m_state.e = 0;
        }
    }
}
