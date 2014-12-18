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
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class SpreadSheetProviderTest {

    static final URL dataUrl = SpreadSheetProviderTest.class.getResource("/Top5Browsers.ods");
    static File file;

    @BeforeClass
    public static void beforeClass() throws URISyntaxException {
        file = new File(dataUrl.toURI());
    }
    SpreadSheetProvider provider;

    @Before
    public void before() {
        provider = new SpreadSheetProvider();
    }

    @After
    public void afterClass() {
        provider.dispose();
    }

    DataSource loadFile() {
        SpreadSheetBean bean = provider.newBean();
        bean.setFile(file);
        DataSource dataSource = provider.encodeBean(bean);
        Assert.assertTrue(provider.open(dataSource));
        return dataSource;
    }

    @Test
    public void testGetDataSources() {
        DataSource dataSource = loadFile();
        List<DataSource> dataSources = provider.getDataSources();
        Assert.assertEquals(1, dataSources.size());
        Assert.assertEquals(dataSource, dataSources.get(0));
    }

    @Test
    public void testGetDisplayNameDataSource() {
        DataSource dataSource = loadFile();
        Assert.assertEquals(file.getPath(), provider.getDisplayName(dataSource));
    }

    @Test
    public void testGetDisplayNameDataSet() throws Exception {
        DataSource dataSource = loadFile();
        DataSet o = provider.children(provider.children(dataSource).get(0)).get(2);
        Assert.assertEquals("Top 5 Browsers - Monthly - Chrome", provider.getDisplayName(o));
    }

    @Test
    public void testGetDisplayNodeName() throws Exception {
        DataSource dataSource = loadFile();
        DataSet o = provider.children(provider.children(dataSource).get(0)).get(2);
        Assert.assertEquals("Chrome", provider.getDisplayNodeName(o));
    }
}
