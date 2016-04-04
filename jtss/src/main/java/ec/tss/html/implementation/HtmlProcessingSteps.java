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

package ec.tss.html.implementation;

import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlTable;
import ec.tss.html.HtmlTableCell;
import ec.tss.html.HtmlTag;
import ec.tss.html.IHtmlElement;
import ec.tstoolkit.information.InformationSet;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class HtmlProcessingSteps extends AbstractHtmlElement implements IHtmlElement {

    InformationSet history_;

    public HtmlProcessingSteps(final InformationSet history) {
        history_=history;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        if (history_ == null) {
            return;
        }
        stream.write(HtmlTag.HEADER1, h1, "Processing steps").newLine();
        List<String> hdic = history_.getDictionary();
        stream.open(new HtmlTable(0, 300));
        for (String s : hdic) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell(s, 100));
            Object obj = history_.search(s, Object.class);
            if (obj != null) {
                stream.write(new HtmlTableCell(obj.toString(), 200));
            }
            stream.close(HtmlTag.TABLEROW);
        }
        stream.close(HtmlTag.TABLE);
    }
}
