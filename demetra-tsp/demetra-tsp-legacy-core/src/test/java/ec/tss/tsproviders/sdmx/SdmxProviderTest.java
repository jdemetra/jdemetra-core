/*
 * Copyright 2016 National Bank of Belgium
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
package ec.tss.tsproviders.sdmx;

import demetra.bridge.ToFileBean;
import demetra.bridge.ToFileLoader;
import demetra.tsprovider.tck.FileLoaderAssert;
import org.junit.jupiter.api.Test;

import java.net.URL;

/**
 * @author Philippe Charles
 */
public class SdmxProviderTest {

    @Test
    public void testCompliance() {
        FileLoaderAssert.assertCompliance(
                () -> ToFileLoader.toFileLoader(new SdmxProvider()),
                p -> ToFileBean.toFileBean(SdmxProviderTest.getSampleBean((SdmxProvider) ((ToFileLoader) p).getDelegate()))
        );
    }

    private static final URL SAMPLE = SdmxProviderTest.class.getResource("/sdmx-generic-sample.xml");

    private static SdmxBean getSampleBean(SdmxProvider p) {
        SdmxBean result = p.newBean();
        result.setFile(FileLoaderAssert.urlAsFile(SAMPLE));
        return result;
    }
}
