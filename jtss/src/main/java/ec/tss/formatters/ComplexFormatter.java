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
package ec.tss.formatters;

import ec.tstoolkit.maths.Complex;
import java.text.DecimalFormat;

/**
 *
 * @author Jean Palate
 */
public class ComplexFormatter implements IStringFormatter {

    private static final DecimalFormat df4 = new DecimalFormat();

    static {
        df4.setMaximumFractionDigits(4);
        df4.setGroupingUsed(false);
    }
    private final DecimalFormat fmt;

    public ComplexFormatter() {
        fmt = df4;
    }

    public ComplexFormatter(DecimalFormat fmt) {
        this.fmt = fmt;
    }

    @Override
    public int getDefaultRepresentationLength() {
        return 2;
    }

    @Override
    public String format(Object obj, int item) {

        Complex c = (Complex) obj;
        if (item == 0) {
            return c.toString();
        }
        switch (Math.abs(item)) {
            case 1:
                return fmt.format(c.abs());
            case 2:
                double arg = c.arg();
                if (arg == 0) {
                    return "";
                } else {
                    double period = (2 * Math.PI) / arg;
                    if (period < -2 + 1e-6 && period > -2 - 1e-6) {
                        return "2";
                    } else {
                        return fmt.format(period);
                    }
                }
            default:
                return "";
        }
    }
}
