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
package jdplus.x13.diagnostics;

import nbbrd.design.Development;
import demetra.sa.SaDiagnosticsFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import jdplus.x13.X13Results;
import nbbrd.service.ServiceProvider;
import demetra.processing.Diagnostics;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public class MDiagnosticsFactory implements SaDiagnosticsFactory<MDiagnosticsConfiguration, X13Results> {
    
    public static final String NAME = "M-Statistics";
    public static final String Q="q", Q2="q2";
    public static final List<String> ALL=Collections.unmodifiableList(Arrays.asList(Q, Q2));
    private final MDiagnosticsConfiguration config;
    private final boolean active;

     public MDiagnosticsFactory(boolean active, MDiagnosticsConfiguration config) {
        this.config = config;
        this.active=true;
    }

    @Override
    public MDiagnosticsConfiguration getConfiguration() {
        return config;
    }

    @Override
    public MDiagnosticsFactory with(boolean active, MDiagnosticsConfiguration config){
        return new MDiagnosticsFactory(active, config);
    }
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public List<String> getTestDictionary() {
         return ALL.stream().map(s->s+":2").collect(Collectors.toList());
   }
    
    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public Diagnostics of(X13Results rslts) {
         return MDiagnostics.of(config, rslts);
    }

    @Override
    public Scope getScope() {
        return Scope.Decomposition;
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
