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

import com.google.common.base.Strings;
import ec.tss.sa.output.BasicConfiguration;
import ec.tstoolkit.information.RegressionItem;
import java.text.DecimalFormat;

/**
 *
 * @author Jean Palate
 */
public class RegressionItemFormatter implements IStringFormatter {

    private static final DecimalFormat df6 = new DecimalFormat();
    private static final DecimalFormat df4 = new DecimalFormat();

    static {
        df6.setMaximumFractionDigits(6);
        df4.setMaximumFractionDigits(4);
        df6.setGroupingUsed(false);
        df4.setGroupingUsed(false);
    }
    private final DecimalFormat fmt;
    private final boolean showDesc_;

    public RegressionItemFormatter() {
        fmt = df6;
        showDesc_ = false;
    }

    public RegressionItemFormatter(boolean showdesc) {
        fmt = df6;
        showDesc_ = showdesc;
    }

    public RegressionItemFormatter(DecimalFormat fmt, boolean showdesc) {
        this.fmt = fmt;
        showDesc_ = showdesc;
    }

    @Override
    public int getDefaultRepresentationLength(){
        return 3;
    }

    @Override
    public String format(Object obj, int item) {

        RegressionItem reg = (RegressionItem) obj;
        if (item == 0) {
            return format(reg);
        }
        if (Strings.isNullOrEmpty(reg.description) || !showDesc_) {
            ++item;
        }
        switch (Math.abs(item)) {
            case 1:
                return StringFormatter.cleanup(reg.description);
            case 2:
                return fmt.format(reg.coefficient);
            case 3:
                 if (reg.stdError == 0) {
                    return "";
                } else {
                    return df4.format(reg.coefficient / reg.stdError);
                }
            case 4:
                return fmt.format(reg.stdError);
            case 5:
              return df4.format(reg.pValue);
             default:
                return "";
        }
    }

    private String format(RegressionItem reg) {
        StringBuilder builder = new StringBuilder();
        if (reg.description != null) {
            builder.append(reg.description).append(':');
        }
        builder.append(df4.format(reg.coefficient));
        if (reg.stdError != 0) {
            builder.append('[').append(
                    df4.format(reg.coefficient / reg.stdError)).append(']');
        }
        return builder.toString();

    }
    

}
