/*
 * Copyright 2015 National Bank of Belgium
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
/*
 */
package ec.tss.html.implementation;

import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlTable;
import ec.tss.html.HtmlTableCell;
import ec.tss.html.HtmlTableHeader;
import ec.tss.html.HtmlTag;
import java.io.IOException;

/**
 *
 * @author PCUser
 */
public class HtmlSignificantSeasons extends AbstractHtmlElement{

    private final int[] p99, p95;
    
    public HtmlSignificantSeasons(int[] p99, int[] p95){
        this.p99=p99;
        this.p95=p95;
    }
    
    private HtmlTableCell cell(int val){
        if (val<0)
            return new HtmlTableCell(".", 50);
        else
            return new HtmlTableCell(Integer.toString(val), 50);
    }
    
    @Override
    public void write(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, h2, TITLE);
        stream.newLine();
        stream.open(new HtmlTable(0, 200));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader(""));
        stream.write(new HtmlTableHeader("95%"));
        stream.write(new HtmlTableHeader("99%"));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Historical", 100));
        stream.write(cell(p95[0]));
        stream.write(cell(p99[0]));
        stream.close(HtmlTag.TABLEROW);
         stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Current", 100));
        stream.write(cell(p95[1]));
        stream.write(cell(p99[1]));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Forecasts", 100));
        stream.write(cell(p95[2]));
        stream.write(cell(p99[2]));
        stream.close(HtmlTag.TABLEROW);
        stream.close(HtmlTag.TABLE);
        stream.newLine();
    }
    
    private static final String TITLE="Number of periods in a year that have significant seasonality";
}
