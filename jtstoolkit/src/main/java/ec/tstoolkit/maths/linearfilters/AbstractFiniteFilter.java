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
package ec.tstoolkit.maths.linearfilters;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.Complex;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public abstract class AbstractFiniteFilter implements IFiniteFilter {

    /**
     * 
     * @param in
     * @param out
     * @param lb
     * @param ub
     */
    protected void defaultFilter(DataBlock in, DataBlock out, int lb, int ub)
    {
	int nw = ub - lb + 1;
	DataBlock cur = in.drop(0, nw - 1);

	out.product(cur, this.getWeight(lb));
	for (int j = 1; j < nw; ++j) {
	    cur.move(1);
	    out.addAY(this.getWeight(lb + j), cur);
	}
    }

    /**
     *
     * @param in
     * @param out
     * @param lb
     * @param ub
     */
    protected void exFilter(final DataBlock in, final DataBlock out,
	    final int lb, final int ub) {
	if (lb > 0 || ub < 0)
	    throw new LinearFilterException(
		    LinearFilterException.InvalidSFilter);
	defaultFilter(in, out.drop(-lb, ub), lb, ub);
    }

    /**
     * 
     * @param in
     * @param out
     */
    public void extendedFilter(final DataBlock in, final DataBlock out) {
	int lb = getLowerBound(), ub = getUpperBound();
	int nw = ub - lb + 1;

	out.product(in, this.getWeight(ub--));
	for (int j = 1; j < nw; ++j)
	    out.drop(j, 0).addAY(this.getWeight(ub--), in.drop(0, j));
    }

    /**
     * 
     * @param in
     * @param out
     * @return
     */
    @Override
    public boolean filter(DataBlock in, DataBlock out) {
	int lb = getLowerBound(), ub = getUpperBound();
	int nw = ub - lb + 1;
	int nin = in.getLength();
	int nout = out.getLength();
	if (nin == nout) {
	    extendedFilter(in, out);
	    return true;
	} else {
	    if (nin < nw || out.getLength() != nin - nw + 1)
		return false;
	    defaultFilter(in, out, lb, ub);
	    return true;
	}
    }

    /**
     * 
     * @param freq
     * @return
     */
    @Override
    public Complex frequencyResponse(final double freq) {
	double[] w = getWeights();
	return Utilities.frequencyResponse(w, getLowerBound(), freq);
    }

    /**
     * 
     * @return
     */
    @Override
    public int getLength() {
	return getUpperBound() - getLowerBound() + 1;
    } // UB-LB+1

    /**
     * 
     * @return
     */
    @Override
    public abstract int getLowerBound();

    @Override
    public abstract int getUpperBound();

    /**
     * 
     * @param pos
     * @return
     */
    @Override
    public abstract double getWeight(int pos);

    /**
     * 
     * @return
     */
    @Override
    public double[] getWeights() {
	int lb = getLowerBound(), ub = getUpperBound();
	double[] w = new double[ub - lb + 1];
	for (int i = 0; i < w.length; ++i)
	    w[i] = getWeight(lb + i);
	return w;
    }

    // IFilter
    /**
     * 
     * @return
     */
    @Override
    public boolean hasLowerBound() {
	return true;
    }

    @Override
    public boolean hasUpperBound() {
	return true;
    }

    /**
     * 
     * @param data
     * @return
     */
    @Override
    public int inPlaceFilter(DataBlock data) {
	int start = this.getLength() - 1;
	int n = data.getLength() - start;
	if (n <= 0)
	    return -1;
	double[] x = new double[n];
	DataBlock r = new DataBlock(x);
	this.filter(data, r);
	data.drop(start, 0).copyFrom(x, 0);
	return start;
    }

    /**
     * Solves recursively the relationship: F * out = in, considering that the
     * initial values are 0.
     * 
     * @param in
     * @param out
     */
    public void solve(final double[] in, final double[] out) {
	int n = in.length;

	double[] w = getWeights();
	int u = w.length - 1;

	// initial iterations
	int nmax = Math.min(w.length, n);
	double a = w[u];
	for (int i = 0; i < nmax; ++i) {
	    double z = in[i];
	    for (int j = 1; j <= i; ++j)
		z -= out[i - j] * w[u - j];
	    out[i] = z / a;
	}
	for (int i = w.length; i < n; ++i) {
	    double z = in[i];
	    for (int j = 1; j <= u; ++j)
		z -= out[i - j] * w[u - j];
	    out[i] = z / a;
	}

    }

    public void solve(final DataBlock in, final DataBlock out) {
	int n = in.getLength();

	double[] w = getWeights();
	int u = w.length - 1;

	// initial iterations
	int nmax = Math.min(w.length, n);
	double a = w[u];
	for (int i = 0; i < nmax; ++i) {
	    double z = in.get(i);
	    for (int j = 1; j <= i; ++j)
		z -= out.get(i - j) * w[u - j];
	    out.set(i, z / a);
	}
	for (int i = w.length; i < n; ++i) {
	    double z = in.get(i);
	    for (int j = 1; j <= u; ++j)
		z -= out.get(i - j) * w[u - j];
	    out.set(i, z / a);
	}

    }
}
