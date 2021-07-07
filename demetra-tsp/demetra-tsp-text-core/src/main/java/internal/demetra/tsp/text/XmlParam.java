package internal.demetra.tsp.text;

import demetra.tsp.text.XmlBean;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.util.TsProviders;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public interface XmlParam extends DataSource.Converter<XmlBean> {

    @NonNull
    String getVersion();

    DataSet.@NonNull Converter<Integer> getCollectionParam(@NonNull DataSource dataSource);

    DataSet.@NonNull Converter<Integer> getSeriesParam(@NonNull DataSource dataSource);

    final class V1 implements XmlParam {

        final Property<File> file = Property.of("file", new File(""), Parser.onFile(), Formatter.onFile());
        final Property<Charset> charset = Property.of("charset", StandardCharsets.UTF_8, Parser.onCharset(), Formatter.onCharset());
        final Property<Integer> collectionIndex = Property.of("collectionIndex", -1, Parser.onInteger(), Formatter.onInteger());
        final Property<Integer> seriesIndex = Property.of("seriesIndex", -1, Parser.onInteger(), Formatter.onInteger());

        @Override
        public @NonNull String getVersion() {
            return "20111201";
        }

        @Override
        public DataSet.@NonNull Converter<Integer> getCollectionParam(@NonNull DataSource dataSource) {
            return TsProviders.dataSetConverterOf(collectionIndex);
        }

        @Override
        public DataSet.@NonNull Converter<Integer> getSeriesParam(@NonNull DataSource dataSource) {
            return TsProviders.dataSetConverterOf(seriesIndex);
        }

        @Override
        public @NonNull XmlBean getDefaultValue() {
            XmlBean result = new XmlBean();
            result.setFile(file.getDefaultValue());
            result.setCharset(charset.getDefaultValue());
            return result;
        }

        @Override
        public @NonNull XmlBean get(@NonNull DataSource config) {
            XmlBean result = new XmlBean();
            result.setFile(file.get(config::getParameter));
            result.setCharset(charset.get(config::getParameter));
            return result;
        }

        @Override
        public void set(DataSource.@NonNull Builder builder, @Nullable XmlBean value) {
            file.set(builder::parameter, value.getFile());
            charset.set(builder::parameter, value.getCharset());
        }
    }
}
