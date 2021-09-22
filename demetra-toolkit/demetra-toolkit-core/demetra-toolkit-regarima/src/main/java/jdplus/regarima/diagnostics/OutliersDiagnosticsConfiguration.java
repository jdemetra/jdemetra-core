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

import java.util.concurrent.atomic.AtomicReference;


/**
 *
 * @author Kristof Bayens
 */
@lombok.Value
@lombok.Builder
public class OutliersDiagnosticsConfiguration{

    private static AtomicReference<OutliersDiagnosticsConfiguration> DEFAULT
            =new AtomicReference<OutliersDiagnosticsConfiguration>(builder().build());
    
    public static void setDefault(OutliersDiagnosticsConfiguration config){
        DEFAULT.set(config);
    }
    
    public static OutliersDiagnosticsConfiguration getDefault(){
        return DEFAULT.get();
    }

    public static final double SEV = .10, BAD = .05, UNC = .03;
    
    private double severeThreshold;
    private double badThreshold;
    private double uncertainThreshold;

    public static Builder builder(){
        return new Builder()
                .severeThreshold(SEV)
                .badThreshold(BAD)
                .uncertainThreshold(UNC);        
   }
    
    public void check() {
        if (severeThreshold < badThreshold || badThreshold < uncertainThreshold || uncertainThreshold < 0)
                throw new IllegalArgumentException();
    }
}
