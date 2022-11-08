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
package jdplus.x13.diagnostics;

import demetra.processing.DiagnosticsConfiguration;
import java.util.concurrent.atomic.AtomicReference;
import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
@Development(status = Development.Status.Release)
public class MDiagnosticsConfiguration implements DiagnosticsConfiguration{

    private static final AtomicReference<MDiagnosticsConfiguration> DEFAULT
            = new AtomicReference<>(builder().build());

    public static void setDefault(MDiagnosticsConfiguration config) {
        DEFAULT.set(config);
    }

    public static MDiagnosticsConfiguration getDefault() {
        return DEFAULT.get();
    }

    public static final boolean ACTIVE = true;
    private boolean active;
    
    public static final double SEVERE = 2, BAD = 1;

    private double badThreshold;
    private double severeThreshold;
    private boolean all;

    public void check() {
        if (badThreshold > severeThreshold || badThreshold < 0 || severeThreshold > 3) {
            throw new IllegalArgumentException("Invalid settings in M-diagnostics");
        }
    }

    public static Builder builder() {
        return new Builder()
                .active(ACTIVE)
                .badThreshold(BAD)
                .severeThreshold(SEVERE)
                .all(true);
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
