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
package demetra.ssf.dk;

import demetra.design.Development;
import demetra.maths.matrices.Matrix;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.State;

/**
 * Represents x* = x + d, where x is a usual state vector and d models the
 * diffuse part. d is represented by its covariance matrix (up to an arbitrary
 * large factor)
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class DiffuseState extends State {

    public static DiffuseState of(ISsfDynamics dyn) {
        DiffuseState state = new DiffuseState(dyn.getStateDim());
        if (!dyn.a0(state.a())) {
            return null;
        }
        if (!dyn.Pf0(state.P())) {
            return null;
        }
        if (dyn.isDiffuse()) {
            dyn.Pi0(state.Pi);
        }
        return state;
    }
    /**
     * Pi is the covariance matrix of the diffuse part
     */
    private final Matrix Pi;

    /**
     *
     *
     * @param dim
     */
    public DiffuseState(final int dim) {
        super(dim);
        Pi = Matrix.square(dim);
    }

    /**
     * @return the Pi
     */
    public Matrix Pi() {
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
