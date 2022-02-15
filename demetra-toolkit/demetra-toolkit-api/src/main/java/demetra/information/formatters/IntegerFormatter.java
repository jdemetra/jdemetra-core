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

import java.text.NumberFormat;

/**
 *
 * @author Jean Palate
 */
class IntegerFormatter implements InformationFormatter {

    @Override
    public String format(Object obj, int item) {
        if (item > 0)
            return null;
        int i = (Integer)obj;
        NumberFormat ifmt = NumberFormat.getIntegerInstance();
        ifmt.setGroupingUsed(false);
        return ifmt.format(i);
    }
}
