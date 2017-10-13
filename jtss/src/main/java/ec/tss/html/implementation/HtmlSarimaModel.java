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
import ec.tss.html.IHtmlElement;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import java.io.IOException;

/**
 *
 * @author Kristof Bayens
 */
public class HtmlSarimaModel extends AbstractHtmlElement implements IHtmlElement {

    private final SarimaModel model_;
    private final boolean diff_;

    public HtmlSarimaModel(SarimaModel model) {
        model_ = model;
        diff_ = false;
    }

    public HtmlSarimaModel(SarimaModel model, boolean showdiff) {
        model_ = model;
        diff_ = showdiff;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {

        SarimaSpecification spec = model_.getSpecification();
        if (diff_) {
            stream.write("regular differencing order: " + Integer.toString(model_.getRegularDifferenceOrder())).newLine();
            stream.write("seasonal differencing order: " + Integer.toString(model_.getSeasonalDifferenceOrder())).newLines(2);
        }
        if (spec.getP() > 0) {
            stream.write("regular AR: " + model_.getRegularAR().toString('B', true)).newLine();
        }
        if (spec.getBP() > 0) {
            stream.write("seasonal AR: " + model_.getSeasonalAR().toString('S', true)).newLine();
        }
        if (spec.getQ() > 0) {
            stream.write("regular MA: " + model_.getRegularMA().toString('B', true)).newLine();
        }
        if (spec.getBQ() > 0) {
            stream.write("seasonal MA: " + model_.getSeasonalMA().toString('S', true)).newLine();
        }
    }
}
