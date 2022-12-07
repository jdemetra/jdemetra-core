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
import demetra.processing.DiagnosticsConfiguration;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class ResidualTradingDaysDiagnosticsConfiguration implements DiagnosticsConfiguration {
    
    private static final AtomicReference<ResidualTradingDaysDiagnosticsConfiguration> DEFAULT
            = new AtomicReference<>(builder().build());
    
    public static void setDefault(ResidualTradingDaysDiagnosticsConfiguration config) {
        DEFAULT.set(config);
    }
    
    public static ResidualTradingDaysDiagnosticsConfiguration getDefault() {
        return DEFAULT.get();
    }
    
    public static final boolean ACTIVE = true, MONTHLY=true;
    private boolean active;
    
    public static final double SEV = .001, BAD = .01, UNC = .05;
    
    private boolean monthlyOnly;
    private double severeThreshold;
    private double badThreshold;
    private double uncertainThreshold;
    
    public static Builder builder() {
        return new Builder()
                .monthlyOnly(MONTHLY)
                .active(ACTIVE)
                .severeThreshold(SEV)
                .badThreshold(BAD)
                .uncertainThreshold(UNC);
    }
    
    public void check() {
        if (severeThreshold > badThreshold || badThreshold > uncertainThreshold || uncertainThreshold > 1 || severeThreshold <= 0) {
            throw new DemetraException("Invalid settings in thresholds");
        }
    }
    
    @Override
    public DiagnosticsConfiguration activate(boolean active) {
        if (this.active == active) {
            return this;
        } else {
            return toBuilder().active(active).build();
        }
    }
    
}
