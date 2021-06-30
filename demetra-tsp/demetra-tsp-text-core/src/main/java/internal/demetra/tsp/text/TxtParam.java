package internal.demetra.tsp.text;

import demetra.timeseries.util.ObsGathering;
import demetra.tsp.text.TxtBean;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.util.IConfig;
import demetra.tsprovider.util.ObsFormat;
import demetra.tsprovider.util.Param;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public interface TxtParam extends Param<DataSource, TxtBean> {

    @NonNull
    String getVersion();

    @NonNull
    Param<DataSet, Integer> getSeriesParam(@NonNull DataSource dataSource);

    class V1 implements TxtParam {

        private final Param<DataSource, File> file = Param.onFile(new File(""), "file");
        private final Param<DataSource, ObsFormat> obsFormat = Param.onObsFormat(ObsFormat.of(Locale.ENGLISH, "yyyy-MM-dd", null), "locale", "datePattern", "numberPattern");
        private final Param<DataSource, Charset> charset = Param.onCharset(StandardCharsets.UTF_8, "charset");
        private final Param<DataSource, TxtBean.Delimiter> delimiter = Param.onEnum(TxtBean.Delimiter.TAB, "delimiter");
        private final Param<DataSource, Boolean> headers = Param.onBoolean(true, "headers");
        private final Param<DataSource, Integer> skipLines = Param.onInteger(0, "skipLines");
        private final Param<DataSource, TxtBean.TextQualifier> textQualifier = Param.onEnum(TxtBean.TextQualifier.NONE, "textQualifier");
        private final Param<DataSource, ObsGathering> obsGathering = Param.onObsGathering(ObsGathering.DEFAULT, "frequency", "aggregationType", "cleanMissing");
        private final Param<DataSet, Integer> series = Param.onInteger(-1, "seriesIndex");

        @Override
        public @NonNull String getVersion() {
            return "20111201";
        }

        @Override
        public @NonNull TxtBean defaultValue() {
            TxtBean result = new TxtBean();
            result.setFile(file.defaultValue());
            result.setObsFormat(obsFormat.defaultValue());
            result.setCharset(charset.defaultValue());
            result.setDelimiter(delimiter.defaultValue());
            result.setTextQualifier(textQualifier.defaultValue());
            result.setHeaders(headers.defaultValue());
            result.setSkipLines(skipLines.defaultValue());
            result.setObsGathering(obsGathering.defaultValue());
            return result;
        }

        @Override
        public @NonNull TxtBean get(@NonNull DataSource config) {
            TxtBean result = new TxtBean();
            result.setFile(file.get(config));
            result.setObsFormat(obsFormat.get(config));
            result.setCharset(charset.get(config));
            result.setDelimiter(delimiter.get(config));
            result.setTextQualifier(textQualifier.get(config));
            result.setHeaders(headers.get(config));
            result.setSkipLines(skipLines.get(config));
            result.setObsGathering(obsGathering.get(config));
            return result;
        }

        @Override
        public void set(@lombok.NonNull IConfig.Builder<?, DataSource> builder, @Nullable TxtBean value) {
            file.set(builder, value.getFile());
            obsFormat.set(builder, value.getObsFormat());
            charset.set(builder, value.getCharset());
            delimiter.set(builder, value.getDelimiter());
            textQualifier.set(builder, value.getTextQualifier());
            headers.set(builder, value.isHeaders());
            skipLines.set(builder, value.getSkipLines());
            obsGathering.set(builder, value.getObsGathering());
        }

        @Override
        public @NonNull Param<DataSet, Integer> getSeriesParam(@NonNull DataSource dataSource) {
            return series;
        }
    }
}
