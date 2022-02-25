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
package _demo;

import demetra.demo.ProviderDemo;
import demetra.tsp.extra.sdmx.file.SdmxFileBean;
import demetra.tsp.extra.sdmx.file.SdmxFileProvider;
import demetra.tsprovider.DataSource;
import sdmxdl.file.SdmxFileManager;
import sdmxdl.util.ext.MapCache;
import tests.sdmxdl.xml.SdmxXmlSources;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author Philippe Charles
 */
final class SdmxFileDemo3 {

    public static void main(String[] args) throws IllegalArgumentException, IOException {
        File file = Files.createTempFile("data_", ".xml").toFile();
        SdmxXmlSources.ECB_DATA.copyTo(file);

        File struct = Files.createTempFile("struct_", ".xml").toFile();
        SdmxXmlSources.ECB_DATA_STRUCTURE.copyTo(struct);

        // 1. create the provider
        try (SdmxFileProvider provider = new SdmxFileProvider()) {
            provider.setSdmxManager(getCustomManager());

            // 2. create and configure a bean
            SdmxFileBean bean = provider.newBean();
            bean.setFile(file);
            // optional params
            bean.setStructureFile(struct);
//            bean.setDimensions(java.util.Arrays.asList("FREQ", "AME_REF_AREA", "AME_TRANSFORMATION", "AME_AGG_METHOD", "AME_UNIT", "AME_REFERENCE", "AME_ITEM"));
//            bean.setLabelAttribute("EXT_TITLE");

            // 3. create and open a DataSource from the bean
            DataSource dataSource = provider.encodeBean(bean);
            provider.open(dataSource);

            // 4. run demos
//            ProviderDemo.printTree(provider, dataSource);
            ProviderDemo.printFirstSeries(provider, dataSource);
//            ProviderDemo.printSeriesCount(provider, dataSource);

            // 5. close resources
            provider.close(dataSource);
        }
    }

    private static SdmxFileManager getCustomManager() {
        return SdmxFileManager
                .ofServiceLoader()
                .toBuilder()
                .cache(MapCache.of())
                .eventListener((source, event) -> System.out.println("FILE: " + event))
                .build();
    }
}
