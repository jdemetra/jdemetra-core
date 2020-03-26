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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import jdplus.regsarima.regular.ModelEstimation;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Kristof Bayens
 */
@ServiceProvider(DiagnosticsFactory.class)
public class OutliersDiagnosticsFactory implements DiagnosticsFactory<ModelEstimation> {

    public static final String NUMBER = "number of outliers";
    public static final String NAME = "Outliers";
    public static final List<String> ALL = Collections.singletonList(NUMBER);
    private final OutliersDiagnosticsConfiguration config;
    private boolean enabled=true;

    public OutliersDiagnosticsFactory() {
        config = OutliersDiagnosticsConfiguration.DEFAULT;
    }

    public OutliersDiagnosticsFactory(OutliersDiagnosticsConfiguration config) {
        this.config = config;
    }

    public OutliersDiagnosticsConfiguration getConfiguration() {
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
    public Diagnostics create(ModelEstimation rslts) {
        return OutliersDiagnostics.create(rslts, config);
    }
}
