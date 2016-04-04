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

import ec.satoolkit.diagnostics.StationaryVarianceDecomposition;
import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlTable;
import ec.tss.html.HtmlTableCell;
import ec.tss.html.HtmlTag;
import java.io.IOException;

/**
 *
 * @author Jean Palate
 */
public class HtmlStationaryVarianceDecomposition extends AbstractHtmlElement{
    
    private final StationaryVarianceDecomposition vdecomp;
    
    public HtmlStationaryVarianceDecomposition(StationaryVarianceDecomposition vdecomp){
        this.vdecomp=vdecomp;
    }
    
    @Override
    public void write(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, h2, TITLE);
        stream.newLine();
        stream.write(vdecomp.getTrendComputer().toString());
        stream.newLine();
        stream.open(new HtmlTable(0, 200));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Cycle", 100));
        stream.write(new HtmlTableCell(df2.format(100 * vdecomp.getVarC()), 100));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Seasonal", 100));
        stream.write(new HtmlTableCell(df2.format(100 * vdecomp.getVarS()), 100));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Irregular", 100));
        stream.write(new HtmlTableCell(df2.format(100 * vdecomp.getVarI()), 100));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("TD & Hol.", 100));
        stream.write(new HtmlTableCell(df2.format(100 * vdecomp.getVarTD()), 100));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Others", 100));
        stream.write(new HtmlTableCell(df2.format(100 * vdecomp.getVarP()), 100));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Total", 50));
        stream.write(new HtmlTableCell(df2.format(100 * vdecomp.getVarTotal()), 50));
        stream.close(HtmlTag.TABLEROW);
        stream.close(HtmlTag.TABLE);
        stream.newLine();
    }

    private static final String TITLE = "Relative contribution of the components to the stationary portion of the variance in the original series, after the removal of the long term trend";

}
