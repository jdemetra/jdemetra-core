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
import ec.tss.html.IHtmlElement;
import ec.tstoolkit.data.Periodogram;
import ec.tstoolkit.maths.Complex;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.utilities.Arrays2;
import java.io.IOException;

/**
 *
 * @author Kristof Bayens
 */
public class HtmlSarimaPolynomials extends AbstractHtmlElement implements IHtmlElement {

    private final SarimaModel model_;
    private final boolean roots_;

    public HtmlSarimaPolynomials(SarimaModel model) {
        model_ = model;
        roots_ = true;
    }

    public HtmlSarimaPolynomials(SarimaModel model, boolean roots) {
        model_ = model;
        roots_ = roots;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {

        Polynomial rar = model_.getRegularAR();

        SarimaSpecification spec = model_.getSpecification();
//        stream.write("regular differencing order: " + Integer.toString(model_.getRegularDifferenceOrder()), new HtmlStyle[]{}).newLine();
//        stream.write("seasonal differencing order: " + Integer.toString(model_.getSeasonalDifferenceOrder())).newLines(2);
        stream.write("Polynomials", HtmlStyle.Bold).newLines(2);
        if (spec.getP() > 0) {
            stream.write("regular AR: " + rar.toString('B', true)).newLine();
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
        if (roots_) {

            Complex[] roots = rar.roots();
            if (Arrays2.isNullOrEmpty(roots)) {
                return;
            }

            double[] arg = new double[roots.length];
            for (int i = 0; i < arg.length; ++i) {
                arg[i] = roots[i].arg();
            }

            stream.newLine().write("Regular AR inverse roots", HtmlStyle.Bold).newLines(2);
            double[] td = Periodogram.getTradingDaysFrequencies(model_.getFrequency());
            for (int i = 0; i < arg.length; ++i) {
                //if (arg[i] >= 0) {
                boolean tdf = false, sf = false;
                for (int j = 0; j < td.length; ++j) {
                    if (Math.abs(Math.abs(arg[i]) - td[j]) < Math.PI / 60) {
                        tdf = true;
                        break;
                    }
                }
                double sfreq = (Math.PI * 2) / model_.getFrequency();
                for (int j = 1; j <= model_.getFrequency() / 2; ++j) {
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
                stream.write(freq.toString(), (tdf || sf ? HtmlStyle.Danger : HtmlStyle.Black)).newLine();
                //}
            }
        }
    }
}
