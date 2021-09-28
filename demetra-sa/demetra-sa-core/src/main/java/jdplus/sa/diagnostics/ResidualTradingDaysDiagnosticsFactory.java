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

import demetra.sa.SaDiagnosticsFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import demetra.processing.Diagnostics;

/**
 *
 * @author Jean Palate
 */
public class ResidualTradingDaysDiagnosticsFactory<R> implements SaDiagnosticsFactory<ResidualTradingDaysDiagnosticsConfiguration, R> {
    
    static final String NAME="Residual trading days tests", DESC="Residual trading days tests";
    static final String FTEST_SA = "F-Test on SA (td)", FTEST_I = "F-Test on I (td)";
    static final List<String> ALL = Collections.unmodifiableList(Arrays.asList(FTEST_SA, FTEST_I));

    private final ResidualTradingDaysDiagnosticsConfiguration config;
    private final Function<R, ResidualTradingDaysDiagnostics.Input> extractor;
    private final boolean active;

    public ResidualTradingDaysDiagnosticsFactory(boolean active, ResidualTradingDaysDiagnosticsConfiguration config, Function<R, ResidualTradingDaysDiagnostics.Input> extractor) {
        this.config = config;
        this.extractor=extractor;
        this.active=active;
    }

    @Override
    public ResidualTradingDaysDiagnosticsConfiguration getConfiguration() {
        return config;
    }

    @Override
    public ResidualTradingDaysDiagnosticsFactory<R> with(boolean active, ResidualTradingDaysDiagnosticsConfiguration config){
        return new ResidualTradingDaysDiagnosticsFactory(active, config, extractor);
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
    public boolean isActive() {
        return active;
    }

    @Override
    public Diagnostics of(R rslts) {
        return ResidualTradingDaysDiagnostics.of(config, extractor.apply(rslts));
    }

    @Override
    public Scope getScope() {
        return Scope.Final; 
    }

    @Override
    public int getOrder() {
        return 100; 
    }

}
