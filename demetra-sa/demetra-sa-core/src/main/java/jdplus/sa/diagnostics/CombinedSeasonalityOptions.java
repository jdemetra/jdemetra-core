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
 * TODO: possible additional parameters (detrending method...)
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
@Development(status = Development.Status.Release)
public class CombinedSeasonalityOptions {

    private static final int DEF_LAST_YEARS = 3;

    private static AtomicReference<CombinedSeasonalityOptions> DEFAULT
            = new AtomicReference<CombinedSeasonalityOptions>(builder().build());

    public static void setDefault(CombinedSeasonalityOptions config) {
        DEFAULT.set(config);
    }

    public static CombinedSeasonalityOptions getDefault() {
        return DEFAULT.get();
    }

    private int lastYears;

    public static Builder builder() {
        return new Builder().lastYears(DEF_LAST_YEARS);
    }
}
