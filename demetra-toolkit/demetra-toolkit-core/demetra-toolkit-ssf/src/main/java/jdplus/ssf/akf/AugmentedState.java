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
package jdplus.ssf.akf;

import demetra.design.Development;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.State;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.ISsfInitialization;
import jdplus.ssf.ISsfState;
import jdplus.maths.matrices.FastMatrix;

/**
 * Represents x* = x + A d, where x is a usual state vector and A is a matrix of
 * constraints on some unknown parameters (d). This is similar to the ENRV
 * (extended normal random vector) of Snyder/Forbes
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class AugmentedState extends State {

    public static AugmentedState of(ISsfState ssf) {
        ISsfInitialization initialization = ssf.initialization();
        AugmentedState state = new AugmentedState(initialization.getStateDim(), initialization.getDiffuseDim());
        initialization.a0(state.a());
        initialization.Pf0(state.P());
        if (initialization.isDiffuse()) {
            initialization.diffuseConstraints(state.B);
        }
        return state;
    }

    /**
     * B contains the states of the constraints. Its interpretation depends on
     * the considered step
     */
    private final CanonicalMatrix B;
    private int ndropped = 0;

    /**
     *
     *
     * @param dim
     * @param ndiffuse
     */
    public AugmentedState(final int dim, final int ndiffuse) {
        super(dim);
        B = CanonicalMatrix.make(dim, ndiffuse);
    }

    public final FastMatrix B() {
        return B.extract(0, B.getRowsCount(), ndropped, B.getColumnsCount()-ndropped);
    }

    public void restoreB(FastMatrix b) {
        int n = b.getColumnsCount(), m = B.getColumnsCount();
        ndropped = m - n;
        B().copy(b);
    }

    public final void dropDiffuseConstraint() {
        ++ndropped;
    }

    public final void dropAllConstraints() {
        ndropped = B.getColumnsCount();
    }

    public final boolean isDiffuse() {
        return ndropped < B.getColumnsCount();
    }

    public int getDiffuseDim() {
        return B.getColumnsCount() - ndropped;
    }

    /**
     * Computes a(t+1|t), P(t+1|t) from a(t|t), P(t|t)
     * a(t+1|t) = T(t)a(t|t)
     * P(t+1|t) = T(t)P(t|t)T'(t) + V(t) 
     * Also, compute TB, if the model is diffuse. 
     * @param pos Current position of the filter (=t)
     * @param dynamics Dynamics of the filter
     */
    @Override
    public void next(int pos, ISsfDynamics dynamics) {
        super.next(pos, dynamics);
        if (isDiffuse()) {
            dynamics.TM(pos, B);
        }
    }
}
