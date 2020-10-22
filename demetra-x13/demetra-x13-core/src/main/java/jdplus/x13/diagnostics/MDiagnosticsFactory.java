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
import demetra.processing.Diagnostics;
import demetra.sa.SaDiagnosticsFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import jdplus.x13.X13Results;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public class MDiagnosticsFactory implements SaDiagnosticsFactory<X13Results> {
    
    public static final String NAME = "M-Statistics";
    public static final String Q="q", Q2="q2";
    public static final List<String> ALL=Collections.unmodifiableList(Arrays.asList(Q, Q2));
    //public static final MDiagnosticsFactory Default = new MDiagnosticsFactory();
    private MDiagnosticsConfiguration config;
    private boolean enabled = true;

     public MDiagnosticsFactory(MDiagnosticsConfiguration config) {
        config = config;
    }

    public MDiagnosticsConfiguration getConfiguration() {
        return config;
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
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled=enabled;
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
