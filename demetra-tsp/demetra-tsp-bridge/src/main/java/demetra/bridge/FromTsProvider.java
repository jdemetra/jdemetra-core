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

import demetra.timeseries.Ts;
import demetra.timeseries.TsCollection;
import demetra.timeseries.TsProvider;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.util.Objects;

/**
 * @author Philippe Charles
 */
@lombok.extern.slf4j.Slf4j
public class FromTsProvider extends ec.tss.tsproviders.utils.AbstractTsProvider implements ec.tss.ITsProvider {

    public static ec.tss.@NonNull ITsProvider fromTsProvider(@NonNull TsProvider delegate) {
        return delegate instanceof ToTsProvider
                ? ((ToTsProvider) delegate).getDelegate()
                : new FromTsProvider(delegate);
    }

    @lombok.Getter
    @lombok.NonNull
    private final TsProvider delegate;

    protected FromTsProvider(TsProvider delegate) {
        super(log, delegate.getSource(), ec.tss.TsAsyncMode.Once);
        this.delegate = delegate;
    }

    @Override
    public void clearCache() {
        getDelegate().clearCache();
    }

    @Override
    public void dispose() {
        super.dispose();
        getDelegate().close();
    }

    @Override
    public boolean queryTs(ec.tss.@NonNull TsMoniker moniker, ec.tss.@NonNull TsInformationType type) {
        Objects.requireNonNull(moniker, "Moniker cannot be null");
        Objects.requireNonNull(type, "Type cannot be null");
        return super.queryTs(moniker, type);
    }

    @Override
    public boolean queryTsCollection(ec.tss.@NonNull TsMoniker moniker, ec.tss.@NonNull TsInformationType type) {
        Objects.requireNonNull(moniker, "Moniker cannot be null");
        Objects.requireNonNull(type, "Type cannot be null");
        return super.queryTsCollection(moniker, type);
    }

    @Override
    protected boolean process(ec.tss.@NonNull TsCollectionInformation info) {
        try {
            TsCollection result = getDelegate().getTsCollection(TsConverter.toTsMoniker(info.moniker), TsConverter.toType(info.type));
            TsConverter.fillTsCollectionInformation(result, info);
        } catch (IOException | IllegalArgumentException | ConverterException ex) {
            info.invalidDataCause = ex.getMessage();
            return false;
        }
        return true;
    }

    @Override
    protected boolean process(ec.tss.@NonNull TsInformation info) {
        try {
            Ts result = getDelegate().getTs(TsConverter.toTsMoniker(info.moniker), TsConverter.toType(info.type));
            TsConverter.fillTsInformation(result, info);
        } catch (IOException | IllegalArgumentException | ConverterException ex) {
            info.invalidDataCause = ex.getMessage();
            return false;
        }
        return true;
    }
}
