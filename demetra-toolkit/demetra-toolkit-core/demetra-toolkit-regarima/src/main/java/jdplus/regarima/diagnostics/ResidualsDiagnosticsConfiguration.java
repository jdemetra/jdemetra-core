/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.regarima.diagnostics;

import demetra.processing.DiagnosticsConfiguration;
import java.util.concurrent.atomic.AtomicReference;
import nbbrd.design.LombokWorkaround;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class ResidualsDiagnosticsConfiguration implements DiagnosticsConfiguration {

    private static AtomicReference<ResidualsDiagnosticsConfiguration> DEFAULT
            = new AtomicReference<>(builder().build());

    public static void setDefault(ResidualsDiagnosticsConfiguration config) {
        DEFAULT.set(config);
    }

    public static ResidualsDiagnosticsConfiguration getDefault() {
        return DEFAULT.get();
    }

    public static final boolean ACTIVE = true;
    private boolean active;

    public static final double NBAD = .01, NUNC = .1,
            TDSEV = .001, TDBAD = .01, TDUNC = .1,
            SSEV = .001, SBAD = .01, SUNC = .1;

    private double badThresholdForNormality, uncertainThresholdForNormality;
    private double severeThresholdForTradingDaysPeak, badThresholdForTradingDaysPeak,
            uncertainThresholdForTradingDaysPeak;
    private double severeThresholdeForSeasonalPeaks,
            badThresholdeForSeasonalPeaks,
            uncertainThresholdeForSeasonalPeaks;

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .active(ACTIVE)
                .badThresholdForNormality(NBAD)
                .uncertainThresholdForNormality(NUNC)
                .severeThresholdForTradingDaysPeak(TDSEV)
                .badThresholdForTradingDaysPeak(TDBAD)
                .uncertainThresholdForTradingDaysPeak(TDUNC)
                .severeThresholdeForSeasonalPeaks(SSEV)
                .badThresholdeForSeasonalPeaks(SBAD)
                .uncertainThresholdeForSeasonalPeaks(SUNC);
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
