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
package demetra.html.modelling;

import demetra.arima.SarimaOrders;
import demetra.html.AbstractHtmlElement;
import demetra.html.HtmlElement;
import demetra.html.HtmlStream;
import java.io.IOException;
import jdplus.sarima.SarimaModel;

/**
 *
 * @author Kristof Bayens
 */
public class HtmlSarimaModel extends AbstractHtmlElement implements HtmlElement {

    private final SarimaModel sarima;
    private final boolean showDiff;

    public HtmlSarimaModel(SarimaModel model) {
        sarima = model;
        showDiff = false;
    }

    public HtmlSarimaModel(SarimaModel model, boolean showdiff) {
        sarima = model;
        showDiff = showdiff;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {

        SarimaOrders spec = sarima.orders();
        if (showDiff) {
            stream.write("regular differencing order: " + Integer.toString(spec.getD())).newLine();
            stream.write("seasonal differencing order: " + Integer.toString(spec.getBd())).newLines(2);
        }
        if (spec.getP() > 0) {
            stream.write("regular AR: " + sarima.getRegularAR().toString('B', true)).newLine();
        }
        if (spec.getBp() > 0) {
            stream.write("seasonal AR: " + sarima.getSeasonalAR().toString('S', true)).newLine();
        }
        if (spec.getQ() > 0) {
            stream.write("regular MA: " + sarima.getRegularMA().toString('B', true)).newLine();
        }
        if (spec.getBq() > 0) {
            stream.write("seasonal MA: " + sarima.getSeasonalMA().toString('S', true)).newLine();
        }
    }
}
