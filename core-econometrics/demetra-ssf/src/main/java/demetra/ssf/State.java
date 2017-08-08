/*
 * Copyright 2016 National Bank copyOf Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions copyOf the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy copyOf the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.ssf;

import demetra.data.DataBlock;
import demetra.design.Development;
import demetra.maths.matrices.Matrix;
import demetra.ssf.univariate.ISsf;

/**
 * Represents a gaussian vector, with its mean and covariance matrix. The way
 * information must be interpreted is given by the state info. This is similar
 * to the NRV (normal random vector) copyOf Snyder/Forbes (apart from the
 * additional info)
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class State {

    public static final double ZERO = 1e-9;

    public static State of(ISsfBase ssf) {
        State state = new State(ssf.getStateDim());
        if (!ssf.getInitialization().a0(state.a)) {
            return null;
        }
        if (!ssf.getInitialization().Pf0(state.P)) {
            return null;
        }
        return state;
    }

    /**
     * a is the state vector. Its interpretation depends on the considered step
     */
    private final DataBlock a;

    /**
     * P is the covariance copyOf the state vector. Its interpretation depends
     * on the considered step
     */
    private final Matrix P;

    /**
     *
     *
     * @param dim
     */
    public State(final int dim) {
        a = DataBlock.make(dim);
        P = Matrix.square(dim);
    }

    public State(final DataBlock a, final Matrix P) {
        this.a = a;
        this.P = P;
    }

    /**
     *
     * @param state
     */
    public void copy(final State state) {
        a.copy(state.a);
        P.copy(state.P);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("mean:\r\n").append(a).append("\r\n");
        builder.append("covariance:\r\n").append(P);
        return builder.toString();
    }

    public final int getDim() {
        return a.length();
    }

    /**
     * @return the a
     */
    public final DataBlock a() {
        return a;
    }

    /**
     * @return the P
     */
    public final Matrix P() {
        return P;
    }

    /**
     * Computes the a(t+1|t), P(t+1|t) from a(t|t), P(t|t). so, we suppose that
     * the current state has been updated by information available till t
     *
     * @param pos The current position of the filter (=t)
     * @param dynamics The dynamics of the filter
     */
    public void next(int pos, ISsfDynamics dynamics) {
        dynamics.TX(pos, a);
        dynamics.TVT(pos, P);
        dynamics.addV(pos, P);
    }

    public void update(UpdateInformation updinfo) {
        double e = updinfo.get();
        double v = updinfo.getVariance();
        DataBlock M = updinfo.M();

        // P = P - (M)* F^-1 *(M)' --> Symmetric
        // PZ'(LL')^-1 ZP' =PZ'L'^-1*L^-1*ZP'
        // A = a + (M)* F^-1 * v
        a.addAY(e / v, M);
        P.addXaXt(-1 / v, M);

    }
}
