package demetra.tsp.text;

import demetra.timeseries.TsCollection;
import demetra.timeseries.TsProvider;
import demetra.tsprovider.*;
import demetra.tsprovider.stream.HasTsStream;
import demetra.tsprovider.stream.TsStreamAsProvider;
import demetra.tsprovider.util.FallbackDataMoniker;
import demetra.tsprovider.util.ResourceMap;
import internal.demetra.tsp.text.*;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;

@DirectImpl
@ServiceProvider(TsProvider.class)
public final class TxtProvider implements FileLoader<TxtBean> {

    private static final String NAME = "Txt";

    @lombok.experimental.Delegate
    private final HasDataSourceMutableList mutableListSupport;

    @lombok.experimental.Delegate
    private final HasDataMoniker monikerSupport;

    @lombok.experimental.Delegate
    private final HasDataSourceBean<TxtBean> beanSupport;

    @lombok.experimental.Delegate
    private final HasFilePaths filePathSupport;

    @lombok.experimental.Delegate
    private final HasDataDisplayName displayNameSupport;

    @lombok.experimental.Delegate(excludes = HasTsStream.class, types = HasDataHierarchy.class)
    private final TxtSupport txtSupport;

    @lombok.experimental.Delegate
    private final TsProvider tsSupport;

    @lombok.experimental.Delegate
    private final TxtFileFilter fileFilter;

    public TxtProvider() {
        TxtParam param = new TxtParam.V1();

        ResourceMap<TsCollection> resources = ResourceMap.newInstance();

        this.mutableListSupport = HasDataSourceMutableList.of(NAME, resources::remove);
        this.monikerSupport = FallbackDataMoniker.of(HasDataMoniker.usingUri(NAME), TxtLegacyMoniker.of(NAME, param));
        this.beanSupport = HasDataSourceBean.of(NAME, param, param.getVersion());
        this.filePathSupport = HasFilePaths.of(resources::clear);
        this.displayNameSupport = TxtDataDisplayName.of(NAME, param, resources);
        this.txtSupport = TxtSupport.of(NAME, new TxtResource(resources, filePathSupport, param));
        this.tsSupport = TsStreamAsProvider.of(NAME, txtSupport, monikerSupport, resources::clear);
        this.fileFilter = new TxtFileFilter();
    }

    @Override
    public String getDisplayName() {
        return "Txt files";
    }

    @lombok.AllArgsConstructor
    private static final class TxtResource implements TxtSupport.Resource {

        @lombok.NonNull
        final ResourceMap<TsCollection> resources;

        @lombok.NonNull
        final HasFilePaths filePathSupport;

        @lombok.NonNull
        final TxtParam param;

        @Override
        public @NonNull TsCollection getData(@NonNull DataSource dataSource) throws IOException {
            return resources.computeIfAbsent(dataSource, this::load);
        }

        @Override
        public DataSet.@NonNull Converter<Integer> getSeriesParam(@NonNull DataSource dataSource) {
            return param.getSeriesParam(dataSource);
        }

        private TsCollection load(DataSource dataSource) throws IOException {
            return new TxtLoader(filePathSupport).load(param.get(dataSource));
        }
    }
}
