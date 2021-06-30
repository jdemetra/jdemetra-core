package internal.demetra.tsp.text;

import demetra.tsp.text.XmlBean;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.util.IConfig;
import demetra.tsprovider.util.Param;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public interface XmlParam extends Param<DataSource, XmlBean> {

    @NonNull
    String getVersion();

    @NonNull
    Param<DataSet, Integer> getCollectionParam(@NonNull DataSource dataSource);

    @NonNull
    Param<DataSet, Integer> getSeriesParam(@NonNull DataSource dataSource);

    final class V1 implements XmlParam {

        final Param<DataSource, File> file = Param.onFile(new File(""), "file");
        final Param<DataSource, Charset> charset = Param.onCharset(StandardCharsets.UTF_8, "charset");
        final Param<DataSet, Integer> collectionIndex = Param.onInteger(-1, "collectionIndex");
        final Param<DataSet, Integer> seriesIndex = Param.onInteger(-1, "seriesIndex");

        @Override
        public @NonNull String getVersion() {
            return "20111201";
        }

        @Override
        public @NonNull Param<DataSet, Integer> getCollectionParam(@NonNull DataSource dataSource) {
            return collectionIndex;
        }

        @Override
        public @NonNull Param<DataSet, Integer> getSeriesParam(@NonNull DataSource dataSource) {
            return seriesIndex;
        }

        @Override
        public @NonNull XmlBean defaultValue() {
            XmlBean result = new XmlBean();
            result.setFile(file.defaultValue());
            result.setCharset(charset.defaultValue());
            return result;
        }

        @Override
        public @NonNull XmlBean get(@NonNull DataSource config) {
            XmlBean result = new XmlBean();
            result.setFile(file.get(config));
            result.setCharset(charset.get(config));
            return result;
        }

        @Override
        public void set(@lombok.NonNull IConfig.Builder<?, DataSource> builder, @Nullable XmlBean value) {
            file.set(builder, value.getFile());
            charset.set(builder, value.getCharset());
        }
    }
}
