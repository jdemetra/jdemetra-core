/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved 
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
package demetra.dstats;

import demetra.design.Development;

/**
 * Represents a discrete probability distribution (e.g. Binomial, Poisson, ...)
 * I.e. the domain is a set of discrete values
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
public interface IDiscreteDistribution extends IDistribution {
    /**
     * Returns the left bound (if any). Throws an exception otherwise
     * 
     * @return
     */
    long getLeftBound();

    /**
     * Returns the right bound (if any). Throws an exception otherwise
     * 
     * @return
     */
    long getRightBound();
}
