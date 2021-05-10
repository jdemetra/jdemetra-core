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
package demetra.timeseries;

import demetra.processing.ProcSpecification;
import java.util.Map;

/**
 *
 * @author PALATEJ
 * @param <I>
 * @param <R>
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public class TsDocument<I extends ProcSpecification, R> {

    @lombok.NonNull
    @lombok.Singular("meta")
    private Map<String, String> meta;

    @lombok.NonNull
    private I specification;

    private Ts input;

    private R result;
}
