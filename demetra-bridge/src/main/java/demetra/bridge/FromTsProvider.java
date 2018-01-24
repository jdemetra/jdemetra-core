/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package demetra.bridge;

import demetra.tsprovider.Ts;
import demetra.tsprovider.TsCollection;
import demetra.tsprovider.TsProvider;
import ec.tss.ITsProvider;
import ec.tss.TsAsyncMode;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tss.TsInformationType;
import ec.tss.TsMoniker;
import java.io.IOException;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * @author Philippe Charles
 * @param <T>
 */
@lombok.AllArgsConstructor
public class FromTsProvider<T extends TsProvider> implements ITsProvider {

    private final T delegate;

    public T getDelegate() {
        return delegate;
    }

    @Override
    public void clearCache() {
        getDelegate().clearCache();
    }

    @Override
    public void dispose() {
        getDelegate().close();
    }

    @Override
    public boolean get(TsCollectionInformation info) {
        try {
            TsCollection result = getDelegate().getTsCollection(Converter.toMoniker(info.moniker), Converter.toType(info.type));
            info.name = result.getName();
            info.metaData = Converter.fromMeta(result.getMetaData());
            info.items.addAll(result.getItems().stream().map(Converter::fromTs).collect(Collectors.toList()));
        } catch (IOException | IllegalArgumentException ex) {
            info.invalidDataCause = ex.getMessage();
            return false;
        }
        return true;
    }

    @Override
    public boolean get(TsInformation info) {
        try {
            Ts result = getDelegate().getTs(Converter.toMoniker(info.moniker), Converter.toType(info.type));
            info.name = result.getName();
            info.metaData = Converter.fromMeta(result.getMetaData());
            info.data = Converter.fromTsData(result.getData());
            info.invalidDataCause = result.getData().isEmpty() ? result.getData().getCause() : null;
        } catch (IOException | IllegalArgumentException ex) {
            info.invalidDataCause = ex.getMessage();
            return false;
        }
        return true;
    }

    @Override
    public TsAsyncMode getAsyncMode() {
        return TsAsyncMode.Once;
    }

    @Override
    public String getSource() {
        return getDelegate().getSource();
    }

    @Override
    public boolean queryTs(TsMoniker ts, TsInformationType type) {
        Objects.requireNonNull(ts);
        Objects.requireNonNull(type);
        return true;
    }

    @Override
    public boolean queryTsCollection(TsMoniker collection, TsInformationType info) {
        Objects.requireNonNull(collection);
        Objects.requireNonNull(info);
        return true;
    }

    @Override
    public void close() {
        getDelegate().close();
    }
}
