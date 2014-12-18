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
import ec.tss.html.HtmlStyle;
import ec.tss.html.HtmlTable;
import ec.tss.html.HtmlTableCell;
import ec.tss.html.HtmlTableHeader;
import ec.tss.html.HtmlTag;
import ec.tss.html.IHtmlElement;
import ec.tstoolkit.arima.special.MixedAirlineMonitor;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

/**
 *
 * @author pcuser
 */
public class HtmlMixedAirline extends AbstractHtmlElement implements IHtmlElement {

    public HtmlMixedAirline(List<MixedAirlineMonitor.MixedEstimation> models, int best) {
        this.models = models;
        this.best = best;
    }
    private List<MixedAirlineMonitor.MixedEstimation> models;
    private int best;
    private DecimalFormat df4 = new DecimalFormat("0.0000");

    public void write(HtmlStream stream) throws IOException {
        stream.open(new HtmlTable(0, 500));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader("Model"));
        stream.write(new HtmlTableHeader("LogLikelihood"));
        stream.write(new HtmlTableHeader("Theta"));
        stream.write(new HtmlTableHeader("BTheta"));
        stream.write(new HtmlTableHeader("Noise"));
        stream.close(HtmlTag.TABLEROW);
        int icur = 0;
        for (MixedAirlineMonitor.MixedEstimation cur : models) {
            stream.open(HtmlTag.TABLEROW);
            HtmlStyle[] style;
            if (icur++ == best) {
                style = new HtmlStyle[]{HtmlStyle.Bold};
            } else {
                style = new HtmlStyle[0];
            }
            stream.write(new HtmlTableCell(cur.model.toString(), 100, style));
            stream.write(new HtmlTableCell(df4.format(cur.ll.getLogLikelihood()), 100, style));
            stream.write(new HtmlTableCell(df4.format(cur.model.getTheta()), 100, style));
            stream.write(new HtmlTableCell(df4.format(cur.model.getBTheta()), 100, style));
            stream.write(new HtmlTableCell(df4.format(cur.model.getNoisyPeriodsVariance()), 100, style));
            stream.close(HtmlTag.TABLEROW);
        }
        stream.close(HtmlTag.TABLE).newLine();
    }
}
