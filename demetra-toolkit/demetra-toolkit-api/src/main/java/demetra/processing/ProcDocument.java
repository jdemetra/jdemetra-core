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
package demetra.processing;

import demetra.information.Explorable;
import demetra.util.Documented;
import java.util.Map;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author PALATEJ
 * @param <S>
 * @param <I>
 * @param <R>
 */
public interface ProcDocument<S extends ProcSpecification, I, R extends Explorable> extends Documented {

    public static final String INPUT = "input", SPEC = "specification", ALGORITHM = "algorithm", RESULTS = "results", METADATA = "metadata";
    public static final String ERROR = "@error";

    I getInput();

    S getSpecification();

    ProcessingStatus getStatus();

    R getResult();

    void set(S newSpec, I newInput);

    default void set(S newSpec) {
        set(newSpec, getInput());
    }

    default void set(I newInput) {
        set(getSpecification(), newInput);
    }

    void setMetadata(Map<String, String> newMetadata);

    void updateMetadata(Map<String, String> update);

    ProcessingStatus process();

    @NonNull
    UUID getKey();

}
