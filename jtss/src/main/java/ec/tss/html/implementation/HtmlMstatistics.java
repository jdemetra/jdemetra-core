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

import ec.satoolkit.x11.Mstatistics;
import ec.tss.html.*;
import java.io.IOException;

/**
 *
 * @author Jean Palate
 */
public class HtmlMstatistics extends AbstractHtmlElement {

    Mstatistics stats_;

    public HtmlMstatistics(Mstatistics mstats) {
        stats_ = mstats;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        writeSummary(stream);
    }

    public void writeSummary(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, h2, "Monitoring and Quality Assessment Statistics").newLine();
        stream.open(new HtmlTable(0, 800));
        for (int i = 1; i <= stats_.getMCount(); ++i) {
            if (stats_.isUsedM(i)) {
                stream.open(HtmlTag.TABLEROW);
                stream.write(new HtmlTableCell("M-" + Integer.toString(i), 50));
                double m = stats_.getM(i);
                HtmlTableCell cell = new HtmlTableCell(df3.format(m), 50);
                if (m > 1) {
                    cell.styles = new HtmlStyle[]{HtmlStyle.Danger};
                }
                stream.write(cell);
                stream.write(new HtmlTableCell(M_DESC[i - 1], 700, HtmlStyle.Left));
                stream.close(HtmlTag.TABLEROW);
            }
        }
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Q", 50));
        double q = stats_.getQ();
        HtmlTableCell cell = new HtmlTableCell(df3.format(q), 50);
        cell.styles = new HtmlStyle[]{q > 1 ? HtmlStyle.Danger : HtmlStyle.Success, HtmlStyle.Bold};
        stream.write(cell);
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Q-m2", 50));
        double qm2 = stats_.getQm2();
        cell = new HtmlTableCell(df3.format(qm2), 50);
        cell.styles = new HtmlStyle[]{qm2 > 1 ? HtmlStyle.Danger : HtmlStyle.Success, HtmlStyle.Bold};
        stream.write(cell);
        stream.close(HtmlTag.TABLEROW);
        stream.close(HtmlTag.TABLE);
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
