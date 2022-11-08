/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved 
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
package jdplus.seats.diagnostics;

import demetra.sa.SaDiagnosticsFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import demetra.processing.Diagnostics;
import jdplus.seats.SeatsTests;

/**
 *
 * @author Kristof Bayens
 */
public class SeatsDiagnosticsFactory<R> implements SaDiagnosticsFactory<SeatsDiagnosticsConfiguration, R> {

    public static final String SEAS_VAR = "seas variance", IRR_VAR = "irregular variance";
    public static final String SEAS_I_CORR = "seas/irr cross-correlation";
    public static final String NOTSAME = "Non decomposable model. Changed by Seats";
    public static final String CUTOFF = "Parameters cut off by Seats";
    public static final String NAME = "Seats";

    public static final List<String> ALL = Collections.unmodifiableList(Arrays.asList(SEAS_VAR, IRR_VAR, SEAS_I_CORR));

    private final SeatsDiagnosticsConfiguration config;
    private final Function<R, SeatsTests> extractor;

    public SeatsDiagnosticsFactory(SeatsDiagnosticsConfiguration config, Function<R, SeatsTests> extractor) {
        this.config = config;
        this.extractor = extractor;
    }

    @Override
    public SeatsDiagnosticsConfiguration getConfiguration() {
        return config;
    }

    @Override
    public SeatsDiagnosticsFactory<R> with(SeatsDiagnosticsConfiguration config) {
        return new SeatsDiagnosticsFactory(config, extractor);
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
    public Diagnostics of(R rslts) {
        return SeatsDiagnostics.of(config, extractor.apply(rslts));
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
