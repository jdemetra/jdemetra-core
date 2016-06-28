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
package ec.tss.tsproviders.common.txt;

import ec.tss.tsproviders.IFileLoaderAssert;
import java.net.URL;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class TxtProviderTest {

    @Test
    public void testCompliance() {
        IFileLoaderAssert.assertCompliance(TxtProvider::new, TxtProviderTest::getSampleBean);
    }

    private static final URL SAMPLE = TxtProviderTest.class.getResource("/Insee1.txt");

    private static TxtBean getSampleBean(TxtProvider p) {
        TxtBean result = p.newBean();
        result.setFile(IFileLoaderAssert.urlAsFile(SAMPLE));
        return result;
    }
}
