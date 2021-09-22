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
import demetra.processing.DiagnosticsFactory;
import demetra.sa.SaDiagnosticsFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Jean Palate
 * @param <R>
 */
@Development(status = Development.Status.Release)
public class CoherenceDiagnosticsFactory<R> implements SaDiagnosticsFactory<CoherenceDiagnosticsConfiguration, R> {

    public static final String DEF = "definition", BIAS = "annual totals";
    public static final String NAME = "Basic checks";
    public static final List<String> ALL = Collections.unmodifiableList(Arrays.asList(DEF, BIAS));
//    public static final CoherenceDiagnosticsFactory Default = new CoherenceDiagnosticsFactory();
    private final CoherenceDiagnosticsConfiguration config;
    private final Function<R, CoherenceDiagnostics.Input> extractor;
    private final boolean active;

    public CoherenceDiagnosticsFactory(boolean active, @NonNull CoherenceDiagnosticsConfiguration config,
            @NonNull Function<R, CoherenceDiagnostics.Input> extractor) {
        this.config = config;
        this.extractor = extractor;
        this.active=active;
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
    public boolean isActive() {
        return active;
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

    @Override
    public CoherenceDiagnosticsConfiguration getConfiguration() {
        return config;
    }

    @Override
    public CoherenceDiagnosticsFactory<R> with(boolean active, @NonNull CoherenceDiagnosticsConfiguration newConfig) {
        return new CoherenceDiagnosticsFactory(active, config, extractor);
    }

}
