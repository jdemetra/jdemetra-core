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
import java.util.stream.Collectors;
import demetra.information.Explorable;

/**
 *
 * @author Kristof Bayens
 */
public class ResidualSeasonalityDiagnosticsFactory implements DiagnosticsFactory<ResidualSeasonalityDiagnosticsConfiguration, Explorable> {

    public static final String NAME = "combined seasonality test",
            SA = NAME + " on sa", SA_LAST = NAME + " on sa (last 3 years)", IRR = NAME + " on irregular";
    public static final List<String> ALL = Collections.unmodifiableList(Arrays.asList(SA, SA_LAST, IRR));

    private final ResidualSeasonalityDiagnosticsConfiguration config;
    private final boolean active;

    public ResidualSeasonalityDiagnosticsFactory(boolean active, ResidualSeasonalityDiagnosticsConfiguration config) {
        this.config = config;
        this.active=active;
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
    public Diagnostics of(Explorable rslts) {
        return ResidualSeasonalityDiagnostics.create(rslts, config);
    }

    @Override
    public ResidualSeasonalityDiagnosticsConfiguration getConfiguration() {
 return config;   }

    @Override
    public ResidualSeasonalityDiagnosticsFactory with(boolean active, ResidualSeasonalityDiagnosticsConfiguration newConfig) {
        return new ResidualSeasonalityDiagnosticsFactory(active, newConfig);
     }
}
