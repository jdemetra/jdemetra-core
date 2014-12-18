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

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.SubArrayOfInt;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.SubMatrix;

/**
 * Base interface for state space models.
 * Describes the transition equation.
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface ISsfBase {
    /**
     * B = d x nd, where d = getStateDim(), nd = getNonStationaryDim()
     * @param b
     */
    void diffuseConstraints(SubMatrix b);

    /**
     * Variance matrix of the innovations in the transition equation.
     * V is also modelled as
     * 
     * V(R(i), R(j)) = (W.Q.W')(i,j)
     * 
     * V = d x d where d = getStateDim()
     * Q = q x q where q = getTransitionResDim()
     * R = 1 x r where r = getTransitionResCount()
     * W = r x q
     * 
     * When r = d, R should be considered as missing (R = 0...d-1)
     * When W = I, it should be considered as missing.
     * 
     * @param pos
     * @param qm
     */
    void fullQ(int pos, SubMatrix qm);

    /**
     * Dimension of the non stationary part of the model
     * @return
     */
    int getNonStationaryDim();

    /**
     * Dimension of the state vector
     * @return
     */
    int getStateDim(); // M

    /**
     * Number of elements of the state vector which are modified by some
     * random information (not necessary for each period). 
     * @return
     */
    int getTransitionResCount(); // E

    /**
     * Dimension of the random effects. See fullQ for further information
     * @return
     */
    int getTransitionResDim(); // E

    /**
     * @return
     */
    boolean hasR();

    /**
     *
     * @param pos
     * @return
     */
    boolean hasTransitionRes(int pos);

    /**
     *
     * @return
     */
    boolean hasW();

    /**
     * 
     * @return
     */
    boolean isDiffuse();

    /**
     *
     * @return
     */
    boolean isMeasurementEquationTimeInvariant();

    // information
    /**
     *
     * @return
     */
    boolean isTimeInvariant();

    /**
     *
     * @return
     */
    boolean isTransitionEquationTimeInvariant();

    /**
     *
     * @return
     */
    boolean isTransitionResidualTimeInvariant();

    /**
     *
     * @return
     */
    boolean isValid();

    /**
     *
     * @param pf0
     */
    void Pf0(SubMatrix pf0);

    /**
     *
     * @param pf0
     */
    void Pi0(SubMatrix pi0);

    /**
     *
     * @param pos
     * @param qm
     */
    void Q(int pos, SubMatrix qm);

    /**
     *
     * @param pos
     * @param rv
     */
    void R(int pos, SubArrayOfInt rv);

    /**
     * Gets the transition matrix.
     * @param pos The position of the model
     * @param tr The sub-matrix that will receive the transition matrix.
     * It must have the dimensions (getStateDim() x getStateDim()). 
     * The caller has the responsibility to provide a clean sub-matrix, 
     * so that the callee can safely set only the non zero values.  
     */
    void T(int pos, SubMatrix tr);

    /**
     * Computes T V T'
     * @param pos The position of the model
     * @param vm 
     */
    void TVT(int pos, SubMatrix vm);

    // forward operations
    /**
     *
     * @param pos
     * @param x
     */
    void TX(int pos, DataBlock x);

    /**
     *
     * @param pos
     * @param wv
     */
    void W(int pos, SubMatrix wv);

    // backward operations
    /**
     *
     * @param pos
     * @param x
     */
    void XT(int pos, DataBlock x);
}
