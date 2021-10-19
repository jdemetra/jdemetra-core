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
package demetra.html;

import java.text.DecimalFormat;

/**
 *
 * @author Jean Palate
 */
public abstract class AbstractHtmlElement implements HtmlElement {

    @Deprecated
    protected static final CssStyle h1 = new CssStyle();
    @Deprecated
    protected static final CssStyle h2 = new CssStyle();
    @Deprecated
    protected static final CssStyle h3 = new CssStyle();
    @Deprecated
    protected static final CssStyle h4 = new CssStyle();
    @Deprecated
    protected static final CssStyle d1 = new CssStyle();
    
    protected static final DecimalFormat df2 = new DecimalFormat("0.00");
    protected static final DecimalFormat df3 = new DecimalFormat("0.000");
    protected static final DecimalFormat df4 = new DecimalFormat("0.0000");
    protected static final DecimalFormat dg2 = new DecimalFormat("0.##");
    protected static final DecimalFormat dg6 = new DecimalFormat("0.######");
    protected static final DecimalFormat de6 = new DecimalFormat("0.######E0#");
    protected static final DecimalFormat pc2 = new DecimalFormat();

    static {
        pc2.setMultiplier(100);
        pc2.setMaximumFractionDigits(2);
        pc2.setPositiveSuffix("%");
        pc2.setNegativeSuffix("%");
        h1.add(CssProperty.FONT_WEIGHT, "bold");
        h1.add(CssProperty.FONT_SIZE, "110%");
        h1.add(CssProperty.TEXT_DECORATION, "underline");
        h2.add(CssProperty.FONT_WEIGHT, "bold");
        h2.add(CssProperty.TEXT_DECORATION, "underline");
        h3.add(CssProperty.TEXT_DECORATION, "underline");
        h4.add(CssProperty.FONT_WEIGHT, "italic");
//        d1.add(CssProperty.FONT_SIZE, "11");
    }

    protected static String format(double n) {
        if (Math.abs(n) < 1e-3 || Math.abs(n) > 1000000) {
            return de6.format(n);
        } else {
            return dg6.format(n);
        }
    }
    
    protected static String formatT(double t) {
        if (t < -T_BIG) {
            return T_LBIG;
        } else if (t > T_BIG) {
            return T_UBIG;
        } else {
            return df2.format(t);
        }
    }
    
    private static final double T_BIG = 100;
    private static final String T_LBIG = "&lt -100", T_UBIG = "&gt 100";
    
}
