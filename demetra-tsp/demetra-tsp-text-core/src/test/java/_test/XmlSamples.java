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
import demetra.tsp.text.XmlBean;
import demetra.tsp.text.XmlProvider;
import demetra.tsprovider.tck.FileLoaderAssert;
import ec.tss.tsproviders.IFileBean;

import java.io.File;

/**
 * @author Philippe Charles
 */
public enum XmlSamples implements ProviderResources.FileLoader2<ec.tss.tsproviders.common.xml.XmlProvider>, ProviderResources.FileLoader3<XmlProvider> {

    INSEE1;

    private final File file = FileLoaderAssert.urlAsFile(XmlSamples.class.getResource("/Insee.xml"));

    @Override
    public IFileBean getBean2(ec.tss.tsproviders.common.xml.XmlProvider provider) {
        IFileBean bean = provider.newBean();
        bean.setFile(file);
        return bean;
    }

    @Override
    public ec.tss.tsproviders.common.xml.XmlProvider getProvider2() {
        return new ec.tss.tsproviders.common.xml.XmlProvider();
    }

    @Override
    public XmlBean getBean3(XmlProvider provider) {
        XmlBean bean = provider.newBean();
        bean.setFile(file);
        return bean;
    }

    @Override
    public XmlProvider getProvider3() {
        return new XmlProvider();
    }
}
