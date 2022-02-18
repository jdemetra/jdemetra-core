package demetra.tsp.text;

import demetra.timeseries.TsCollection;
import demetra.timeseries.TsProvider;
import demetra.tsprovider.*;
import demetra.tsprovider.stream.HasTsStream;
import demetra.tsprovider.stream.TsStreamAsProvider;
import demetra.tsprovider.util.FallbackDataMoniker;
import demetra.tsprovider.util.ImmutableValuePool;
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

        ImmutableValuePool<TsCollection> pool = ImmutableValuePool.of();

        this.mutableListSupport = HasDataSourceMutableList.of(NAME, pool::remove);
        this.monikerSupport = FallbackDataMoniker.of(HasDataMoniker.usingUri(NAME), TxtLegacyMoniker.of(NAME, param));
        this.beanSupport = HasDataSourceBean.of(NAME, param, param.getVersion());
        this.filePathSupport = HasFilePaths.of(pool::clear);
        this.displayNameSupport = TxtDataDisplayName.of(NAME, param, pool::peek);
        this.txtSupport = TxtSupport.of(NAME, pool.asFactory(dataSource -> getData(dataSource, filePathSupport, param)), ignore -> param.getSeriesParam());
        this.tsSupport = TsStreamAsProvider.of(NAME, txtSupport, monikerSupport, pool::clear);
        this.fileFilter = new TxtFileFilter();
    }

    @Override
    public String getDisplayName() {
        return "Txt files";
    }

    private static @NonNull TsCollection getData(@NonNull DataSource dataSource, HasFilePaths paths, TxtParam param) throws IOException {
        return new TxtLoader(paths).load(param.get(dataSource));
    }
}
