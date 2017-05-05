package demetra.maths;

import demetra.design.Development;

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



/**
 * Some useful constants. The classes "Double" and "Math" provide other
 * important constants. For compatibility issues, this class continues to
 * provide (deprecated) wrappers around some constants of Double.
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class Constants {

    // other useful constants
    public static final double MAXLOG = 7.09782712893383996732e2;
    public static final double MINLOG = -7.451332191019412076235e2;
    /**
     * 2*PI
     */
    public static final double TWOPI = 2 * Math.PI;
    /**
     * PI/2
     */
    public static final double PIO2 = Math.PI / 2;
    /**
     * sqrt(2*pi)
     */
    public static final double SQTPI = 2.50662827463100050242e0;
    public static final double SQRTH = 7.07106781186547524401e-1;
    public static final double LOGPI = 1.14472988584940017414;

    public static final double BIG = 4.503599627370496e15;
    public static final double BIGINV = 2.22044604925031308085e-16;
    public static final double MACHEP = 1.11022302462515654042E-16;

    /**
     * Relative machine precision
     *
     * @return the value
     */
    public static double getEpsilon() {
        return MACHEP;
    }

    /**
     * Safe minimum, such that 1/(safe minimum) does not overflow.
     *
     * @return
     */
    @Deprecated
    public static double getSafeMinimum() {
        return Double.MIN_NORMAL;
    }

    @Deprecated
    public static double getMaxExponent() {
        return Double.MAX_EXPONENT;
    }

    @Deprecated
    public static double getMinExponent() {
        return Double.MIN_EXPONENT;
    }

    @Deprecated
    public static double getOverflowThreshold() {
        return Double.MAX_VALUE;
    }

    @Deprecated
    public static double getUnderflowThreshold() {
        return Double.MIN_NORMAL;
    }

    @Deprecated
    public static boolean isRounding() {
        return true;
    }
}
