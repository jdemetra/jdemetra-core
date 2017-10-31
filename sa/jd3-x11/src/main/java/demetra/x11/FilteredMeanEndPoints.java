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
import demetra.design.Development;
import demetra.maths.linearfilters.SymmetricFilter;

/**
 * extends a series with the mean of the first/last observations (the number of
 * observations used to compute the mean being defined by the number of missing
 * values (= filter.length/2) and filters the extended series.
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class FilteredMeanEndPoints implements IEndPointsProcessor {

    private SymmetricFilter filter;

    /**
     * 
     * @param filter
     */
    public FilteredMeanEndPoints(SymmetricFilter filter)
    {
	this.filter = filter;
    }

    @Override
    public void process(DataBlock in, DataBlock out) {
	/*
	 * Too complicate
	 * 
	 * double[] w=filter.getWeights(); int n=in.getLength(); // computes the
	 * means int len =w.length/2; DataBlock rbeg=in.range(0, len).reverse(),
	 * rend=in.range(n-len, n); double beg=rbeg.sum()/len; double
	 * end=rend.sum()/len;
	 * 
	 * DataBlock f=new DataBlock(w, w.length-len, w.length, 1); double
	 * m=f.sum(); int imax=len; if (n < w.length) imax=n/2; for (int i=0;
	 * i<imax; ++i) { rbeg.expand(1, 0); rend.expand(1, 0); f.expand(1, 0);
	 * out.set(i, rbeg.dot(f)+m*beg); out.set(n-i-1, rend.dot(f)+m*end);
	 * m-=w[len+i]; } // complete the missing items if (imax <len) { for
	 * (int i =imax; i< n-imax; ++i) {
	 * 
	 * } }
	 */

	int len = filter.length() / 2;
	int n = in.length();

	// expand the block...
	double[] tmp = new double[n + 2 * len];
	// copy the input
	in.copyTo(tmp, len);

	// computes the means
	DataBlock rbeg = in.range(0, len), rend = in.range(n - len, n);
	double beg = rbeg.sum() / len;
	double end = rend.sum() / len;
	// fill the first/last items
	for (int i = 0, j = n + len; i < len; ++i, ++j) {
	    tmp[i] = beg;
	    tmp[j] = end;
	}
	filter.apply(DataBlock.ofInternal(tmp), out);
    }

}
