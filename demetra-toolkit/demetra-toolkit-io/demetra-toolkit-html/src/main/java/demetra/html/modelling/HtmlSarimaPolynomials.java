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
import demetra.html.Bootstrap4;
import demetra.html.HtmlElement;
import demetra.html.HtmlStream;
import demetra.html.HtmlTag;
import demetra.math.Complex;
import demetra.util.Arrays2;
import java.io.IOException;
import jdplus.data.analysis.Periodogram;
import jdplus.math.polynomials.Polynomial;
import jdplus.sarima.SarimaModel;

/**
 *
 * @author Kristof Bayens
 */
public class HtmlSarimaPolynomials extends AbstractHtmlElement implements HtmlElement {

    private final SarimaModel sarima;
    private final boolean showRoots;

    public HtmlSarimaPolynomials(SarimaModel model) {
        sarima = model;
        showRoots = true;
    }

    public HtmlSarimaPolynomials(SarimaModel model, boolean roots) {
        sarima = model;
        showRoots = roots;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {

        Polynomial rar = sarima.getRegularAR();

        SarimaOrders spec = sarima.orders();
        stream.write(HtmlTag.IMPORTANT_TEXT, "Polynomials").newLines(2);
        if (spec.getP() > 0) {
            stream.write("regular AR: " + rar.toString('B', true)).newLine();
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
        if (showRoots) {

            Complex[] roots = rar.roots();
            if (Arrays2.isNullOrEmpty(roots)) {
                return;
            }

            double[] arg = new double[roots.length];
            for (int i = 0; i < arg.length; ++i) {
                arg[i] = roots[i].arg();
            }

            stream.newLine().write(HtmlTag.IMPORTANT_TEXT, "Regular AR inverse roots").newLines(2);
            double[] td = Periodogram.getTradingDaysFrequencies(sarima.getPeriod());
            for (int i = 0; i < arg.length; ++i) {
                //if (arg[i] >= 0) {
                boolean tdf = false, sf = false;
                for (int j = 0; j < td.length; ++j) {
                    if (Math.abs(Math.abs(arg[i]) - td[j]) < Math.PI / 60) {
                        tdf = true;
                        break;
                    }
                }
                double sfreq = (Math.PI * 2) / sarima.getPeriod();
                for (int j = 1; j <= sarima.getPeriod() / 2; ++j) {
                    if (Math.abs(Math.abs(arg[i]) - j * sfreq) < Math.PI / 60) {
                        sf = true;
                        break;
                    }
                }
                StringBuilder freq = new StringBuilder();
                freq.append("argument=").append(df4.format(arg[i])).append(", modulus=").
                        append(df4.format(1 / roots[i].abs()));
                if (tdf) {
                    freq.append(" (td frequency)");
                } else if (sf) {
                    freq.append(" (seasonal frequency)");
                }
                stream.write(freq.toString(), (tdf || sf ? Bootstrap4.TEXT_DANGER : Bootstrap4.TEXT_DARK)).newLine();
                //}
            }
        }
    }
}
