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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import jdplus.regarima.RegArimaModel;
import jdplus.sarima.SarimaModel;

/**
 *
 * @author Jean Palate
 * @param <R>
 */
public class OutOfSampleDiagnosticsFactory<R> implements DiagnosticsFactory<R>  {

    public static final String MEAN = "mean", MSE = "mse";
    public static final String NAME = "Out-of-sample";
    public static final List<String> ALL = Collections.unmodifiableList(Arrays.asList(MEAN, MSE));

    //public static final OutOfSampleDiagnosticsFactory Default = new OutOfSampleDiagnosticsFactory();
    private final OutOfSampleDiagnosticsConfiguration config;
    private final Function<R, RegArimaModel<SarimaModel> > extractor;
    private boolean enabled=true;

    public OutOfSampleDiagnosticsFactory(OutOfSampleDiagnosticsConfiguration config, Function<R, RegArimaModel<SarimaModel> > extractor) {
        this.config = config;
        this.extractor=extractor;
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
    public Diagnostics of(R rslts) {
        return OutOfSampleDiagnostics.create(config, extractor.apply(rslts));
    }

}
