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
import ec.tss.html.Bootstrap4;
import ec.tss.html.HtmlClass;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlTable;
import ec.tss.html.HtmlTableCell;
import ec.tss.html.HtmlTableHeader;
import ec.tss.html.HtmlTag;
import ec.tss.html.IHtmlElement;
import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.arima.special.GeneralizedAirlineModel;
import java.io.IOException;

/**
 *
 * @author Jean Palate
 */
public class HtmlGeneralizedAirline extends AbstractHtmlElement implements IHtmlElement {

    public HtmlGeneralizedAirline(RegArimaEstimation<GeneralizedAirlineModel>[] models, int best, boolean decomp) {
        this.models = models;
        this.best = best;
        this.decomp=decomp;
    }
    private final RegArimaEstimation<GeneralizedAirlineModel>[] models;
    private final int best;
    private final boolean decomp;

    @Override
    public void write(HtmlStream stream) throws IOException {
        stream.open(new HtmlTable().withWidth(500));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader("Model").withWidth(100));
        stream.write(new HtmlTableHeader("LogLikelihood").withWidth(80));
        int freq=models[0].model.getArima().getFrequency();
        for (int i = 0; i < 4; ++i) {
            StringBuilder builder = new StringBuilder();
            builder.append("p(").append(i + 1).append(')');
            stream.write(new HtmlTableHeader(builder.toString()).withWidth(80));
        }
        stream.close(HtmlTag.TABLEROW);
        int icur = 0;
        for (int i = 0; i < models.length; ++i) {
            stream.open(HtmlTag.TABLEROW);
            HtmlClass style = icur++ == best ? Bootstrap4.FONT_WEIGHT_BOLD : HtmlClass.NO_CLASS;
            stream.write(new HtmlTableCell(models[i].model.getArima().getModelType()).withWidth(100).withClass(style));
            stream.write(new HtmlTableCell(df4.format(models[i].likelihood.getLogLikelihood())).withWidth(80).withClass(style));
            double[] p = models[i].model.getArima().getCoefficients();
            for (int j = 0; j < p.length; ++j) {
                stream.write(new HtmlTableCell(df4.format(j > 0 ? Math.pow(p[j], freq) : p[j])).withWidth(80).withClass(style));
            }
            stream.close(HtmlTag.TABLEROW);
        }
        stream.close(HtmlTag.TABLE).newLine();
        if (! decomp){
            stream.write(HtmlTag.IMPORTANT_TEXT, "Non decomposable model", Bootstrap4.TEXT_DANGER);
            stream.newLine();
        }
    }
}
