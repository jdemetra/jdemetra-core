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
import ec.tstoolkit.information.ParameterInfo;
import java.text.DecimalFormat;

/**
 *
 * @author Jean Palate
 */
public class ParameterInfoFormatter implements IStringFormatter {

    private static final DecimalFormat df6 = new DecimalFormat();
    private static final DecimalFormat df4 = new DecimalFormat();

    static {
        df6.setMaximumFractionDigits(6);
        df4.setMaximumFractionDigits(3);
        df6.setGroupingUsed(false);
        df4.setGroupingUsed(false);
    }
    private final DecimalFormat fmt;

    public ParameterInfoFormatter() {
        fmt = df6;
    }

    public ParameterInfoFormatter(DecimalFormat fmt) {
        this.fmt = fmt;
    }

    @Override
    public String format(Object obj, int item) {

        ParameterInfo param = (ParameterInfo) obj;
        switch (item) {
            case 0:
            case 1:
                return fmt.format(param.value);
            case 2:
                if (param.stde == 0) {
                    return "";
                } else {
                    return df4.format(param.value / param.stde);
                }
            case 3:
                return fmt.format(param.stde);
            case 4:
                return df4.format(param.pvalue);
            default:
                return "";
        }
    }
}
