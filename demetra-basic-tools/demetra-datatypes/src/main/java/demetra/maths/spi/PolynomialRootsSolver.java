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
package demetra.maths.spi;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import demetra.design.Development;
import demetra.design.PrototypePattern;
import demetra.design.ServiceDefinition;
import demetra.design.ThreadSafe;
import demetra.maths.ComplexType;
import demetra.maths.PolynomialType;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@ServiceDefinition(isSingleton = true)
public interface PolynomialRootsSolver {

    /**
     * The method tries to factorize the polynomial passed as a parameter.
     *
     * @param p
     * @return True if it succeeded, even partly
     */
    boolean factorize(PolynomialType p);

    /**
     * The ratio between the original polynomial and the identified roots
     *
     * @return Equals to the initial polynomial if the factorization didn't
     * succeed
     */
    PolynomialType remainder();

    /**
     * The found roots
     *
     * @return Null if the factorization didn't success
     */
    ComplexType[] roots();

}