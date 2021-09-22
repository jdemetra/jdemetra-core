/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved 
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
package jdplus.seats.diagnostics;

import java.util.concurrent.atomic.AtomicReference;
import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.Builder
@Development(status = Development.Status.Release)
public class SeatsDiagnosticsConfiguration {

   private static AtomicReference<SeatsDiagnosticsConfiguration> DEFAULT
            =new AtomicReference<SeatsDiagnosticsConfiguration>(builder().build());
    
    public static void setDefault(SeatsDiagnosticsConfiguration config){
        DEFAULT.set(config);
    }
    
    public static SeatsDiagnosticsConfiguration getDefault(){
        return DEFAULT.get();
    }

    public static final double BAD = .005, UNC = .05;

    private double badThreshold;
    private double uncertainThreshold;

    public void check() {
        if (badThreshold > uncertainThreshold || uncertainThreshold > 1 || badThreshold <= 0) {
            throw new IllegalArgumentException("Invalid settings in Seats diagnostics");
        }
    }

    public static Builder builder() {
        return new Builder()
                .badThreshold(BAD)
                .uncertainThreshold(UNC);
    }

}
