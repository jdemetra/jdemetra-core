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
package jdplus.sa.diagnostics;

import demetra.design.Development;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
@Development(status = Development.Status.Release)
public class CoherenceDiagnosticsConfiguration {

    public static CoherenceDiagnosticsConfiguration DEFAULT = builder().build();

    public static final double TOL = 1e-3, ERR = .5, SEV = .1, BAD = .05, UNC = .01;
    public static final int SHORT = 7;

    private double tolerance;
    private double errorThreshold;
    private double severeThreshold;
    private double badThreshold;
    private double uncertainThreshold;
    private int shortSeriesLimit;

    public static Builder builder() {
        return new Builder()
                .tolerance(TOL)
                .errorThreshold(ERR)
                .severeThreshold(SEV)
                .badThreshold(BAD)
                .uncertainThreshold(UNC)
                .shortSeriesLimit(SHORT);
    }

    public void check() {
        if (errorThreshold < severeThreshold || severeThreshold < badThreshold || badThreshold < uncertainThreshold || uncertainThreshold < 0) {
            throw new IllegalArgumentException("Invalid settings in Annual totals diagnostics");
        }
    }
}
