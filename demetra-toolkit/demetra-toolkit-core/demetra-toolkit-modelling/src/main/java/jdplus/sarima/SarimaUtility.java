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
package jdplus.sarima;

import demetra.arima.SarimaSpecification;
import jdplus.maths.linearfilters.BackFilter;
import jdplus.maths.polynomials.Polynomial;
import jdplus.maths.polynomials.UnitRoots;

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
        if (spec.getD() == 0 && spec.getBd() == 0) {
            return BackFilter.ONE;
        }
        if (spec.getPeriod() == 12) {
            if (spec.getBd() == 0) {
                if (spec.getD() == 1) {
                    return m10;
                } else if (spec.getD() == 2) {
                    return m20;
                }
            } else if (spec.getBd() == 1) {
                if (spec.getD() == 0) {
                    return m01;
                } else if (spec.getD() == 1) {
                    return m11;
                }
            } else if (spec.getPeriod() == 4) {
                if (spec.getBd() == 0) {
                    if (spec.getD() == 1) {
                        return q10;
                    } else if (spec.getD() == 2) {
                        return q20;
                    }
                } else if (spec.getBd() == 1) {
                    if (spec.getD() == 0) {
                        return q01;
                    } else if (spec.getD() == 1) {
                        return q11;
                    }
                }
            }
        }
        return differencingFilter(spec.getPeriod(), spec.getD(), spec.getBd());
    }
    /**
     *
     * @param spec
     * @return
     */
    public UnitRoots getUnitRoots(SarimaSpecification spec) {
        UnitRoots ur = new UnitRoots();
        if (spec.getPeriod() > 1) {
            for (int i = 0; i < spec.getBd(); ++i) {
                ur.add(spec.getPeriod());
            }
        }
        for (int i = 0; i < spec.getD(); ++i) {
            ur.add(1);
        }
        return ur;
    }
}
