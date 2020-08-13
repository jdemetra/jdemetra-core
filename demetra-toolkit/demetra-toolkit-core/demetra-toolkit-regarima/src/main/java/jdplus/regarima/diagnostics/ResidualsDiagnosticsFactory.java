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

import jdplus.regsarima.regular.ModelEstimation;
import demetra.processing.DiagnosticsFactory;
import demetra.processing.Diagnostics;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author Jean Palate
 * @param <R>
 */
public class ResidualsDiagnosticsFactory<R> implements DiagnosticsFactory<R> {

    public static final String NORMALITY = "normality", INDEPENDENCE = "independence",
        TD_PEAK = "spectral td peaks", S_PEAK = "spectral seas peaks";

    public static final String NAME = "Regarima residuals";

    public static List<String> ALL = Collections.unmodifiableList(Arrays.asList(NORMALITY, INDEPENDENCE, TD_PEAK, S_PEAK));

    private final ResidualsDiagnosticsConfiguration config;
    private final Function<R, ModelEstimation > extractor;
    private boolean enabled=true;

    public ResidualsDiagnosticsFactory(ResidualsDiagnosticsConfiguration config,Function<R, ModelEstimation > extractor) {
        this.config = config;
        this.extractor=extractor;
    }

    public ResidualsDiagnosticsConfiguration getConfiguration() {
        return config;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public List<String> getTestDictionary(){
        return ALL.stream().map(s->s+":2").collect(Collectors.toList());
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled=enabled;
    }

    @Override
    public Diagnostics of(R rslts) {
        return ResidualsDiagnostics.create(config, extractor.apply(rslts));
    }

}
