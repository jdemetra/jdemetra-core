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

package ec.tss.html;

import java.io.IOException;
import java.util.ArrayList;

/**
 * 
 * @author Kristof Bayens, Jean Palate
 */
@Deprecated
public class CssStyle implements IHtmlElement {

    static class CSSItem {

        final CssProperty property;
        final String value;

        CSSItem(CssProperty property, String value) {
            this.property = property;
            this.value = value;
        }
    }
    private ArrayList<CSSItem> m_items = new ArrayList<>();

    /**
     * 
     */
    public CssStyle() {
    }

    /**
     * 
     * @param property
     * @param value
     */
    public void add(CssProperty property, String value) {
        m_items.add(new CSSItem(property, value));
    }

    /**
     * 
     * @param stream
     * @throws IOException
     */
    @Override
    public void write(HtmlStream stream) throws IOException {
        if (m_items.isEmpty()) {
            return;
        }
        stream.write(" style=\"");
        for (CSSItem item : m_items) {
            stream.write(item.property.tag);
            stream.write(':');
            stream.write(item.value);
            stream.write(';');
        }
        stream.write('\"');
    }
}
