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
import java.util.List;

@DirectImpl
@ServiceProvider(TsProvider.class)
public final class XmlProvider implements FileLoader<XmlBean> {

    private static final String NAME = "Xml";

    @lombok.experimental.Delegate
    private final HasDataSourceMutableList mutableListSupport;

    @lombok.experimental.Delegate
    private final HasDataMoniker monikerSupport;

    @lombok.experimental.Delegate
    private final HasDataSourceBean<XmlBean> beanSupport;

    @lombok.experimental.Delegate
    private final HasFilePaths filePathSupport;

    @lombok.experimental.Delegate
    private final HasDataDisplayName displayNameSupport;

    @lombok.experimental.Delegate(excludes = HasTsStream.class, types = HasDataHierarchy.class)
    private final XmlSupport xmlSupport;

    @lombok.experimental.Delegate
    private final TsProvider tsSupport;

    @lombok.experimental.Delegate
    private final XmlFileFilter fileFilter;

    public XmlProvider() {
        XmlParam param = new XmlParam.V1();

        ResourceMap<List<TsCollection>> resources = ResourceMap.newInstance();

        this.mutableListSupport = HasDataSourceMutableList.of(NAME, resources::remove);
        this.monikerSupport = FallbackDataMoniker.of(HasDataMoniker.usingUri(NAME), XmlLegacyMoniker.of(NAME, param));
        this.beanSupport = HasDataSourceBean.of(NAME, param, param.getVersion());
        this.filePathSupport = HasFilePaths.of(resources::clear);
        this.displayNameSupport = XmlDataDisplayName.of(NAME, param, resources);
        this.xmlSupport = XmlSupport.of(NAME, new XmlResource(resources, filePathSupport, param));
        this.tsSupport = TsStreamAsProvider.of(NAME, xmlSupport, monikerSupport, resources::clear);
        this.fileFilter = new XmlFileFilter();
    }

    @Override
    public String getDisplayName() {
        return "Xml files";
    }

    @lombok.AllArgsConstructor
    private static final class XmlResource implements XmlSupport.Resource {

        @lombok.NonNull
        final ResourceMap<List<TsCollection>> resources;

        @lombok.NonNull
        final HasFilePaths filePathSupport;

        @lombok.NonNull
        final XmlParam param;

        @Override
        public @NonNull List<TsCollection> getData(@NonNull DataSource dataSource) throws IOException {
            return resources.computeIfAbsent(dataSource, this::load);
        }

        @Override
        public DataSet.@NonNull Converter<Integer> getCollectionParam(@NonNull DataSource dataSource) {
            return param.getCollectionParam(dataSource);
        }

        @Override
        public DataSet.@NonNull Converter<Integer> getSeriesParam(@NonNull DataSource dataSource) {
            return param.getSeriesParam(dataSource);
        }

        private List<TsCollection> load(DataSource dataSource) throws IOException {
            return new XmlLoader(filePathSupport).load(param.get(dataSource));
        }
    }
}
