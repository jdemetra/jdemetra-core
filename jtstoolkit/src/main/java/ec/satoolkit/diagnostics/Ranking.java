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

package ec.satoolkit.diagnostics;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import java.util.Arrays;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
class Ranking {
    static void sort(DataBlock data, DataBlock r) {
	int n = data.getLength();
	Item[] items = new Item[n];
	for (int i = 0; i < n; ++i)
	    items[i] = new Item(i, data.get(i));

	Arrays.sort(items);

	// ranking and correction for tied values...
	int s = 0;
	while (s < n) {
	    int j = s;
	    do
		++s;
	    while (s < n && items[s].val == items[s - 1].val);

	    int k = s - j;
	    if (k == 1)
		items[j].rank = j + 1;
	    else {
		double c = j + (k + 1) * .5;
		for (; j < s; ++j)
		    items[j].rank = c;
	    }
	}

	for (int i = 0; i < n; ++i)
	    r.set(items[i].pos, items[i].rank);
    }

}
