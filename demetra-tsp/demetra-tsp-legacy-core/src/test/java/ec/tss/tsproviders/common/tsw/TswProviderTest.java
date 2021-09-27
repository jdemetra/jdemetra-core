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
package ec.tss.tsproviders.common.tsw;

import demetra.bridge.ToFileBean;
import demetra.bridge.ToFileLoader;
import demetra.tsprovider.tck.FileLoaderAssert;
import org.junit.Test;

import java.net.URL;

/**
 * @author Philippe Charles
 */
public class TswProviderTest {

    @Test
    public void testCompliance() {
        FileLoaderAssert.assertCompliance(
                () -> ToFileLoader.toFileLoader(new TswProvider()),
                p -> ToFileBean.toFileBean(TswProviderTest.getSampleBean((TswProvider) ((ToFileLoader) p).getDelegate()))
        );
    }

    private static final URL SAMPLE = TswFactoryTest.class.getResource("MultiObsPerLine");

    private static TswBean getSampleBean(TswProvider p) {
        TswBean result = p.newBean();
        result.setFile(FileLoaderAssert.urlAsFile(SAMPLE).getParentFile());
        return result;
    }
}
