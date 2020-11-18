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
package jdplus.regarima.diagnostics;

import nbbrd.design.LombokWorkaround;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class OutOfSampleDiagnosticsConfiguration {

    public static final double BAD = .01, UNC = .1, LENGTH = 1.5;
    public static OutOfSampleDiagnosticsConfiguration DEFAULT = builder().build();

    private double badThreshold;
    private double uncertainThreshold;
    private boolean diagnosticOnMean, diagnosticOnVariance;
    private double outOfSampleLength;

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .badThreshold(BAD)
                .uncertainThreshold(UNC)
                .diagnosticOnMean(true)
                .diagnosticOnVariance(false)
                .outOfSampleLength(LENGTH);
    }
}
