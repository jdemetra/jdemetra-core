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
package ec.tstoolkit.random;

import ec.tstoolkit.design.Development;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public final class StochasticRandomizer {

    private StochasticRandomizer() {
        // static class
    }
    
    /**
     * 
     * @param degreesoffreedom
     * @return
     */
    public static double chi2(IRandomNumberGenerator rng, final int degreesoffreedom) {
	double r = 0;
	for (int i = 0; i < degreesoffreedom; ++i) {
	    double n = normal(rng);
	    r += n * n;
	}
	return r;
    }

    /**
     * 
     * @param dfnum
     * @param dfdenom
     * @return
     */
    public static double F(IRandomNumberGenerator rng, final int dfnum, final int dfdenom) {
	return chi2(rng, dfnum) / dfnum / (chi2(rng, dfdenom) / dfdenom);
    }

    /**
     * 
     * @return
     */
    public static double normal(IRandomNumberGenerator rng) {
	double x1, x2, w;
	do {
	    x1 = 2 * rng.nextDouble() - 1;
	    x2 = 2 * rng.nextDouble() - 1;
	    w = x1 * x1 + x2 * x2;
	} while (w >= 1 || w < 1E-30);
	w = Math.sqrt((-2 * Math.log(w)) / w);
	x1 *= w;
	return x1;
    }

    /**
     * 
     * @param mean
     * @param stdev
     * @return
     */
    public static double normal(IRandomNumberGenerator rng, final double mean, final double stdev) {
	return normal(rng) * stdev + mean;
    }

    /**
     * 
     * @param degreesoffreedom
     * @return
     */
    public static double T(IRandomNumberGenerator rng, final int degreesoffreedom) {
	return normal(rng) / Math.sqrt(chi2(rng, degreesoffreedom) / degreesoffreedom);
    }

    /**
     * 
     * @param a
     * @param b
     * @return
     */
    public static double uniform(IRandomNumberGenerator rng, final double a, final double b) {
	return a + rng.nextDouble() * (b - a);
    }

}
