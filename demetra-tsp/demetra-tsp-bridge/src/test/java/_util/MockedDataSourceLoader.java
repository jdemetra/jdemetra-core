package _util;

import demetra.timeseries.TsProvider;
import demetra.tsprovider.*;
import demetra.tsprovider.stream.HasTsStream;
import demetra.tsprovider.stream.TsStreamAsProvider;
import nbbrd.io.function.IORunnable;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class MockedDataSourceLoader implements DataSourceLoader<String> {

    private static final String NAME = "mocked";

    private static final DataSource.Converter<String> MOCKED_PARAM = new DataSource.Converter<String>() {
        final Property<String> property = Property.of("key", "value", Parser.onString(), Formatter.onString());

        @Override
        public @NonNull String getDefaultValue() {
            return property.getDefaultValue();
        }

        @Override
        public @NonNull String get(@NonNull DataSource config) {
            return property.get(config::getParameter);
        }

        @Override
        public void set(@lombok.NonNull DataSource.Builder builder, @Nullable String value) {
            property.set(builder::parameter, value);
        }
    };

    @lombok.experimental.Delegate
    private final HasDataSourceMutableList dataSourceMutableList = HasDataSourceMutableList.of(NAME);

    @lombok.experimental.Delegate
    private final HasDataHierarchy dataHierarchy = HasDataHierarchy.noOp(NAME);

    @lombok.experimental.Delegate
    private final HasDataDisplayName dataDisplayName = HasDataDisplayName.usingUri(NAME);

    @lombok.experimental.Delegate
    private final HasDataMoniker dataMoniker = HasDataMoniker.usingUri(NAME);

    @lombok.experimental.Delegate
    private final HasDataSourceBean<String> dataSourceBean = HasDataSourceBean.of(NAME, MOCKED_PARAM, "");

    @lombok.experimental.Delegate
    private final TsProvider provider = TsStreamAsProvider.of(NAME, HasTsStream.noOp(NAME), dataMoniker, IORunnable.noOp().asUnchecked());
}
