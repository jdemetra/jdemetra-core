/*
 * Copyright 2013 National Bank of Belgium
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

import static ec.util.spreadsheet.Assertions.*;
import ec.util.spreadsheet.helpers.ArrayBook;
import ec.util.spreadsheet.helpers.ArraySheet;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Optional;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class XMLStreamBookWriterTest {

    @Test
    public void test() throws XMLStreamException, IOException {
        XMLStreamBookWriter writer = new XMLStreamBookWriter(XMLOutputFactory.newInstance(), new SimpleDateFormat(), NumberFormat.getInstance(), StandardCharsets.UTF_8);
        JsoupBookReader reader = new JsoupBookReader(Optional.empty(), "");

        ArrayBook input = ArraySheet.builder().name("hello").table(0, 0, new Object[][]{{"A1", "B1", "C1"}, {"A2", "B2"}}).build().toBook();

        assertThat(reader.read(writer.write(input))).hasSameContentAs(input, true);
    }
}
