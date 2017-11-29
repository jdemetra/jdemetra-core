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

package demetra.x11;

import demetra.data.DataBlock;
import demetra.data.DataWindow;
import demetra.data.DoubleSequence;
import static demetra.data.Doubles.average;
import static demetra.data.Doubles.sum;
import demetra.design.Development;
import demetra.maths.linearfilters.IFiniteFilter;


/**
 *
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class AsymmetricEndPoints implements IEndPointsProcessor {

    private IFiniteFilter[] filters;

    /**
     * 
     * @param filters
     */
    public AsymmetricEndPoints(IFiniteFilter[] filters)
    {
	this.filters = filters;
    }

    @Override
    public void process(DoubleSequence in, DataBlock out) {
	int n = filters.length;
	// complete the missing items...
	int plen = in.length();
	// filter[0].length = 2*n
	// The first items we have to complete (with filters[0])
	// are at position n-1, plen -n.
	// They needs inputs from [0, 2*n[ (or [plen-2*n, plen[),
	// which is not possible when 2*n > plen.
	// More generally, filters[k], (k in [0, n[) has a length
	// equal to 2*n - k. It is used to fill the item at position
	// n-k-1, plen-n+k
	// The first used filter (k) is min(plen/2, max(0, 2*n-plen)).
	// The missing items, if any are filled with the means of the obs (?).

	int ifilter = 2 * n - plen;
	if (ifilter < 0)
	    ifilter = 0;

	int istart = n - ifilter;
	int plen2 = (plen + 1) / 2;
	if (istart > plen2) {
	    istart = plen2;
	    ifilter = n - istart;
	}

	int rlen = 2 * n - ifilter;
	DoubleSequence beg = in.reverse().extract(plen - rlen, rlen), end = in.extract(plen - rlen, rlen);
	int icur = istart;
	while (icur > 0) {
            IFiniteFilter f=filters[ifilter++];
            out.set(plen - icur, f.apply(end));
            out.set(--icur, f.apply(beg));
            end=end.drop(1, 0);
            beg=beg.drop(1, 0);
	}
	if (istart < n) {
	    double av = average(in);
	    out.range(istart, plen - istart).set(av);
	}
    }
}
