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

package jdplus.regarima.regular.diagnostics;

import demetra.processing.Diagnostics;
import demetra.processing.DiagnosticsFactory;
import jdplus.regarima.regular.PreprocessingModel;
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
public class OutOfSampleDiagnosticsFactory implements DiagnosticsFactory<PreprocessingModel>  {

    public static final String MEAN = "mean", MSE = "mse";
    public static final String NAME = "Out-of-sample";
    public static final List<String> ALL = Collections.unmodifiableList(Arrays.asList(MEAN, MSE));

    //public static final OutOfSampleDiagnosticsFactory Default = new OutOfSampleDiagnosticsFactory();
    private OutOfSampleDiagnosticsConfiguration config;
    private boolean enabled;

    public OutOfSampleDiagnosticsFactory() {
        config = OutOfSampleDiagnosticsConfiguration.builder().build();
    }

    public OutOfSampleDiagnosticsFactory(OutOfSampleDiagnosticsConfiguration config) {
        this.config = config;
    }

    public OutOfSampleDiagnosticsConfiguration getConfiguration() {
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
        return OutOfSampleDiagnostics.create(config, rslts.getDescription().regarima());
    }

}
