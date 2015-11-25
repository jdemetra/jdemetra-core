/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tss.html.implementation;

import ec.satoolkit.x11.MsrTable;
import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlTable;
import ec.tss.html.HtmlTableCell;
import ec.tss.html.HtmlTableHeader;
import ec.tss.html.HtmlTag;
import java.io.IOException;

/**
 *
 * @author Thomas Witthohn
 */
public class HtmlMovingSeasonalityRatios extends AbstractHtmlElement {

    private final MsrTable msrTable;
    private final String TITLE = "Moving Seasonality Ratios (MSR)";
    private final String[] HEADERS = new String[]{"Period", "I", "S", "MSR"};

    public HtmlMovingSeasonalityRatios(MsrTable msrTable) {
        this.msrTable = msrTable;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, h2, TITLE);
        double[][] Q = new double[3][];
        Q[0] = msrTable.getMeanIrregularEvolutions();
        Q[1] = msrTable.getMeanSeasonalEvolutions();

        int len = Q[0].length;

        Q[2] = new double[len];
        for (int i = 0; i < len; ++i) {
            Q[2][i] = msrTable.getRMS(i);
        }

        stream.open(new HtmlTable(0, 50 * HEADERS.length));
        stream.open(HtmlTag.TABLEROW);
        for (int j = 0; j < HEADERS.length; ++j) {
            stream.write(new HtmlTableHeader(HEADERS[j], 50));
        }
        stream.close(HtmlTag.TABLEROW);
        for (int i = 1; i <= len; ++i) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell(Integer.toString(i), 50));
            for (int j = 0; j < Q.length; ++j) {
                if (Q[j] != null) {
                    stream.write(new HtmlTableCell(df2.format(Q[j][i - 1]), 50));
                } else {
                    stream.write(new HtmlTableCell(".", 50));
                }
            }
            stream.close(HtmlTag.TABLEROW);
        }
        stream.close(HtmlTag.TABLE);
        stream.newLines(2);
    }

}
