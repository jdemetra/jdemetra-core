/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.sarima;

import demetra.maths.linearfilters.BackFilter;
import demetra.maths.polynomials.Polynomial;
import demetra.maths.polynomials.UnitRoots;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class SarimaUtility {
    // Optimization. Default differencing filters
    private static final BackFilter m10 = differencingFilter(12, 1, 0), m20 = differencingFilter(12, 2, 0), m01 = differencingFilter(12, 0, 1),
            m11 = differencingFilter(12, 1, 1), q10 = differencingFilter(4, 1, 0), q20 = differencingFilter(4, 2, 0), q01 = differencingFilter(4, 0, 1), q11 = differencingFilter(4, 1, 1);

    public static BackFilter differencingFilter(int freq, int d, int bd) {
        Polynomial X = null;
        if (d > 0) {
            X = UnitRoots.D(1, d);
        }
        if (bd > 0) {
            Polynomial XD = UnitRoots.D(freq, bd);
            if (X == null) {
                X = XD;
            } else {
                X = X.times(XD);
            }
        }
        if (X == null) {
            X = Polynomial.ONE;
        }
        return new BackFilter(X);

    }
    
    /**
     *
     * @return
     */
    public BackFilter getDifferencingFilter(SarimaSpecification spec) {
        // search in the pre-specified filters
        if (spec.D == 0 && spec.BD == 0) {
            return BackFilter.ONE;
        }
        if (spec.frequency == 12) {
            if (spec.BD == 0) {
                if (spec.D == 1) {
                    return m10;
                } else if (spec.D == 2) {
                    return m20;
                }
            } else if (spec.BD == 1) {
                if (spec.D == 0) {
                    return m01;
                } else if (spec.D == 1) {
                    return m11;
                }
            } else if (spec.frequency == 4) {
                if (spec.BD == 0) {
                    if (spec.D == 1) {
                        return q10;
                    } else if (spec.D == 2) {
                        return q20;
                    }
                } else if (spec.BD == 1) {
                    if (spec.D == 0) {
                        return q01;
                    } else if (spec.D == 1) {
                        return q11;
                    }
                }
            }
        }
        return differencingFilter(spec.frequency, spec.D, spec.BD);
    }
    /**
     *
     * @param spec
     * @return
     */
    public UnitRoots getUnitRoots(SarimaSpecification spec) {
        UnitRoots ur = new UnitRoots();
        if (spec.frequency > 1) {
            for (int i = 0; i < spec.BD; ++i) {
                ur.add(spec.frequency);
            }
        }
        for (int i = 0; i < spec.D; ++i) {
            ur.add(1);
        }
        return ur;
    }
}
