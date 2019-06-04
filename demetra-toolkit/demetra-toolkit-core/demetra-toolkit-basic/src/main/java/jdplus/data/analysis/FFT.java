/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.data.analysis;

import demetra.design.Development;
import demetra.maths.Complex;
import demetra.maths.Constants;
import demetra.util.Arrays2;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.experimental.UtilityClass
public class FFT {

    private final double[] COS_ARRAY;
    private final double[] SIN_ARRAY;

    static {
	COS_ARRAY = new double[16];
	SIN_ARRAY = new double[16];
	double twopi = Constants.TWOPI;
	double n = 2;
	for (int i = 0; i < 16; ++i) {
	    COS_ARRAY[i] = Math.cos(twopi / n);
	    SIN_ARRAY[i] = Math.sin(twopi / n);
	    n *= 2;
	}
    }
    
    /**
     * Expand an array so that its length is a power of two.
     * Padding with 0.
     * @param data
     * @return The input array or a new array
     */
    public Complex[] expand(final Complex[] data){
        int n=1; 
        while (n<data.length)
            n=n<<1;
        if (n == data.length)
            return data;
        Complex[] ndata=new Complex[n];
        System.arraycopy(data, 0, ndata, 0, data.length);
        for (int i=data.length; i<n; ++i)
            ndata[i]=Complex.ZERO;
        return ndata;
    }

    /*
     * Expand an array so that its length is a power of two.
     * Padding with 0.
     * @param data
     * @return The input array or a new array
     */
    public double[] expand(final double[] data){
        int n=1; 
        while (n<data.length)
            n=n<<1;
        if (n == data.length)
            return data;
        double[] ndata=new double[n];
        System.arraycopy(data, 0, ndata, 0, data.length);
        return ndata;
    }
    /**
     * Back transformation.
     * The length of the array should be a power of 2 (not checked)
     * 
     * @param data
     */
    public void backTransform(final Complex[] data) {
	transform(data, true);
    }

    /**
     * Transformation.
     * The length of the array should be a power of 2 (not checked)
     * 
     * @param data
     */
    public void transform(final Complex[] data)
    {
	transform(data, false);
    }

    /**
     * Transformation.
     * The length of the array should be a power of 2 (not checked)
     * 
     * @param rdata Real part
     * @param idata Imaginary part
     * 
     */
    public void transform(final double[] rdata, final double[] idata)
    {
	transform(rdata, idata, false);
    }

    /**
     * Back transformation.
     * The length of the array should be a power of 2 (not checked)
     * @param rdata Real part. 
     * @param idata Imaginary part
     * 
     */
    public void backTransform(final double[] rdata, final double[] idata)
    {
	transform(rdata, idata, true);
    }

    private void transform(final Complex[] data, final boolean back) {
	final int n = data.length;
	for (int i = 0, j = 0; i < n; ++i) {
	    if (j > i)
		Arrays2.swap(data, i, j);
	    int q = n >> 1;
	    while (q >= 1 && j >= q) {
		j -= q;
		q >>= 1;
	    }
	    j += q;
	}
	// Danielson-Lanzcos routine
	int m = 1, s = 0;
	// external loop
	while (m < n) {
	    int tm = m << 1;
	    Complex wm = Complex.cart(COS_ARRAY[s], back ? -SIN_ARRAY[s]
		    : SIN_ARRAY[s]);
	    Complex w = Complex.ONE;
	    // internal loops
	    for (int j = 0; j < m; ++j) {
		for (int k = j; k < n; k += tm) {
		    int l = k + m;
		    Complex t = w.times(data[l]);
		    Complex u = data[k];
		    data[k] = u.plus(t);
		    data[l] = u.minus(t);
		}
		w = w.times(wm);
	    }
	    m = tm;
	    ++s;
	}
	if (back) {
	    final double v = 1.0 / n;
	    for (int i = 0; i < n; ++i)
		data[i] = data[i].times(v);
	}
    }

    private void transform(final double[] rdata, final double[] idata, final boolean back) {
	final int n = rdata.length;
	for (int i = 0, j = 0; i < n; ++i) {
	    if (j > i){
		Arrays2.swap(rdata, i, j);
		Arrays2.swap(idata, i, j);
            }
	    int q = n >> 1;
	    while (q >= 1 && j >= q) {
		j -= q;
		q >>= 1;
	    }
	    j += q;
	}
	// Danielson-Lanzcos routine
	int m = 1, s = 0;
	// external loop
	while (m < n) {
	    int tm = m << 1;
            double rwm=COS_ARRAY[s], iwm=back ? -SIN_ARRAY[s]: SIN_ARRAY[s];
            double rw=1, iw=0;
	    // internal loops
	    for (int j = 0; j < m; ++j) {
		for (int k = j; k < n; k += tm) {
		    int l = k + m;
                    // t
                    double rt=rw*rdata[l]-iw*idata[l];
                    double it=rw*idata[l]+iw*rdata[l];
                    double ru=rdata[k], iu=idata[k];
		    rdata[k] = ru+rt;
		    idata[k] = iu+it;
		    rdata[l] = ru-rt;
		    idata[l] = iu-it;
		}
                double r=rw,i=iw;
                rw=r*rwm-i*iwm;
                iw=r*iwm+i*rwm;
	    }
	    m = tm;
	    ++s;
	}
	if (back) {
	    final double v = 1.0 / n;
	    for (int i = 0; i < n; ++i){
		rdata[i] *= v;
		idata[i] *= v;
            }
	}
    }
}