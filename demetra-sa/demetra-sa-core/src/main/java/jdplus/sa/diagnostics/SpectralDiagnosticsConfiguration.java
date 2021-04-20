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

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder
public class SpectralDiagnosticsConfiguration {

    public static SpectralDiagnosticsConfiguration DEFAULT = builder().build();

    public static final double SENSITIVITY = 6.0 / 52;
    public static final int LENGTH = 8;

    private double sensitivity;
    private int length;
    private boolean strict;

    public static Builder builder() {
        return new Builder()
                .sensitivity(SENSITIVITY)
                .length(LENGTH)
                .strict(false);
    }

    public void check() {
        if (sensitivity < 3.0 / 52) {
            throw new IllegalArgumentException("Value is too low (should be grater than 3/52)");
        }
        if (length != 0 && length < 6) {
            throw new IllegalArgumentException("Value is too low (should be 0 or greater than 5)");
        }
    }

}
