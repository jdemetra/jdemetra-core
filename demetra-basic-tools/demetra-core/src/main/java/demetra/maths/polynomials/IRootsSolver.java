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
package demetra.maths.polynomials;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import demetra.design.Development;
import demetra.design.PrototypePattern;
import demetra.maths.Complex;
import demetra.maths.polynomials.internal.RobustMullerNewtonSolver;
import demetra.maths.polynomials.internal.MullerNewtonSolver;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@PrototypePattern
public interface IRootsSolver {

    /**
     * Clear the previous results
     */
    void clear();

    /**
     * The method tries to factorize the polynomial passed as a parameter.
     *
     * @param p
     * @return True if it succeeded, even partly
     */
    boolean factorize(Polynomial p);

    /**
     * The ratio between the original polynomial and the identified roots
     *
     * @return Equals to the initial polynomial if the factorization didn't
     * succeed
     */
    Polynomial remainder();

    /**
     * The found roots
     *
     * @return Null if the factorization didn't success
     */
    Complex[] roots();

    /**
     * By default, it returns a fast Muller-Newton solver 
     * The factorization is complete
     *
     * @return
     */
    public static IRootsSolver fastSolver() {
        return Roots_Factory.FASTSOLVER.get().get();
    }

    /**
     * By default, it returns a robust Muller-Newton solver. This version is significantly more robust
     * against multiple roots. However, it is not able to solve any
     * problem of multiple roots.
     * 
     * From a technical point of view, when multiple roots are found, the algorithm
     * will try to search the roots on derivatives of this polynomial.
     *
     * The factorization is complete
     *
     * @return
     */
    public static IRootsSolver robustSolver() {
        return Roots_Factory.ROBUSTSOLVER.get().get();
    }

    /**
     *
     * @param value
     */
    public static void setFastSolver(final Supplier<IRootsSolver> value) {
        Roots_Factory.FASTSOLVER.set(value);
    }

    /**
     *
     * @param value
     */
    public static void setRobustSolver(final Supplier<IRootsSolver> value) {
        Roots_Factory.ROBUSTSOLVER.set(value);
    }
}

class Roots_Factory {

    /**
     * The static member defines the Root finding algorithm used to find the
     * roots of the polynomial. By default this is the Muller algorithm. It can
     * be changed to whatever algorithm that supports the RootsSearcher
     * interface.
     *
     */
    static final AtomicReference<Supplier<IRootsSolver>> FASTSOLVER = new AtomicReference<>(() -> new MullerNewtonSolver());
    static final AtomicReference<Supplier<IRootsSolver>> ROBUSTSOLVER = new AtomicReference<>(() -> new RobustMullerNewtonSolver());

}
