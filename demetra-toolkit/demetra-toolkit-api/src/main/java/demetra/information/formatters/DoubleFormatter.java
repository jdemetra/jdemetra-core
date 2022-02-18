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

package demetra.information.formatters;

import java.text.DecimalFormat;

/**
 *
 * @author Jean Palate
 */
class DoubleFormatter implements InformationFormatter {
    
    private static final DecimalFormat df6 = new DecimalFormat();
    private final DecimalFormat fmt;
    
    static{
        df6.setMaximumFractionDigits(9);
        df6.setGroupingUsed(false);
    }
    
    public DoubleFormatter(){
        fmt=df6;
    }
    
    public DoubleFormatter(DecimalFormat fmt){
        this.fmt=fmt;
    }

    @Override
    public String format(Object obj, int item) {
        if (item > 0)
            return null;
        double val=(Double) obj;
        if (! Double.isFinite(val))
            return null;
        return fmt.format(val);
    }
}
