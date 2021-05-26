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

import internal.timeseries.LombokHelper;
import nbbrd.design.LombokWorkaround;
import nbbrd.design.StaticFactoryMethod;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author Jean Palate
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public class TsCollection implements TsResource<TsSeq> {

    @lombok.NonNull
    private TsMoniker moniker;

    @lombok.NonNull
    private TsInformationType type;

    @lombok.NonNull
    private String name;

    @lombok.Singular("meta")
    private Map<String, String> meta;

    @lombok.NonNull
    private TsSeq data;

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .moniker(TsMoniker.NULL)
                .type(TsInformationType.UserDefined)
                .data(TsSeq.EMPTY)
                .name("");
    }

    public static final TsCollection EMPTY = TsCollection.builder().build();

    @StaticFactoryMethod
    public static @NonNull TsCollection of(@NonNull TsSeq data) {
        return builder().data(data).build();
    }

    public static class Builder implements TsResource<TsSeq> {

        @Override
        public TsMoniker getMoniker() {
            return moniker;
        }

        @Override
        public TsInformationType getType() {
            return type;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Map<String, String> getMeta() {
            return LombokHelper.getMap(meta$key, meta$value);
        }

        @Override
        public TsSeq getData() {
            return data;
        }
    }

    public static @NonNull Collector<Ts, ?, TsCollection> toTsCollection() {
        return Collectors.collectingAndThen(TsSeq.toTsSeq(), TsCollection::of);
    }
}
