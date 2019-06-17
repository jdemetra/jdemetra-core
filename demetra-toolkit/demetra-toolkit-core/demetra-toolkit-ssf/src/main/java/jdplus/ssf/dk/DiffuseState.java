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
package jdplus.ssf.dk;

import demetra.design.Development;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.State;
import jdplus.ssf.ISsfInitialization;
import jdplus.ssf.ISsfState;

/**
 * Represents x* = x + d, where x is a usual state vector and d models the
 * diffuse part. d is represented by its covariance matrix (up to an arbitrary
 * large factor)
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class DiffuseState extends State {

    public static DiffuseState of(ISsfState ssf) {
        ISsfInitialization initialization = ssf.initialization();
        DiffuseState state = new DiffuseState(initialization.getStateDim());
        initialization.a0(state.a());
        initialization.Pf0(state.P());
        if (initialization.isDiffuse()) {
            initialization.Pi0(state.Pi);
        }
        return state;
    }
    /**
     * Pi is the covariance matrix of the diffuse part
     */
    private final CanonicalMatrix Pi;

    /**
     *
     *
     * @param dim
     */
    public DiffuseState(final int dim) {
        super(dim);
        Pi = CanonicalMatrix.square(dim);
    }

    /**
     * @return the Pi
     */
    public CanonicalMatrix Pi() {
        return Pi;
    }

    public boolean isDiffuse() {
        return Pi.isZero(ZERO);
    }

    /**
     * @param pos
     * @param dynamics 
     */
    @Override
    public void next(int pos, ISsfDynamics dynamics){
        super.next(pos, dynamics);
        dynamics.TVT(pos, Pi);
    }
}
