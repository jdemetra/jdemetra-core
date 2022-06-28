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
package demetra.sa.html;

import demetra.html.AbstractHtmlElement;
import demetra.html.HtmlStream;
import demetra.html.HtmlTable;
import demetra.html.HtmlTableCell;
import demetra.html.HtmlTag;
import demetra.sa.StationaryVarianceDecomposition;
import java.io.IOException;

/**
 *
 * @author Jean Palate
 */
public class HtmlStationaryVarianceDecomposition extends AbstractHtmlElement {

    private final StationaryVarianceDecomposition vdecomp;

    public HtmlStationaryVarianceDecomposition(StationaryVarianceDecomposition vdecomp) {
        this.vdecomp = vdecomp;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, TITLE);
        stream.newLine();
        stream.write(vdecomp.getTrendType().toString());
        stream.newLine();
        stream.open(new HtmlTable().withWidth(200));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Cycle").withWidth(100));
        stream.write(new HtmlTableCell(df2.format(100 * vdecomp.getC())).withWidth(100));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Seasonal").withWidth(100));
        stream.write(new HtmlTableCell(df2.format(100 * vdecomp.getS())).withWidth(100));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Irregular").withWidth(100));
        stream.write(new HtmlTableCell(df2.format(100 * vdecomp.getI())).withWidth(100));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("TD & Hol.").withWidth(100));
        stream.write(new HtmlTableCell(df2.format(100 * vdecomp.getCalendar())).withWidth(100));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Others").withWidth(100));
        stream.write(new HtmlTableCell(df2.format(100 * vdecomp.getP())).withWidth(100));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Total").withWidth(50));
        stream.write(new HtmlTableCell(df2.format(100 * vdecomp.total())).withWidth(50));
        stream.close(HtmlTag.TABLEROW);
        stream.close(HtmlTag.TABLE);
        stream.newLine();
    }

    private static final String TITLE = "Relative contribution of the components to the stationary portion of the variance in the original series, after the removal of the long term trend";

}
