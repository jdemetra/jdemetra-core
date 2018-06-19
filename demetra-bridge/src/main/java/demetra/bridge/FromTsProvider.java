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
import ec.tss.tsproviders.utils.AbstractTsProvider;
import java.io.IOException;
import java.util.Objects;

/**
 *
 * @author Philippe Charles
 * @param <T>
 */
@lombok.extern.slf4j.Slf4j
public class FromTsProvider<T extends TsProvider> extends AbstractTsProvider implements ITsProvider {

    private final T delegate;

    public FromTsProvider(T delegate) {
        super(log, delegate.getSource(), TsAsyncMode.Once);
        this.delegate = delegate;
    }

    public T getDelegate() {
        return delegate;
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
    public boolean queryTs(TsMoniker moniker, TsInformationType type) {
        Objects.requireNonNull(moniker, "Moniker cannot be null");
        Objects.requireNonNull(type, "Type cannot be null");
        return super.queryTs(moniker, type);
    }

    @Override
    public boolean queryTsCollection(TsMoniker moniker, TsInformationType type) {
        Objects.requireNonNull(moniker, "Moniker cannot be null");
        Objects.requireNonNull(type, "Type cannot be null");
        return super.queryTsCollection(moniker, type);
    }

    @Override
    protected boolean process(TsCollectionInformation info) {
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
    protected boolean process(TsInformation info) {
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
