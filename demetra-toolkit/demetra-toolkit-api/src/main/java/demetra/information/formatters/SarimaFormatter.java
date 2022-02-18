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

import demetra.arima.SarimaOrders;

/**
 *
 * @author Jean Palate
 */
public class SarimaFormatter implements InformationFormatter {

    @Override
    public String format(Object obj, int item) {

        SarimaOrders orders = (SarimaOrders) obj;
        switch (item) {
            case 0:
                return orders.toString();
            case 1:
                return Integer.toString(orders.getP());
            case 2:
                return Integer.toString(orders.getD());
            case 3:
                return Integer.toString(orders.getQ());
            case 4:
                return Integer.toString(orders.getBp());
            case 5:
                return Integer.toString(orders.getBd());
            case 6:
                return Integer.toString(orders.getBq());
            default:
                return null;
        }
    }
}
