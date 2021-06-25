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
import demetra.processing.Processor;
import demetra.processing.TsDataProcessorFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;
import demetra.information.Explorable;

/**
 *
 * @author PALATEJ
 * @param <I>
 * @param <R>
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public class TsDocument<I extends ProcSpecification, R> {

    public static final String ERROR = "@error";

    @lombok.NonNull
    @lombok.Singular("meta")
    private Map<String, String> meta;

    @lombok.NonNull
    private I specification;

    private Ts input;

    private R result;

    private Processor.Status status;

    private TsDataProcessorFactory<I, R> processor;

    public TsDocument<I, R> withProcessor(@NonNull TsDataProcessorFactory<I, R> nprocessor) {
        if (nprocessor.equals(processor)) {
            return this;
        }
        return new TsDocument(meta, specification, input, null, Processor.Status.Unprocessed, nprocessor);
    }

    public TsDocument<I, R> process(TsFactory tsFactory) {
        if (status != Processor.Status.Unprocessed) {
            return this;
        }
        if (input == null) {
            throw new IllegalArgumentException("No series");
        }
        if (processor == null) {
            throw new IllegalArgumentException("No processor");
        }
        Ts s = input;
        if (input.getData().isEmpty()) {
            s = tsFactory.makeTs(input.getMoniker(), TsInformationType.BaseInformation);
        }
        try {
            R rslt = processor.generateProcessor(specification).process(s.getData());
            return new TsDocument(meta, specification, s, rslt, Processor.Status.Valid, processor);
        } catch (Exception err) {
            HashMap<String, String> m = new HashMap<>(meta);
            m.put(ERROR, err.getMessage());
            return new TsDocument(Collections.unmodifiableMap(m), specification, s, null, Processor.Status.Invalid, processor);
        }
    }
}
