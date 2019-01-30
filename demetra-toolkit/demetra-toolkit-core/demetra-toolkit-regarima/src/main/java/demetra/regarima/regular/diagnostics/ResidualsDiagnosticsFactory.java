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

package demetra.regarima.regular.diagnostics;

import demetra.regarima.regular.PreprocessingModel;
import demetra.processing.DiagnosticsFactory;
import demetra.processing.Diagnostics;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = DiagnosticsFactory.class)
public class ResidualsDiagnosticsFactory implements DiagnosticsFactory<PreprocessingModel> {

    public static final String NORMALITY = "normality", INDEPENDENCE = "independence",
        TD_PEAK = "spectral td peaks", S_PEAK = "spectral seas peaks";

    public static final String NAME = "Regarima residuals";

    public static List<String> ALL = Collections.unmodifiableList(Arrays.asList(NORMALITY, INDEPENDENCE, TD_PEAK, S_PEAK));

    private ResidualsDiagnosticsConfiguration config;
    private boolean enabled=true;
    //public static final ResidualsDiagnosticsFactory Default = new ResidualsDiagnosticsFactory();

    public ResidualsDiagnosticsFactory() {
        config = ResidualsDiagnosticsConfiguration.builder().build();
    }

    public ResidualsDiagnosticsFactory(ResidualsDiagnosticsConfiguration config) {
        this.config = config;
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
    public Diagnostics create(PreprocessingModel rslts) {
        return ResidualsDiagnostics.create(config, rslts);
    }

}
