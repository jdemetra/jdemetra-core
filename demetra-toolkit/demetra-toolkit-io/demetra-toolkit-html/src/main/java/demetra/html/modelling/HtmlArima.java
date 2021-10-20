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

import demetra.data.DoubleSeq;
import demetra.html.AbstractHtmlElement;
import demetra.html.HtmlElement;
import demetra.html.HtmlStream;
import demetra.html.HtmlTag;
import java.io.IOException;
import java.util.Formatter;
import jdplus.arima.ArimaModel;
import jdplus.arima.IArimaModel;
import jdplus.math.linearfilters.BackFilter;

/**
 *
 * @author Jean Palate & BAYENSK
 */
public class HtmlArima extends AbstractHtmlElement implements HtmlElement {

    private final IArimaModel model;
    static final String AR = "AR:   ", D = "D:   ", MA = "MA:   ", VAR = "Innovation variance: ";
    static final double EPS = 1e-6;

    /**
     *
     * @param model
     */
    public HtmlArima(IArimaModel model) {
        this.model = model;
    }

    /**
     *
     * @param stream
     * @throws IOException
     */
    @Override
    public void write(HtmlStream stream) throws IOException {
        writeModel(stream);
    }

    public void writeModel(HtmlStream stream) throws IOException {
        BackFilter ar = model.getStationaryAr(), delta=model.getNonStationaryAr(), ma=model.getMa();
        double var=model.getInnovationVariance();
        if (ar.getDegree() > 0) {
            stream.write(AR).write(ar.toString()).newLine();
        }
        if (delta.getDegree() > 0) {
            stream.write(D).write(delta.toString()).newLine();
        }
        if (ma.getDegree() > 0) {
            stream.write(MA).write(ma.toString()).newLine();
        }
        if (Math.abs(var - 1) > EPS) {
            String val = new Formatter().format("%.5f", var).toString();
            stream.write(VAR).write(HtmlTag.IMPORTANT_TEXT, val).newLine();
        }
    }

//    public void writeShortModel(HtmlStream stream) throws IOException {
//        double var = model.getInnovationVariance();
//        if (Math.abs(var - 1) > EPS) {
//            String val = new Formatter().format("%.5f", var).toString();
//            stream.write(". " + VAR).write(val);
//        }
//    }
}
