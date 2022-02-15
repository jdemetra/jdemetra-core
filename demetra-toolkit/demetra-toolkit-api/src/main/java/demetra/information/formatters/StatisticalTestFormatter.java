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

import demetra.stats.StatisticalTest;
import java.text.DecimalFormat;

/**
 *
 * @author Jean Palate
 */
public class StatisticalTestFormatter implements InformationFormatter {

    private static final DecimalFormat df6 = new DecimalFormat();
    private static final DecimalFormat df4 = new DecimalFormat();
     static {
        df6.setMaximumFractionDigits(6);
        df4.setMaximumFractionDigits(4);
        df6.setGroupingUsed(false);
        df4.setGroupingUsed(false);
    }
   private final DecimalFormat fmt;

    public StatisticalTestFormatter(){
        fmt=df6;
    }
    
    public StatisticalTestFormatter(DecimalFormat fmt){
        this.fmt=fmt;
    }
    
    @Override
    public String format(Object obj, int item) {

        StatisticalTest test = (StatisticalTest)obj;
        if (item == 0)
            return fmt.format(test.getValue());
        if (test.getDescription() != null)
            ++item;
        switch (Math.abs(item)) {
            case 1:
                return test.getDescription();
            case 2:
                return fmt.format(test.getValue());
            case 3:
                return df4.format(test.getPvalue());
            default:
                return null;
        }
    }
}
