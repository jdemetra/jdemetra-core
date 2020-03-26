/*
 * Copyright 2013-2014 National Bank of Belgium
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

import demetra.processing.Diagnostics;
import demetra.processing.DiagnosticsFactory;
import demetra.processing.ProcResults;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(DiagnosticsFactory.class)
public class ResidualTradingDaysDiagnosticsFactory implements DiagnosticsFactory<ProcResults> {
    
    static final String NAME="Residual trading days tests", DESC="Residual trading days tests";
    static final String FTEST_SA = "F-Test on SA (td)", FTEST_I = "F-Test on I (td)";
    static final List<String> ALL = Collections.unmodifiableList(Arrays.asList(FTEST_SA, FTEST_I));

    private final ResidualTradingDaysDiagnosticsConfiguration config;
    private boolean enabled;

    public ResidualTradingDaysDiagnosticsFactory() {
        config = ResidualTradingDaysDiagnosticsConfiguration.DEFAULT;
    }

    public ResidualTradingDaysDiagnosticsFactory(ResidualTradingDaysDiagnosticsConfiguration config) {
        this.config = config;
    }

    public ResidualTradingDaysDiagnosticsConfiguration getConfiguration() {
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
    public Diagnostics create(ProcResults rslts) {
        return ResidualTradingDaysDiagnostics.create(rslts, config);
    }

}
