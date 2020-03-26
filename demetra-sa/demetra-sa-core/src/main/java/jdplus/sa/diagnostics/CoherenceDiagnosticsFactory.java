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
public class CoherenceDiagnosticsFactory implements DiagnosticsFactory<ProcResults> {

    public static final String DEF = "definition", BIAS = "annual totals";
    public static final String NAME = "Basic checks";
    public static final List<String> ALL = Collections.unmodifiableList(Arrays.asList(DEF, BIAS));
//    public static final CoherenceDiagnosticsFactory Default = new CoherenceDiagnosticsFactory();
    private final CoherenceDiagnosticsConfiguration config;
    private boolean enabled;

    public CoherenceDiagnosticsFactory() {
         config = CoherenceDiagnosticsConfiguration.DEFAULT;
    }

    public CoherenceDiagnosticsFactory(CoherenceDiagnosticsConfiguration config) {
        this.config = config;
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
    public Diagnostics create(ProcResults rslts) {
        return CoherenceDiagnostics.create(rslts, config);
    }

    
}
