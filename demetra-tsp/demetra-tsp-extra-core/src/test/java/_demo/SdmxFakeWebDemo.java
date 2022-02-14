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
import demetra.tsp.extra.sdmx.web.SdmxWebBean;
import demetra.tsp.extra.sdmx.web.SdmxWebProvider;
import demetra.tsprovider.DataSource;
import sdmxdl.SdmxManager;
import sdmxdl.repo.SdmxRepositoryManager;
import sdmxdl.samples.RepoSamples;

import java.io.IOException;

/**
 * @author Philippe Charles
 */
final class SdmxFakeWebDemo {

    public static void main(String[] args) throws IllegalArgumentException, IOException {
        // 1. create the provider
        try (SdmxWebProvider provider = new SdmxWebProvider()) {
            provider.setSdmxManager(getCustomManager());

            // 2. create and configure a bean
            SdmxWebBean bean = provider.newBean();
            bean.setSource(RepoSamples.REPO.getName());
            bean.setFlow(RepoSamples.FLOW_REF.getId());
            // optional params
//            bean.setDimensions(java.util.Arrays.asList("SECTOR", "REGION", "FREQ"));
//            bean.setLabelAttribute("TITLE");

            // 3. create and open a DataSource from the bean
            DataSource dataSource = provider.encodeBean(bean);
            provider.open(dataSource);

            // 4. run demos
            ProviderDemo.printTree(provider, dataSource);
            ProviderDemo.printFirstSeries(provider, dataSource);

            // 5. close resources
            provider.close(dataSource);
        }
    }

    private static SdmxManager getCustomManager() {
        return SdmxRepositoryManager
                .builder()
                .repository(RepoSamples.REPO)
                .build();
    }
}
