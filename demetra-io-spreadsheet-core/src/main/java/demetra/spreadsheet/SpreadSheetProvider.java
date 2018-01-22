/*
 * Copyright 2017 National Bank of Belgium
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

import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.FileLoader;
import demetra.tsprovider.HasDataDisplayName;
import demetra.tsprovider.HasDataMoniker;
import demetra.tsprovider.HasDataSourceBean;
import demetra.tsprovider.HasDataSourceMutableList;
import demetra.tsprovider.HasFilePaths;
import demetra.tsprovider.TsProvider;
import demetra.tsprovider.cursor.HasTsCursor;
import demetra.tsprovider.cursor.TsCursorAsProvider;
import demetra.tsprovider.grid.GridImport;
import demetra.tsprovider.grid.GridReader;
import demetra.tsprovider.util.CacheProvider;
import demetra.tsprovider.util.DataSourcePreconditions;
import demetra.tsprovider.util.FallbackDataMoniker;
import demetra.tsprovider.util.IParam;
import ec.util.spreadsheet.Book;
import internal.spreadsheet.BookSupplier;
import internal.spreadsheet.SpreadSheetDataDisplayName;
import internal.spreadsheet.SpreadSheetParam;
import internal.spreadsheet.SpreadSheetSupport;
import internal.spreadsheet.grid.BookData;
import internal.spreadsheet.grid.SheetGridInfo;
import internal.spreadsheet.legacy.LegacySpreadSheetMoniker;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentMap;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = TsProvider.class)
public final class SpreadSheetProvider implements FileLoader<SpreadSheetBean> {

    private static final String NAME = "XCLPRVDR";

    private final BookSupplier bookSupplier;

    @lombok.experimental.Delegate
    private final HasDataSourceMutableList mutableListSupport;

    @lombok.experimental.Delegate
    private final HasDataMoniker monikerSupport;

    @lombok.experimental.Delegate
    private final HasDataSourceBean<SpreadSheetBean> beanSupport;

    @lombok.experimental.Delegate
    private final HasFilePaths filePathSupport;

    @lombok.experimental.Delegate
    private final HasDataDisplayName displayNameSupport;

    @lombok.experimental.Delegate(excludes = HasTsCursor.class)
    private final SpreadSheetSupport spreadSheetSupport;

    @lombok.experimental.Delegate
    private final TsProvider tsSupport;

    public SpreadSheetProvider() {
        this.bookSupplier = BookSupplier.usingServiceLoader();

        ConcurrentMap<DataSource, BookData> cache = CacheProvider.getDefault().softValuesCacheAsMap();
        SpreadSheetParam param = new SpreadSheetParam.V1();

        this.mutableListSupport = HasDataSourceMutableList.of(NAME, cache::remove);
        this.monikerSupport = FallbackDataMoniker.of(HasDataMoniker.usingUri(NAME), LegacySpreadSheetMoniker.of(NAME, param));
        this.beanSupport = HasDataSourceBean.of(NAME, param, param.getVersion());
        this.filePathSupport = HasFilePaths.of(cache::clear);
        this.displayNameSupport = SpreadSheetDataDisplayName.of(NAME, param);
        this.spreadSheetSupport = SpreadSheetSupport.of(NAME, new SpreadSheetResource(cache, filePathSupport, param, bookSupplier));
        this.tsSupport = TsCursorAsProvider.of(NAME, spreadSheetSupport, monikerSupport, cache::clear);
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
        return bookSupplier.hasFactory(pathname);
    }

    @lombok.AllArgsConstructor
    private static final class SpreadSheetResource implements SpreadSheetSupport.Resource {

        private final ConcurrentMap<DataSource, BookData> cache;
        private final HasFilePaths filePathSupport;
        private final SpreadSheetParam param;
        private final BookSupplier bookSupplier;

        @Override
        public BookData getAccessor(DataSource dataSource) throws IOException {
            DataSourcePreconditions.checkProvider(NAME, dataSource);
            BookData result = cache.get(dataSource);
            if (result == null) {
                result = loadAccessor(dataSource);
                cache.put(dataSource, result);
            }
            return result;
        }

        @Override
        public IParam<DataSet, String> getSheetParam(DataSource dataSource) {
            return param.getSheetParam(dataSource);
        }

        @Override
        public IParam<DataSet, String> getSeriesParam(DataSource dataSource) {
            return param.getSeriesParam(dataSource);
        }

        private BookData loadAccessor(DataSource key) throws IOException {
            SpreadSheetBean bean = param.get(key);
            File file = filePathSupport.resolveFilePath(bean.getFile());
            Book.Factory factory = bookSupplier.getFactory(file);
            if (factory == null) {
                throw new IOException("File type not supported");
            }
            try (Book book = factory.load(file)) {
                GridImport options = GridImport.of(bean.getObsFormat(), bean.getObsGathering());
                return BookData.of(book, GridReader.of(options, SheetGridInfo.of(factory)));
            }
        }
    }
}
