/*
 * Copyright 2017 National Bank of Belgium
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
package ec.tss.html;

import static ec.tss.html.HtmlClass.NO_CLASS;
import static ec.tss.html.HtmlTag.DIV;
import java.io.IOException;
import java.io.StringWriter;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class HtmlStreamTest {

    @Test
    public void testOpenTag() throws IOException {
        assertThat(of().open(DIV).writer.toString()).isEqualTo("<div>");
        assertThat(of().open(DIV, NO_CLASS).writer.toString()).isEqualTo("<div>");
        assertThat(of().open(DIV, HELLO_CLASS).writer.toString()).isEqualTo("<div class=\"hello\">");
    }

    @Test
    public void testCloseTag() throws IOException {
        assertThat(of().close(DIV).writer.toString()).isEqualTo("</div>");
    }

    @Test
    public void testWriteTag() throws IOException {
        assertThat(of().write(DIV).writer.toString()).isEqualTo("<div/>");
        assertThat(of().write(DIV, "content").writer.toString()).isEqualTo("<div>content</div>");
        assertThat(of().write(DIV, "content", NO_CLASS).writer.toString()).isEqualTo("<div>content</div>");
        assertThat(of().write(DIV, "content", HELLO_CLASS).writer.toString()).isEqualTo("<div class=\"hello\">content</div>");
    }

    private static HtmlStream of() {
        return new HtmlStream(new StringWriter());
    }

    private static final HtmlClass HELLO_CLASS = HtmlClass.of("hello");
}
