/*
 * Copyright 2018 National Bank of Belgium
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
package _test;

import demetra.demo.ProviderResources;
import demetra.spreadsheet.SpreadSheetBean;
import demetra.spreadsheet.SpreadSheetProvider;
import ec.tss.tsproviders.IFileBean;
import ec.tss.tsproviders.IFileLoaderAssert;
import java.io.File;

/**
 *
 * @author Philippe Charles
 */
public enum SpreadSheetSamples implements ProviderResources.FileLoader2<ec.tss.tsproviders.spreadsheet.SpreadSheetProvider>, ProviderResources.FileLoader3<SpreadSheetProvider> {

    TOP5;

    private final File file = IFileLoaderAssert.urlAsFile(SpreadSheetSamples.class.getResource("/Top5Browsers.xlsx"));

    @Override
    public IFileBean getBean2(ec.tss.tsproviders.spreadsheet.SpreadSheetProvider provider) {
        ec.tss.tsproviders.IFileBean bean = provider.newBean();
        bean.setFile(file);
        return bean;
    }

    @Override
    public ec.tss.tsproviders.spreadsheet.SpreadSheetProvider getProvider2() {
        return new ec.tss.tsproviders.spreadsheet.SpreadSheetProvider();
    }

    @Override
    public SpreadSheetBean getBean3(SpreadSheetProvider provider) {
        SpreadSheetBean bean = provider.newBean();
        bean.setFile(file);
        return bean;
    }

    @Override
    public SpreadSheetProvider getProvider3() {
        return new SpreadSheetProvider();
    }
}
