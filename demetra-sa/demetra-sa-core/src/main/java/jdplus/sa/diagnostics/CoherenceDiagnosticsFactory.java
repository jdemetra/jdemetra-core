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

import nbbrd.design.Development;
import demetra.processing.Diagnostics;
import demetra.sa.SaDiagnosticsFactory;
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
public class CoherenceDiagnosticsFactory<R> implements SaDiagnosticsFactory<R> {

    public static final String DEF = "definition", BIAS = "annual totals";
    public static final String NAME = "Basic checks";
    public static final List<String> ALL = Collections.unmodifiableList(Arrays.asList(DEF, BIAS));
//    public static final CoherenceDiagnosticsFactory Default = new CoherenceDiagnosticsFactory();
    private final CoherenceDiagnosticsConfiguration config;
    private final Function<R, CoherenceDiagnostics.Input> extractor;
    private boolean enabled=true;

    public CoherenceDiagnosticsFactory(CoherenceDiagnosticsConfiguration config,
            Function<R, CoherenceDiagnostics.Input> extractor) {
        this.config = config;
        this.extractor=extractor;
    }

    public CoherenceDiagnosticsConfiguration getConfiguration() {
        return config;
    }

     @Override
    public List<String> getTestDictionary() {
        return ALL.stream().map(s -> s + ":2").collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return NAME; 
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
        return CoherenceDiagnostics.of(config, extractor.apply(rslts));
    }

    @Override
    public Scope getScope() {
        return Scope.General;
    }

    @Override
    public int getOrder() {
        return 0;
    }
    
}
