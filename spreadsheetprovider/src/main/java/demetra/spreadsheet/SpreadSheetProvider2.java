/*
 * Copyright 2015 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.spreadsheet;

import com.google.common.cache.Cache;
import ec.tss.ITsProvider;
import ec.tss.TsAsyncMode;
import ec.tss.tsproviders.DataSet;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.HasDataDisplayName;
import ec.tss.tsproviders.HasDataMoniker;
import ec.tss.tsproviders.HasDataSourceBean;
import ec.tss.tsproviders.HasDataSourceMutableList;
import ec.tss.tsproviders.HasFilePaths;
import ec.tss.tsproviders.IFileLoader;
import ec.tss.tsproviders.cursor.HasTsCursor;
import ec.tss.tsproviders.cursor.TsCursorAsFiller;
import internal.spreadsheet.SpreadSheetFactory;
import internal.spreadsheet.SpreadSheetSource;
import internal.spreadsheet.TsImportOptions;
import ec.tss.tsproviders.utils.DataSourcePreconditions;
import ec.tss.tsproviders.utils.IParam;
import ec.tss.tsproviders.utils.TsFillerAsProvider;
import ec.tstoolkit.utilities.GuavaCaches;
import ec.util.spreadsheet.Book;
import java.io.File;
import java.io.IOException;
import java.util.ServiceLoader;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = ITsProvider.class, supersedes = "ec.tss.tsproviders.spreadsheet.SpreadSheetProvider")
public final class SpreadSheetProvider2 implements IFileLoader {

    private static final String NAME = "XCLPRVDR";

    @lombok.experimental.Delegate
    private final HasDataSourceMutableList mutableListSupport;

    @lombok.experimental.Delegate
    private final HasDataMoniker monikerSupport;

    @lombok.experimental.Delegate
    private final HasDataSourceBean<SpreadSheetBean2> beanSupport;

    @lombok.experimental.Delegate
    private final HasFilePaths filePathSupport;

    @lombok.experimental.Delegate
    private final HasDataDisplayName displayNameSupport;

    @lombok.experimental.Delegate(excludes = HasTsCursor.class)
    private final SpreadSheetSupport spreadSheetSupport;

    @lombok.experimental.Delegate
    private final ITsProvider tsSupport;

    public SpreadSheetProvider2() {
        Logger logger = LoggerFactory.getLogger(NAME);
        Cache<DataSource, SpreadSheetSource> cache = GuavaCaches.softValuesCache();
        SpreadSheetParam beanParam = new SpreadSheetParam.V1();

        this.mutableListSupport = HasDataSourceMutableList.of(NAME, logger, cache::invalidate);
        this.monikerSupport = HasDataMoniker.usingUri(NAME);
        this.beanSupport = HasDataSourceBean.of(NAME, beanParam, beanParam.getVersion());
        this.filePathSupport = HasFilePaths.of(cache::invalidateAll);
        this.displayNameSupport = new SpreadSheetDataDisplayName(NAME, beanParam);
        this.spreadSheetSupport = SpreadSheetSupport.of(NAME, new SpreadSheetResource(cache, filePathSupport, beanParam));
        this.tsSupport = TsFillerAsProvider.of(NAME, TsAsyncMode.Once, TsCursorAsFiller.of(logger, spreadSheetSupport, monikerSupport, displayNameSupport), cache::invalidateAll);
    }

    @Override
    public String getDisplayName() {
        return "Spreadsheets";
    }

    @Override
    public String getFileDescription() {
        return "Spreadsheet file";
    }

    @Override
    public boolean accept(File pathname) {
        return getFactoryByFile(pathname) != null;
    }

    @Nullable
    private static Book.Factory getFactoryByFile(@Nonnull File file) {
        for (Book.Factory o : ServiceLoader.load(Book.Factory.class)) {
            if (o.canLoad() && o.accept(file)) {
                return o;
            }
        }
        return null;
    }

    @lombok.AllArgsConstructor
    private static final class SpreadSheetResource implements SpreadSheetSupport.Resource {

        private final Cache<DataSource, SpreadSheetSource> cache;
        private final HasFilePaths filePathSupport;
        private final SpreadSheetParam param;

        @Override
        public SpreadSheetSource getAccessor(DataSource dataSource) throws IOException {
            DataSourcePreconditions.checkProvider(NAME, dataSource);
            return GuavaCaches.getOrThrowIOException(cache, dataSource, () -> load(dataSource));
        }

        @Override
        public IParam<DataSet, String> getSheetParam(DataSource dataSource) {
            return param.getSheetParam(dataSource);
        }

        @Override
        public IParam<DataSet, String> getSeriesParam(DataSource dataSource) {
            return param.getSeriesParam(dataSource);
        }

        private SpreadSheetSource load(DataSource key) throws IOException {
            SpreadSheetBean2 bean = param.get(key);
            File file = filePathSupport.resolveFilePath(bean.getFile());
            Book.Factory factory = getFactoryByFile(file);
            if (factory != null) {
                try (Book book = factory.load(file)) {
                    TsImportOptions options = TsImportOptions.of(bean.getObsFormat(), bean.getObsGathering());
                    return SpreadSheetFactory.getDefault().toSource(book, options);
                }
            }
            throw new RuntimeException("File type not supported");
        }
    }
}
