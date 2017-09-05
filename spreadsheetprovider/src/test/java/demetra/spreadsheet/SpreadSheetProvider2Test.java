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
package demetra.spreadsheet;

import static ec.tss.tsproviders.Assertions.assertThat;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.IFileLoaderAssert;
import ec.tss.tsproviders.spreadsheet.SpreadSheetBean;
import ec.tss.tsproviders.spreadsheet.SpreadSheetProvider;
import ec.tss.tsproviders.spreadsheet.SpreadSheetProviderTest;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.io.IOException;
import java.net.URL;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class SpreadSheetProvider2Test {

    private static final URL SAMPLE = SpreadSheetProviderTest.class.getResource("/Top5Browsers.xlsx");

    @Test
    public void testEquivalence() throws IOException {
        assertThat(new SpreadSheetProvider2())
                .isEquivalentTo(new SpreadSheetProvider(), this::getSampleDataSource);
    }

    @Test
    public void testTspCompliance() {
        IFileLoaderAssert.assertCompliance(SpreadSheetProvider2::new, this::getSampleBean);
    }

    private SpreadSheetBean2 getSampleBean(SpreadSheetProvider2 o) {
        SpreadSheetBean2 bean = o.newBean();
        bean.setFile(IFileLoaderAssert.urlAsFile(SAMPLE));
        return bean;
    }

    private DataSource getSampleDataSource(SpreadSheetProvider o) {
        SpreadSheetBean bean = o.newBean();
        bean.setFile(IFileLoaderAssert.urlAsFile(SAMPLE));
        bean.setFrequency(TsFrequency.Quarterly);
        bean.setAggregationType(TsAggregationType.Average);
        bean.setCleanMissing(false);
        return o.encodeBean(bean);
    }
}
