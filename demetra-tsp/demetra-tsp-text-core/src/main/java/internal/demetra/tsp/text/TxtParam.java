package internal.demetra.tsp.text;

import demetra.timeseries.util.ObsGathering;
import demetra.tsp.text.TxtBean;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.util.ObsFormat;
import demetra.tsprovider.util.TsProviders;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public interface TxtParam extends DataSource.Converter<TxtBean> {

    @NonNull String getVersion();

    DataSet.@NonNull Converter<Integer> getSeriesParam();

    class V1 implements TxtParam {

        private final Property<File> file = Property.of("file", new File(""), Parser.onFile(), Formatter.onFile());
        private final DataSource.Converter<ObsFormat> obsFormat = TsProviders.onObsFormat(ObsFormat.of(Locale.ENGLISH, "yyyy-MM-dd", null), "locale", "datePattern", "numberPattern");
        private final Property<Charset> charset = Property.of("charset", StandardCharsets.UTF_8, Parser.onCharset(), Formatter.onCharset());
        private final Property<TxtBean.Delimiter> delimiter = Property.of("delimiter", TxtBean.Delimiter.TAB, Parser.onEnum(TxtBean.Delimiter.class), Formatter.onEnum());
        private final Property<Boolean> headers = Property.of("headers", true, Parser.onBoolean(), Formatter.onBoolean());
        private final Property<Integer> skipLines = Property.of("skipLines", 0, Parser.onInteger(), Formatter.onInteger());
        private final Property<TxtBean.TextQualifier> textQualifier = Property.of("textQualifier", TxtBean.TextQualifier.NONE, Parser.onEnum(TxtBean.TextQualifier.class), Formatter.onEnum());
        private final DataSource.Converter<ObsGathering> obsGathering = TsProviders.onObsGathering(ObsGathering.DEFAULT, "frequency", "aggregationType", "cleanMissing");
        private final Property<Integer> series = Property.of("seriesIndex", -1, Parser.onInteger(), Formatter.onInteger());

        @Override
        public @NonNull String getVersion() {
            return "20111201";
        }

        @Override
        public @NonNull TxtBean getDefaultValue() {
            TxtBean result = new TxtBean();
            result.setFile(file.getDefaultValue());
            result.setObsFormat(obsFormat.getDefaultValue());
            result.setCharset(charset.getDefaultValue());
            result.setDelimiter(delimiter.getDefaultValue());
            result.setTextQualifier(textQualifier.getDefaultValue());
            result.setHeaders(headers.getDefaultValue());
            result.setSkipLines(skipLines.getDefaultValue());
            result.setObsGathering(obsGathering.getDefaultValue());
            return result;
        }

        @Override
        public @NonNull TxtBean get(@NonNull DataSource config) {
            TxtBean result = new TxtBean();
            result.setFile(file.get(config::getParameter));
            result.setObsFormat(obsFormat.get(config));
            result.setCharset(charset.get(config::getParameter));
            result.setDelimiter(delimiter.get(config::getParameter));
            result.setTextQualifier(textQualifier.get(config::getParameter));
            result.setHeaders(headers.get(config::getParameter));
            result.setSkipLines(skipLines.get(config::getParameter));
            result.setObsGathering(obsGathering.get(config));
            return result;
        }

        @Override
        public void set(DataSource.@NonNull Builder builder, @Nullable TxtBean value) {
            file.set(builder::parameter, value.getFile());
            obsFormat.set(builder, value.getObsFormat());
            charset.set(builder::parameter, value.getCharset());
            delimiter.set(builder::parameter, value.getDelimiter());
            textQualifier.set(builder::parameter, value.getTextQualifier());
            headers.set(builder::parameter, value.isHeaders());
            skipLines.set(builder::parameter, value.getSkipLines());
            obsGathering.set(builder, value.getObsGathering());
        }

        @Override
        public DataSet.@NonNull Converter<Integer> getSeriesParam() {
            return TsProviders.dataSetConverterOf(series);
        }
    }
}
