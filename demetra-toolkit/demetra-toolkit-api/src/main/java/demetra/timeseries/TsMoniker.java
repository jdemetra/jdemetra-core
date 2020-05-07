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
package demetra.timeseries;

import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;
import lombok.AccessLevel;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TsMoniker {

    public static final TsMoniker NULL = TsMoniker.of("", "");

    @NonNull
    public static TsMoniker of() {
        return new TsMoniker("", UUID.randomUUID().toString());
    }

    @NonNull
    public static TsMoniker of(@NonNull String source, @NonNull String id) {
        return new TsMoniker(source, id);
    }

    @lombok.NonNull
    private String source;

    @lombok.NonNull
    private String id;

    public boolean isProvided() {
        return !source.isEmpty();
    }

    public boolean isNull() {
        return source.isEmpty() && id.isEmpty();
    }

    @Override
    public String toString() {
        return isProvided() ? (source + "<@>" + id) : id;
    }
}
