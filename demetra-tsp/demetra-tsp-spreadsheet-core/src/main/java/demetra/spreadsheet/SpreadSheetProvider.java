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

import demetra.timeseries.TsProvider;
import demetra.tsprovider.*;
import demetra.tsprovider.grid.GridReader;
import demetra.tsprovider.stream.HasTsStream;
import demetra.tsprovider.stream.TsStreamAsProvider;
import demetra.tsprovider.util.FallbackDataMoniker;
import demetra.tsprovider.util.JCacheFactory;
import demetra.tsprovider.util.ResourceMap;
import ec.util.spreadsheet.Book;
import internal.spreadsheet.*;
import internal.spreadsheet.grid.SheetGrid;
import internal.spreadsheet.legacy.LegacySpreadSheetMoniker;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

/**
 *
 * @author Philippe Charles
 */
@DirectImpl
@ServiceProvider(TsProvider.class)
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

    @lombok.experimental.Delegate(excludes = HasTsStream.class)
    private final SpreadSheetSupport spreadSheetSupport;

    @lombok.experimental.Delegate
    private final TsProvider tsSupport;

    public SpreadSheetProvider() {
        this.bookSupplier = BookSupplier.usingServiceLoader();

        ResourceMap<SpreadSheetAccessor> accessors = ResourceMap.newInstance();
        SpreadSheetParam param = new SpreadSheetParam.V1();

        this.mutableListSupport = HasDataSourceMutableList.of(NAME, accessors::remove);
        this.monikerSupport = FallbackDataMoniker.of(HasDataMoniker.usingUri(NAME), LegacySpreadSheetMoniker.of(NAME, param));
        this.beanSupport = HasDataSourceBean.of(NAME, param, param.getVersion());
        this.filePathSupport = HasFilePaths.of(accessors::clear);
        this.displayNameSupport = SpreadSheetDataDisplayName.of(NAME, param);
        this.spreadSheetSupport = SpreadSheetSupport.of(NAME, new SpreadSheetResource(accessors, filePathSupport, param, bookSupplier));
        this.tsSupport = TsStreamAsProvider.of(NAME, spreadSheetSupport, monikerSupport, accessors::clear);
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

        private final ResourceMap<SpreadSheetAccessor> accessors;
        private final HasFilePaths filePathSupport;
        private final SpreadSheetParam param;
        private final BookSupplier bookSupplier;

        @Override
        public SpreadSheetAccessor getAccessor(DataSource dataSource) throws IOException {
            return accessors.computeIfAbsent(dataSource, this::load);
        }

        @Override
        public DataSet.Converter<String> getSheetParam(DataSource dataSource) {
            return param.getSheetParam(dataSource);
        }

        @Override
        public DataSet.Converter<String> getSeriesParam(DataSource dataSource) {
            return param.getSeriesParam(dataSource);
        }

        private SpreadSheetAccessor load(DataSource key) throws IOException {
            SpreadSheetBean bean = param.get(key);
            File file = filePathSupport.resolveFilePath(bean.getFile());
            Book.Factory factory = bookSupplier.getFactory(file);
            if (factory == null) {
                throw new IOException("File type not supported");
            }
            SheetGrid result = SheetGrid.of(file, factory, getReader(bean));
            return new CachedSpreadSheetAccessor(JCacheFactory.getTtlCacheByRef(key::toString, Duration.ofMinutes(5)), result);
        }

        private GridReader getReader(SpreadSheetBean bean) {
            return GridReader
                    .builder()
                    .format(bean.getObsFormat())
                    .gathering(bean.getObsGathering())
                    .build();
        }
    }
}
