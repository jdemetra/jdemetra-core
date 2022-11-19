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

package ec.tss.tsproviders.legacy;

import ec.tss.tsproviders.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class FileDataSourceIdTest {

    static File FILE, OTHER;
    
    @BeforeClass
    public static void beforeClass() throws IOException {
        FILE = Files.createTempFile("123", "456").toFile();
        OTHER = Files.createTempFile("aaa", "bbb").toFile();
    }
    
    @AfterClass
    public static void afterClass() {
        FILE.delete();
        OTHER.delete();
    }
   
    @Test
    public void testDemetraUri() {
        String input = DataSource.uriFormatter().formatAsString(DataSource.of("p", "123"));
        assertThat(FileDataSourceId.parse(input)).isNull();
    }
    
    @Test
    public void testFromFile() {
        FileDataSourceId sourceId = FileDataSourceId.from(FILE);
        Assert.assertNotNull(sourceId);
        Assert.assertEquals(FILE, new File(sourceId.getFile())); 
    }
    
    @Test
    public void testParseString() {
        FileDataSourceId sourceId = FileDataSourceId.parse(FILE.getPath());
        Assert.assertNotNull(sourceId);
        Assert.assertEquals(FILE, new File(sourceId.getFile()));
    }
    
    @Test
    public void testParseCharSequence() {
        FileDataSourceId sourceId = FileDataSourceId.parse((CharSequence)FILE.getPath());
        Assert.assertNotNull(sourceId);
        Assert.assertEquals(FILE, new File(sourceId.getFile()));
    }

    @Test 
    public void testEquals() throws IOException {
        FileDataSourceId sourceId = FileDataSourceId.from(FILE);

        Assert.assertEquals(sourceId, FileDataSourceId.from(FILE));
        Assert.assertNotSame(sourceId, FileDataSourceId.from(FILE));        
        Assert.assertFalse(sourceId.equals(FileDataSourceId.from(OTHER)));

        Assert.assertEquals(sourceId, FileDataSourceId.parse(FILE.getPath()));
        Assert.assertNotSame(sourceId, FileDataSourceId.parse(FILE.getPath()));        
        Assert.assertFalse(sourceId.equals(FileDataSourceId.parse(OTHER.getPath())));

        Assert.assertEquals(sourceId, FileDataSourceId.parse((CharSequence)sourceId));
        Assert.assertSame(sourceId, FileDataSourceId.parse((CharSequence)sourceId));        
    }
}
