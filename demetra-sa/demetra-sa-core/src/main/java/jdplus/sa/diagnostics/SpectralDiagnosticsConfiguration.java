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

import demetra.processing.DiagnosticsConfiguration;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder(toBuilder=true, builderClassName="Builder")
public class SpectralDiagnosticsConfiguration implements DiagnosticsConfiguration {

    private static final AtomicReference<SpectralDiagnosticsConfiguration> DEFAULT
            =new AtomicReference<>(builder().build());
    
    public static void setDefault(SpectralDiagnosticsConfiguration config){
        DEFAULT.set(config);
    }
    
    public static SpectralDiagnosticsConfiguration getDefault(){
        return DEFAULT.get();
    }
    
    public static final boolean ACTIVE=false, STRICT=false;

    public static final double SENSIBILITY = 6.0 / 52;
    public static final int LENGTH = 8;
    
    

    private double sensibility;
    private int length;
    private boolean strict;
    
    private boolean active;

    public static Builder builder() {
        return new Builder()
                .sensibility(SENSIBILITY)
                .length(LENGTH)
                .strict(STRICT)
                .active(ACTIVE);
    }

    public void check() {
        if (sensibility < 3.0 / 52) {
            throw new IllegalArgumentException("Value is too low (should be grater than 3/52)");
        }
        if (length != 0 && length < 6) {
            throw new IllegalArgumentException("Value is too low (should be 0 or greater than 5)");
        }
    }

    @Override
    public boolean isActive() {
        return active;
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
