/*
 * Copyright 2013-2014 National Bank of Belgium
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

import demetra.DemetraException;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class ResidualTradingDaysDiagnosticsConfiguration {

    public static final ResidualTradingDaysDiagnosticsConfiguration DEFAULT = builder().build();

    public static final double SEV = .001, BAD = .01, UNC = .05;
    public static final int DEF_NYEARS = 8;

    private double severeThreshold;
    private double badThreshold;
    private double uncertainThreshold;
    private boolean arModel;
    private int spanInYears;

    public static Builder builder() {
        return new Builder()
                .severeThreshold(SEV)
                .badThreshold(BAD)
                .uncertainThreshold(UNC)
                .arModel(true)
                .spanInYears(DEF_NYEARS);
    }

    public void check() {
        if (severeThreshold > badThreshold || badThreshold > uncertainThreshold || uncertainThreshold > 1 || severeThreshold <= 0) {
            throw new DemetraException("Invalid settings in thresholds");
        }
        if (spanInYears < 0) {
            throw new DemetraException("Invalid settings in span");
        }
    }

}
