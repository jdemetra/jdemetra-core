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
import demetra.tsp.text.TxtBean;
import demetra.tsp.text.TxtProvider;
import demetra.tsprovider.tck.FileLoaderAssert;
import ec.tss.tsproviders.IFileBean;

import java.io.File;

/**
 * @author Philippe Charles
 */
public enum TxtSamples implements ProviderResources.FileLoader2<ec.tss.tsproviders.common.txt.TxtProvider>, ProviderResources.FileLoader3<TxtProvider> {

    INSEE1;

    private final File file = FileLoaderAssert.urlAsFile(TxtSamples.class.getResource("/Insee1.txt"));

    @Override
    public IFileBean getBean2(ec.tss.tsproviders.common.txt.TxtProvider provider) {
        IFileBean bean = provider.newBean();
        bean.setFile(file);
        return bean;
    }

    @Override
    public ec.tss.tsproviders.common.txt.TxtProvider getProvider2() {
        return new ec.tss.tsproviders.common.txt.TxtProvider();
    }

    @Override
    public TxtBean getBean3(TxtProvider provider) {
        TxtBean bean = provider.newBean();
        bean.setFile(file);
        return bean;
    }

    @Override
    public TxtProvider getProvider3() {
        return new TxtProvider();
    }
}
