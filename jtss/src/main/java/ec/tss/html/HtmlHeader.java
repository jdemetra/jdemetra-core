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

/**
 *
 * @author Jean Palate
 */
public class HtmlHeader extends AbstractHtmlElement {

    private final int level;
    private final String txt;
    private final boolean nl;

    public HtmlHeader(final int level, final String txt, boolean newLine) {
        this.level = level;
        this.txt = txt;
        this.nl=newLine;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        HtmlTag h;
        switch (level) {
            case 1:
                h = HtmlTag.HEADER1;
                break;
            case 2:
                h = HtmlTag.HEADER2;
                break;
            case 3:
                h = HtmlTag.HEADER3;
                break;
            case 4:
                h = HtmlTag.HEADER4;
                break;
            case 5:
                h = HtmlTag.HEADER5;
                break;
            default:
                h = HtmlTag.HEADER6;
                break;
        }
        if (level <= 3) {
            stream.write(h, txt);
        }
        else {
            stream.write(h, txt);
        }
        if (nl)
            stream.newLine();
    }
}
