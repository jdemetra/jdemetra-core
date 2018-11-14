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
 * Represents a continuous statistical distribution (Normal, LogNormal, X2, ...)
 * The domain is a continuum
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public interface ContinuousDistribution extends Distribution {

    /**
     * Returns the value of x for the density function describing the
     * distribution
     *
     * @param x The value at which density is to be computed
     * @return density(x).
     * @throws DStatException
     */
    double getDensity(double x) throws DStatException;

    /**
     * Returns the left bound.
     *
     * @return The left bound. Can be Double.NEGATIVE INFINITY
     */
    double getLeftBound();

    /**
     * Returns the probability that the variable belongs to the interval [x,y]
     *
     * @param x Lower bound of the interval
     * @param y Upper bound of the interval
     * @return P(X in [x,y]). Belongs to [0, 1].
     * @throws DStatException
     */
    default double getProbabilityForInterval(double x, double y) throws DStatException {
        double l, u;
        if (Double.isFinite(x) && Double.isFinite(y) && x > y){
            l=y;
            u=x;
        }else{
            l=x;
            u=y;
        }
             
        double pu = Double.isFinite(u) ? getProbability(u, ProbabilityType.Lower) : 1;
        double pl = Double.isFinite(l) ? getProbability(l, ProbabilityType.Lower) : 0;

        return pu > pl ? pu - pl : 0;
    }

    /**
     * Returns the right bound.
     *
     * @return The right bound. Can be Double.POSITIVEINFINITY.
     */
    double getRightBound();
}
