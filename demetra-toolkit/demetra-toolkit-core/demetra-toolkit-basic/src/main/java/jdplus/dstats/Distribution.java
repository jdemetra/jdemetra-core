/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.dstats;

import demetra.stats.ProbabilityType;
import demetra.design.Development;
import jdplus.random.RandomNumberGenerator;

/**
 * Represents a statistical distribution
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public interface Distribution {

    /**
     *  Threshold for identifying quasi-zero values
     */
    public static final double EPS_P = 1e-15, EPS_X=1e-9;

    /**
     * Gets the description of the distribution
     * @return Short description of the distribution
     */
    String getDescription();

    /**
     * Returns E(X); the first moment of the distribution
     * @return Expectation. Can be Double.Nan
     * @throws DStatException
     */
    double getExpectation() throws DStatException;

    /**
     * Returns the lower or upper tail probability of x
     * @param x The value for which the probability is returned
     * @param pt The type of requested probability: lower or upper tail
     * @return The requested probability (double in [0, 1]).
     * @throws DStatException
     */
    double getProbability(double x, ProbabilityType pt) throws DStatException;

    /**
     * Returns the value x that has probability p for the given distribution and
     * probability type
     * @param p The probability
     * @param pt The probability type
     * @return The value x such that P(X &lt x or X &gt x or X = x) = p    
     * @throws DStatException
     */
    double getProbabilityInverse(double p, ProbabilityType pt)
            throws DStatException;

    /**
     * Returns the second moment of the distribution
     * @return The variance of the distribution. Can be Double.Nan
     * @throws DStatException
     */
    double getVariance() throws DStatException;

    /**
     * Indicates whether the distribution is bounded to the left
     * @return The boundary type 
     */
    BoundaryType hasLeftBound();

    /**
     * Indicates whether the distribution is bounded to the right
     * @return The boundary type
     */
    BoundaryType hasRightBound();

    /**
     * Indicates whether the distribution is symmetrical around some central
     * value
     * @return True if the distribution is symmetrical, false otherwise
     */
    boolean isSymmetrical();

    /**
     * Generates a random value from the given distribution
     * @param rng the random number generator used to create the value
     * @return The random number
     * @throws DStatException
     */
    double random(RandomNumberGenerator rng) throws DStatException;
}
