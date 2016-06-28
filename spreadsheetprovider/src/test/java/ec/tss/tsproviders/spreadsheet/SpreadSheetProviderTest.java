/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
package ec.tss.tsproviders.spreadsheet;

import ec.tss.tsproviders.DataSet;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.IFileLoaderAssert;
import java.net.URL;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class SpreadSheetProviderTest {

    static final URL dataUrl = SpreadSheetProviderTest.class.getResource("/Top5Browsers.ods");

    SpreadSheetProvider provider;

    @Before
    public void before() {
        provider = new SpreadSheetProvider();
    }

    @After
    public void after() {
        provider.dispose();
    }

    @Test
    public void testGetDataSources() {
        DataSource dataSource = loadFile(provider);
        List<DataSource> dataSources = provider.getDataSources();
        Assert.assertEquals(1, dataSources.size());
        Assert.assertEquals(dataSource, dataSources.get(0));
    }

    @Test
    public void testGetDisplayNameDataSource() {
        DataSource dataSource = loadFile(provider);
        Assert.assertEquals(IFileLoaderAssert.urlAsFile(dataUrl).getPath(), provider.getDisplayName(dataSource));
    }

    @Test
    public void testGetDisplayNameDataSet() throws Exception {
        DataSource dataSource = loadFile(provider);
        DataSet o = provider.children(provider.children(dataSource).get(0)).get(2);
        Assert.assertEquals("Top 5 Browsers - Monthly\nChrome", provider.getDisplayName(o));
    }

    @Test
    public void testGetDisplayNodeName() throws Exception {
        DataSource dataSource = loadFile(provider);
        DataSet o = provider.children(provider.children(dataSource).get(0)).get(2);
        Assert.assertEquals("Chrome", provider.getDisplayNodeName(o));
    }

    @Test
    public void testCompliance() {
        IFileLoaderAssert.assertCompliance(SpreadSheetProvider::new, o -> getSampleBean(o));
    }

    private static SpreadSheetBean getSampleBean(SpreadSheetProvider p) {
        SpreadSheetBean bean = p.newBean();
        bean.setFile(IFileLoaderAssert.urlAsFile(dataUrl));
        return bean;
    }

    private static DataSource loadFile(SpreadSheetProvider p) {
        DataSource dataSource = p.encodeBean(getSampleBean(p));
        Assert.assertTrue(p.open(dataSource));
        return dataSource;
    }
}
