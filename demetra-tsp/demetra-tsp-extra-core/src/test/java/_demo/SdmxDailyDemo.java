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
import demetra.timeseries.Ts;
import demetra.timeseries.TsInformationType;
import demetra.tsp.extra.sdmx.web.SdmxWebBean;
import demetra.tsp.extra.sdmx.web.SdmxWebProvider;
import demetra.tsprovider.cube.BulkCube;
import sdmxdl.Key;
import sdmxdl.SdmxManager;
import sdmxdl.util.ext.MapCache;
import sdmxdl.web.SdmxWebListener;
import sdmxdl.web.SdmxWebManager;

import java.io.IOException;

/**
 * @author Philippe Charles
 */
final class SdmxDailyDemo {

    public static void main(String[] args) throws IllegalArgumentException, IOException {
        // 1. create the provider
        try (SdmxWebProvider provider = new SdmxWebProvider()) {
            provider.setSdmxManager(getCustomManager());

            // 2. create and configure a bean
            SdmxWebBean bean = provider.newBean();
            bean.setSource("ECB");
            bean.setFlow("EXR");
            bean.setLabelAttribute("TITLE");
            bean.setCacheConfig(BulkCube.NONE);

            // 3. run demos
            Ts ts = provider.getTs(bean, Key.parse("D.CHF.EUR.SP00.A"), TsInformationType.All);
            ProviderDemo.printSeries(provider, ts);
        }
    }

    private static SdmxManager getCustomManager() {
        return SdmxWebManager
                .ofServiceLoader()
                .toBuilder()
                .cache(MapCache.of())
                .eventListener(SdmxWebListener.of((source, event) -> System.out.println("WEB: " + event)))
                .build();
    }
}
