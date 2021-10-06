/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.sa.diagnostics;

import java.util.concurrent.atomic.AtomicReference;
import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.Builder
@Development(status = Development.Status.Release)
public class AdvancedResidualSeasonalityDiagnosticsConfiguration {
    
    private static AtomicReference<AdvancedResidualSeasonalityDiagnosticsConfiguration> DEFAULT
            =new AtomicReference<AdvancedResidualSeasonalityDiagnosticsConfiguration>(builder().build());
    
    public static void setDefault(AdvancedResidualSeasonalityDiagnosticsConfiguration config){
        DEFAULT.set(config);
    }
    
    public static AdvancedResidualSeasonalityDiagnosticsConfiguration getDefault(){
        return DEFAULT.get();
    }

    public static final double SEV = .001, BAD = .01, UNC = .05;

    private double severeThreshold;
    private double badThreshold;
    private double uncertainThreshold;
    private boolean qs;
    private boolean ftest;

    public static Builder builder(){
        return new Builder()
                .severeThreshold(SEV)
                .badThreshold(BAD)
                .uncertainThreshold(UNC)
                .qs(true)
                .ftest(true);        
   }

    public void check() {
        if (severeThreshold < badThreshold || badThreshold < uncertainThreshold || uncertainThreshold < 0)
                throw new IllegalArgumentException();
    }
}
