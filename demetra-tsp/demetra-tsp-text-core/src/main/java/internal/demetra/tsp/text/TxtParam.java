package internal.demetra.tsp.text;

import demetra.timeseries.util.ObsGathering;
import demetra.tsp.text.TxtBean;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.legacy.LegacyHandler;
import demetra.tsprovider.util.ObsFormat;
import demetra.tsprovider.util.PropertyHandler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface TxtParam extends DataSource.Converter<TxtBean> {

    @NonNull String getVersion();

    DataSet.@NonNull Converter<Integer> getSeriesParam();

    class V1 implements TxtParam {

        @lombok.experimental.Delegate
        private final DataSource.Converter<TxtBean> converter =
                TxtBeanHandler
                        .builder()
                        .file(PropertyHandler.onFile("file", new File("")))
                        .format(LegacyHandler.onObsFormat("locale", "datePattern", "numberPattern", ObsFormat.builder().locale(Locale.ENGLISH).dateTimePattern("yyyy-MM-dd").build()))
                        .charset(PropertyHandler.onCharset("charset", StandardCharsets.UTF_8))
                        .delimiter(PropertyHandler.onEnum("delimiter", TxtBean.Delimiter.TAB))
                        .headers(PropertyHandler.onBoolean("headers", true))
                        .skipLines(PropertyHandler.onInteger("skipLines", 0))
                        .textQualifier(PropertyHandler.onEnum("textQualifier", TxtBean.TextQualifier.NONE))
                        .gathering(LegacyHandler.onObsGathering("frequency", "aggregationType", "cleanMissing", ObsGathering.DEFAULT))
                        .build()
                        .asDataSourceConverter();

        @lombok.Getter
        private final String version = "20111201";

        @lombok.Getter
        private final DataSet.Converter<Integer> seriesParam = PropertyHandler.onInteger("seriesIndex", -1).asDataSetConverter();
    }

    @lombok.Builder(toBuilder = true)
    final class TxtBeanHandler implements PropertyHandler<TxtBean> {

        @lombok.NonNull
        private final PropertyHandler<File> file;

        @lombok.NonNull
        private final PropertyHandler<ObsFormat> format;

        @lombok.NonNull
        private final PropertyHandler<Charset> charset;

        @lombok.NonNull
        private final PropertyHandler<TxtBean.Delimiter> delimiter;

        @lombok.NonNull
        private final PropertyHandler<Boolean> headers;

        @lombok.NonNull
        private final PropertyHandler<Integer> skipLines;

        @lombok.NonNull
        private final PropertyHandler<TxtBean.TextQualifier> textQualifier;

        @lombok.NonNull
        private final PropertyHandler<ObsGathering> gathering;

        @Override
        public @NonNull TxtBean get(@NonNull Function<? super String, ? extends CharSequence> properties) {
            TxtBean result = new TxtBean();
            result.setFile(file.get(properties));
            result.setFormat(format.get(properties));
            result.setCharset(charset.get(properties));
            result.setDelimiter(delimiter.get(properties));
            result.setTextQualifier(textQualifier.get(properties));
            result.setHeaders(headers.get(properties));
            result.setSkipLines(skipLines.get(properties));
            result.setGathering(gathering.get(properties));
            return result;
        }

        @Override
        public void set(@NonNull BiConsumer<? super String, ? super String> properties, @Nullable TxtBean value) {
            if (value != null) {
                file.set(properties, value.getFile());
                format.set(properties, value.getFormat());
                charset.set(properties, value.getCharset());
                delimiter.set(properties, value.getDelimiter());
                textQualifier.set(properties, value.getTextQualifier());
                headers.set(properties, value.isHeaders());
                skipLines.set(properties, value.getSkipLines());
                gathering.set(properties, value.getGathering());
            }
        }
    }
}
