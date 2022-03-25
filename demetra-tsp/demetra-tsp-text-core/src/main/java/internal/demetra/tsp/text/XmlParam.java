package internal.demetra.tsp.text;

import demetra.tsp.text.XmlBean;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.util.PropertyHandler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface XmlParam extends DataSource.Converter<XmlBean> {

    @NonNull
    String getVersion();

    DataSet.@NonNull Converter<Integer> getCollectionParam();

    DataSet.@NonNull Converter<Integer> getSeriesParam();

    final class V1 implements XmlParam {

        @lombok.experimental.Delegate
        private final DataSource.Converter<XmlBean> converter =
                XmlBeanHandler
                        .builder()
                        .file(PropertyHandler.onFile("file", new File("")))
                        .charset(PropertyHandler.onCharset("charset", StandardCharsets.UTF_8))
                        .build()
                        .asDataSourceConverter();

        @lombok.Getter
        private final String version = "20111201";

        @lombok.Getter
        private final DataSet.Converter<Integer> collectionParam = PropertyHandler.onInteger("collectionIndex", -1).asDataSetConverter();

        @lombok.Getter
        private final DataSet.Converter<Integer> seriesParam = PropertyHandler.onInteger("seriesIndex", -1).asDataSetConverter();
    }

    @lombok.Builder(toBuilder = true)
    final class XmlBeanHandler implements PropertyHandler<XmlBean> {

        @lombok.NonNull
        private final PropertyHandler<File> file;

        @lombok.NonNull
        private final PropertyHandler<Charset> charset;

        @Override
        public @NonNull XmlBean get(@NonNull Function<? super String, ? extends CharSequence> properties) {
            XmlBean result = new XmlBean();
            result.setFile(file.get(properties));
            result.setCharset(charset.get(properties));
            return result;
        }

        @Override
        public void set(@NonNull BiConsumer<? super String, ? super String> properties, @Nullable XmlBean value) {
            if (value != null) {
                file.set(properties, value.getFile());
                charset.set(properties, value.getCharset());
            }
        }
    }
}
