package _util;

import demetra.bridge.TsConverter;
import ec.tss.tsproviders.IDataSourceLoader;

public final class MockedDataSourceLoaderV2 implements IDataSourceLoader {

    @lombok.experimental.Delegate
    private final IDataSourceLoader delegate = (IDataSourceLoader) TsConverter.fromTsProvider(new MockedDataSourceLoader());
}
