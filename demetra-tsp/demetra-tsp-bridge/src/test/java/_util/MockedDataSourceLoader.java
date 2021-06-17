package _util;

import demetra.timeseries.TsProvider;
import demetra.tsprovider.*;
import demetra.tsprovider.stream.HasTsStream;
import demetra.tsprovider.stream.TsStreamAsProvider;
import demetra.tsprovider.util.Params;
import nbbrd.io.function.IORunnable;

public final class MockedDataSourceLoader implements DataSourceLoader<String> {

    private static final String NAME = "mocked";

    @lombok.experimental.Delegate
    private final HasDataSourceMutableList dataSourceMutableList = HasDataSourceMutableList.of(NAME);

    @lombok.experimental.Delegate
    private final HasDataHierarchy dataHierarchy = HasDataHierarchy.noOp(NAME);

    @lombok.experimental.Delegate
    private final HasDataDisplayName dataDisplayName = HasDataDisplayName.usingUri(NAME);

    @lombok.experimental.Delegate
    private final HasDataMoniker dataMoniker = HasDataMoniker.usingUri(NAME);

    @lombok.experimental.Delegate
    private final HasDataSourceBean<String> dataSourceBean = HasDataSourceBean.of(NAME, Params.onString("value", "key"), "");

    @lombok.experimental.Delegate
    private final TsProvider provider = TsStreamAsProvider.of(NAME, HasTsStream.noOp(NAME), dataMoniker, IORunnable.noOp().asUnchecked());
}
