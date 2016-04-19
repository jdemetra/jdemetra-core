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
package ec.util.spreadsheet.html;

import static ec.util.spreadsheet.Assertions.assertThat;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Philippe Charles
 */
public class HtmlBookFactoryTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void testCompliance() throws IOException {
        File valid = createContent(temp.newFile("valid.htm"));
        File invalid = temp.newFile("invalid.htm");
        assertThat(new HtmlBookFactory()).isCompliant(valid, invalid);
    }

    private static File createContent(File file) throws IOException {
        Files.write(file.toPath(), Collections.singleton("<table><tr><td>A1</td><td rowspan=2>B1</td></tr> <tr><td>A2</td></tr> <tr><td>A3</td><td>B3</td></tr>"));
        return file;
    }
}
