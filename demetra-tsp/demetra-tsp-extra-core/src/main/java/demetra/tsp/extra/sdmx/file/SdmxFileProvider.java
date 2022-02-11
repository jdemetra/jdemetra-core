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
package demetra.tsp.extra.sdmx.file;

import demetra.timeseries.TsProvider;
import demetra.tsp.extra.sdmx.HasSdmxProperties;
import demetra.tsprovider.*;
import demetra.tsprovider.cube.CubeAccessor;
import demetra.tsprovider.cube.CubeId;
import demetra.tsprovider.cube.CubeSupport;
import demetra.tsprovider.stream.HasTsStream;
import demetra.tsprovider.stream.TsStreamAsProvider;
import demetra.tsprovider.util.ResourceMap;
import internal.tsp.extra.sdmx.SdmxCubeAccessor;
import internal.tsp.extra.sdmx.SdmxCubeItems;
import internal.tsp.extra.sdmx.SdmxPropertiesSupport;
import nbbrd.io.function.IOSupplier;
import nbbrd.service.ServiceProvider;
import sdmxdl.DataflowRef;
import sdmxdl.SdmxConnection;
import sdmxdl.SdmxManager;
import sdmxdl.file.SdmxFileManager;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.xml.XmlFileSource;

import java.io.File;
import java.io.IOException;

/**
 * @author Philippe Charles
 * @since 2.2.0
 */
@ServiceProvider(TsProvider.class)
public final class SdmxFileProvider implements FileLoader<SdmxFileBean>, HasSdmxProperties {

    public static final String NAME = "sdmx-file";

    @lombok.experimental.Delegate
    private final HasSdmxProperties properties;

    @lombok.experimental.Delegate
    private final HasDataSourceMutableList mutableListSupport;

    @lombok.experimental.Delegate
    private final HasDataMoniker monikerSupport;

    @lombok.experimental.Delegate
    private final HasDataSourceBean<SdmxFileBean> beanSupport;

    @lombok.experimental.Delegate
    private final HasFilePaths filePathSupport;

    @lombok.experimental.Delegate(excludes = {HasTsStream.class, HasDataDisplayName.class})
    private final CubeSupport cubeSupport;

    @lombok.experimental.Delegate
    private final TsProvider tsSupport;

    public SdmxFileProvider() {
        ResourceMap<CubeAccessor> accessors = ResourceMap.newInstance();
        SdmxFileParam sdmxParam = new SdmxFileParam.V1();

        this.properties = SdmxPropertiesSupport.of(SdmxFileManager::ofServiceLoader, accessors::clear);
        this.mutableListSupport = HasDataSourceMutableList.of(NAME, accessors::remove);
        this.monikerSupport = HasDataMoniker.usingUri(NAME);
        this.beanSupport = HasDataSourceBean.of(NAME, sdmxParam, sdmxParam.getVersion());
        this.filePathSupport = HasFilePaths.of(accessors::clear);
        this.cubeSupport = CubeSupport.of(NAME, new SdmxFileCubeResource(accessors, properties, filePathSupport, sdmxParam));
        this.tsSupport = TsStreamAsProvider.of(NAME, cubeSupport, monikerSupport, accessors::clear);
    }

    @Override
    public String getDisplayName() {
        return "SDMX Files";
    }

    @Override
    public String getFileDescription() {
        return "SDMX file";
    }

    @Override
    public boolean accept(File pathname) {
        return pathname.getName().toLowerCase().endsWith(".xml");
    }

    @Override
    public String getDisplayName(DataSource dataSource) throws IllegalArgumentException {
        return getSourceLabel(decodeBean(dataSource));
    }

    @Override
    public String getDisplayName(DataSet dataSet) throws IllegalArgumentException {
        return cubeSupport.getDisplayName(dataSet);
    }

    @Override
    public String getDisplayName(IOException exception) throws IllegalArgumentException {
        return cubeSupport.getDisplayName(exception);
    }

    @Override
    public String getDisplayNodeName(DataSet dataSet) throws IllegalArgumentException {
        return cubeSupport.getDisplayNodeName(dataSet);
    }

    @lombok.AllArgsConstructor
    private static final class SdmxFileCubeResource implements CubeSupport.Resource {

        private final ResourceMap<CubeAccessor> accessors;
        private final HasSdmxProperties properties;
        private final HasFilePaths paths;
        private final SdmxFileParam param;

        @Override
        public CubeAccessor getAccessor(DataSource dataSource) throws IOException {
            return accessors.computeIfAbsent(dataSource, this::load);
        }

        @Override
        public DataSet.Converter<CubeId> getIdParam(CubeId root) {
            return param.getCubeIdParam(root);
        }

        private CubeAccessor load(DataSource dataSource) throws IOException {
            SdmxFileBean bean = param.get(dataSource);
            SdmxFileSource files = SdmxCubeItems.resolveFileSet(paths, bean);

            DataflowRef flow = files.asDataflowRef();

            return SdmxCubeAccessor.of(toConnection(properties, files), flow, bean.getDimensions(), bean.getLabelAttribute(), getSourceLabel(bean));
        }

        private static IOSupplier<SdmxConnection> toConnection(HasSdmxProperties properties, SdmxFileSource files) throws IOException {
            SdmxManager supplier = properties.getSdmxManager();

            if (supplier instanceof SdmxFileManager) {
                return () -> ((SdmxFileManager) supplier).getConnection(files);
            }

            String name = XmlFileSource.getFormatter().formatToString(files);
            return () -> supplier.getConnection(name);
        }
    }

    private static String getSourceLabel(SdmxFileBean bean) {
        return bean.getFile().getPath();
    }
}
