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

import ec.tstoolkit.Parameter;
import java.text.DecimalFormat;

/**
 *
 * @author Jean Palate
 */
public class ParameterFormatter implements IStringFormatter {

    private static final DecimalFormat df6 = new DecimalFormat();
    private static final DecimalFormat df3 = new DecimalFormat();

    static {
        df6.setMaximumFractionDigits(6);
        df3.setMaximumFractionDigits(3);
        df6.setGroupingUsed(false);
        df3.setGroupingUsed(false);
    }
    private final DecimalFormat fmt;

    public ParameterFormatter() {
        fmt = df6;
    }

    public ParameterFormatter(DecimalFormat fmt) {
        this.fmt = fmt;
    }

    @Override
    public String format(Object obj, int item) {

        Parameter param = (Parameter) obj;
        switch (item) {
            case 0:
            case 1:
                return fmt.format(param.getValue());
            case 2:
                if (param.getStde() == 0) {
                    return "";
                } else {
                    return df3.format(param.getValue() / param.getStde());
                }
            case 3:
                return fmt.format(param.getStde());
            default:
                return "";
        }
    }
}
