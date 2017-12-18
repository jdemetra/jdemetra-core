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

package demetra.maths.linearfilters;

import demetra.design.Development;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.experimental.UtilityClass
public final class HendersonFilters {

    private final Map<Integer, SymmetricFilter> FILTERSTORE=new HashMap<>();

    /**
     * 
     * @param length
     * @return
     */
    public synchronized SymmetricFilter withLength(int length) {
        SymmetricFilter filter=FILTERSTORE.get(length);
        if (filter != null)
            return filter;
	if (length % 2 == 0)
	    throw new LinearFilterException(
		    "Invalid length for Henderson filter. Should be odd");
	int m = length / 2;
	double[] c = new double[m + 1];
	int n = m + 2;

	double n2 = n * n;
	for (int i = 0; i < m + 1; i++) {
	    double ii = (i - m) * (i - m);
	    double up = 315 * (n2 - n * 2 + 1 - (ii));
	    up *= n2 - ii;
	    up *= n2 + n * 2 + 1 - ii;
	    up *= n2 * 3 - 16 - ii * 11;
	    double down = n * 8;
	    down *= n2 - 1;
	    down *= n2 * 4 - 1;
	    down *= n2 * 4 - 9;
	    down *= n2 * 4 - 25;
	    c[m - i] = up / down;
	}
	filter=SymmetricFilter.of(c);
        FILTERSTORE.put(length, filter);
        return filter;
    }
}
