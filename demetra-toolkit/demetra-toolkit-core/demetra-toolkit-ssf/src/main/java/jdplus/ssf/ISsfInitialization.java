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
package jdplus.ssf;

import jdplus.data.DataBlock;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public interface ISsfInitialization {

    /**
     * Initialisation of the model. Only the covariance of the state array is
     * considered in the model (the initial prediction errors are defined with
     * the data).
     */
    public static enum Type {
        /**
         * Exact diffuse initialisation
         */
        Diffuse,
        /**
         * Zero initialisation. [A(-1)=0,] P(-1)=0 -> [A(0|-1)=0,] P(0|-1)=Q
         * (transition innovations)
         */
        Zero,
        /**
         * Unconditional initialisation, defined by P0=TP0T'+V
         * [A(0|-1)=0,] P0
         */
        Unconditional,
        /**
         * [A(0) and] P(0) are pre-specified
         */
        UserDefined

    }

    int getStateDim();

    //<editor-fold defaultstate="collapsed" desc="initialisation">
    /**
     *
     * @return
     */
    boolean isDiffuse();

    /**
     * Dimension of the non stationary part of the state vector
     *
     * @return
     */
    int getDiffuseDim();

    /**
     * B = d x nd, where d = getStateDim(), nd = getNonStationaryDim() P(-1,
     * inf) = B * B'
     *
     * @param b
     */
    void diffuseConstraints(FastMatrix b);

    /**
     * Initial state
     *
     * @param a0 Buffer that will contain the initial state = a(0|-1)
     */
    void a0(DataBlock a0);

    /**
     * Modelling of the stationary variance of the initial state P(0|-1)
     *
     * @param pf0
     */
    void Pf0(FastMatrix pf0);

    /**
     * Modelling of the non stationary part of the initial state P(-1, inf)
     *
     * @param pi0
     */
    default void Pi0(FastMatrix pi0) {
        int nd = this.getDiffuseDim();
        if (nd == 0) {
            return;
        }
        int n = pi0.getColumnsCount();
        FastMatrix B = FastMatrix.make(n, nd);
        this.diffuseConstraints(B);
        SymmetricMatrix.XXt(B, pi0);
    }

    //</editor-fold> 
}
