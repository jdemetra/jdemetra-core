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
package demetra.x13.html;

import demetra.html.AbstractHtmlElement;
import demetra.html.Bootstrap4;
import demetra.html.HtmlStream;
import demetra.html.HtmlTable;
import demetra.html.HtmlTableCell;
import demetra.html.HtmlTag;
import java.io.IOException;
import jdplus.x13.Mstatistics;

/**
 *
 * @author Jean Palate
 */
public class HtmlMstatistics extends AbstractHtmlElement {

    Mstatistics mstats;

    public HtmlMstatistics(Mstatistics mstats) {
        this.mstats = mstats;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        writeSummary(stream);
    }

    public void writeSummary(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, "Monitoring and Quality Assessment Statistics").newLine();
        if (mstats.getCIc().getAnnualFrequency() == 2) {
            stream.write("These statistics are not available for half yearly data").newLine();
        } else {
            stream.open(new HtmlTable().withWidth(800));
            for (int i = 1; i <= mstats.getMCount(); ++i) {
                if (mstats.isUsedM(i)) {
                    stream.open(HtmlTag.TABLEROW);
                    stream.write(new HtmlTableCell("M-" + Integer.toString(i)).withWidth(50));
                    double m = mstats.getM(i);
                    HtmlTableCell cell = new HtmlTableCell(df3.format(m)).withWidth(50);
                    if (m > 1) {
                        cell.withClass(Bootstrap4.TEXT_DANGER);
                    }
                    stream.write(cell);
                    stream.write(new HtmlTableCell(M_DESC[i - 1]).withWidth(700).withClass(Bootstrap4.TEXT_LEFT));
                    stream.close(HtmlTag.TABLEROW);
                }
            }
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Q").withWidth(50));
            double q = mstats.getQ();
            HtmlTableCell cell = new HtmlTableCell(df3.format(q)).withWidth(50);
            cell.withClass(Bootstrap4.FONT_WEIGHT_BOLD).withClass(q > 1 ? Bootstrap4.TEXT_DANGER : Bootstrap4.TEXT_SUCCESS);
            stream.write(cell);
            stream.close(HtmlTag.TABLEROW);
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("Q-m2").withWidth(50));
            double qm2 = mstats.getQm2();
            cell = new HtmlTableCell(df3.format(qm2)).withWidth(50);
            cell.withClass(Bootstrap4.FONT_WEIGHT_BOLD).withClass(qm2 > 1 ? Bootstrap4.TEXT_DANGER : Bootstrap4.TEXT_SUCCESS);
            stream.write(cell);
            stream.close(HtmlTag.TABLEROW);
            stream.close(HtmlTag.TABLE);
        }
    }
    private static final String[] M_DESC = new String[]{
        "The relative contribution of the irregular over three months span",
        "The relative contribution of the irregular component to the stationary portion of the variance",
        "The amount of period to period change in the irregular component as compared to the amount of period to period change in the trend",
        "The amount of autocorrelation in the irregular as described by the average duration of run",
        "The number of periods it takes the change in the trend to surpass the amount of change in the irregular",
        "The amount of year to year change in the irregular as compared to the amount of year to year change in the seasonal",
        "The amount of moving seasonality present relative to the amount of stable seasonality",
        "The size of the fluctuations in the seasonal component throughout the whole series",
        "The average linear movement in the seasonal component throughout the whole series",
        "The size of the fluctuations in the seasonal component in the recent years",
        "The average linear movement in the seasonal component in the recent years"
    };
}
