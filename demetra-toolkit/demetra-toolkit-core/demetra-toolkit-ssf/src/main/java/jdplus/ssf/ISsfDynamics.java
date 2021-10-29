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
package jdplus.ssf;

import jdplus.data.DataBlock;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public interface ISsfDynamics extends ISsfRoot {

    //<editor-fold defaultstate="collapsed" desc="description">
    /**
     * Dimension of the innovations. See V for further information
     *
     * @return
     */
    int getInnovationsDim(); // E

    /**
     * Variance matrix of the innovations in the transition equation.
     *
     * Another (non unique) modelling is
     *
     * V = S*S'
     *
     * Where the rank of S is innovationsDim This modelling is useful in square
     * root algorithms. It is used for instance in De Jong.
     *
     * @param pos
     * @param qm
     */
    void V(int pos, FastMatrix qm);

    /**
     * @param pos
     * @param cm
     */
    void S(int pos, FastMatrix cm);

    /**
     *
     * @param pos
     * @return
     */
    boolean hasInnovations(int pos);

    boolean areInnovationsTimeInvariant();
    /**
     * Gets the transition matrix.
     *
     * @param pos The position of the model
     * @param tr The sub-matrix that will receive the transition matrix. It must
     * have the dimensions (getStateDim() x getStateDim()). The caller has the
     * responsibility to provide a clean sub-matrix, so that the callee can
     * safely set only the non zero values.
     */
    void T(int pos, FastMatrix tr);

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="forward operations">
    /**
     * Computes T(pos) * x
     *
     * @param pos
     * @param x
     */
    void TX(int pos, DataBlock x);

    /**
     * Computes x=x+ S(pos) * u. The dimension of u should correspond to the
     * number of columns of S(po)
     *
     * @param pos
     * @param x
     * @param u
     */
    void addSU(int pos, DataBlock x, DataBlock u);

    /**
     * Computes T(pos) * M
     *
     * @param pos
     * @param M
     */
    default void TM(int pos, FastMatrix M) {
        M.applyByColumns(x->TX(pos, x));
    }

    /**
     * Computes M* T'(pos)
     *
     * @param pos
     * @param M
     */
    default void MTt(int pos, FastMatrix M) {
        M.applyByRows(x->TX(pos, x));
    }
    /**
     * Computes T V T'
     *
     * @param pos The position of the model
     * @param M
     */
    default void TVT(int pos, FastMatrix M) {
        TM(pos, M);
        MTt(pos, M);
        SymmetricMatrix.reenforceSymmetry(M);
    }

    /**
     * Adds the variance of the innovations to a given matrix p = p + V(pos)
     *
     * @param pos
     * @param p
     */
    void addV(int pos, FastMatrix p);

    //</editor-fold>  
    //<editor-fold defaultstate="collapsed" desc="backward operations">
    /**
     * Computes x * T(pos)
     *
     * @param pos
     * @param x
     */
    void XT(int pos, DataBlock x);

    /**
     * Computes M * T(pos)
     *
     * @param pos
     * @param M
     */
    default void MT(int pos, FastMatrix M) {
        M.applyByRows(row->XT(pos, row));
    }

    /**
     * Computes T'((pos)*M
     *
     * @param pos
     * @param M
     */
    default void TtM(int pos, FastMatrix M) {
        M.applyByColumns(col->XT(pos, col));
    }
    /**
     * Computes xs = x*S(pos)
     *
     * @param pos
     * @param x
     * @param xs
     */
    void XS(int pos, DataBlock x, DataBlock xs);

    //</editor-fold>
}
