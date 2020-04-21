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

import demetra.design.Development;
import demetra.processing.Diagnostics;
import demetra.processing.DiagnosticsFactory;
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
@Development(status = Development.Status.Release)
public class AdvancedResidualSeasonalityDiagnosticsFactory<R> implements DiagnosticsFactory<R> {

    static final String NAME = "Residual seasonality tests", DESC = "Residual seasonality tests";
    static final String QS_SA = "Qs test on SA", QS_I = "Qs test on I", FTEST_SA = "F-Test on SA (seasonal dummies)", FTEST_I = "F-Test on I (seasonal dummies)";
    static final List<String> ALL = Collections.unmodifiableList(Arrays.asList(QS_SA, QS_I, FTEST_SA, FTEST_I));

    private final AdvancedResidualSeasonalityDiagnosticsConfiguration config;
    private final Function<R, AdvancedResidualSeasonalityDiagnostics.Input> extractor;
    private boolean enabled=true;

    public AdvancedResidualSeasonalityDiagnosticsFactory(AdvancedResidualSeasonalityDiagnosticsConfiguration config, 
            Function<R, AdvancedResidualSeasonalityDiagnostics.Input> extractor) {
        this.config = config;
        this.extractor = extractor;
    }

    public AdvancedResidualSeasonalityDiagnosticsConfiguration getConfiguration() {
        return config;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public List<String> getTestDictionary() {
        return ALL.stream().map(s -> s + ":2").collect(Collectors.toList());
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public Diagnostics of(R rslts) {
        return AdvancedResidualSeasonalityDiagnostics.of(config, extractor.apply(rslts));
    }

}
