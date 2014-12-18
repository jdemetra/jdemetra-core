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

package ec.tstoolkit.data;

import ec.tstoolkit.design.Development;

/**
 *
 * @author pcuser
 */
@Development(status = Development.Status.Alpha)
public class DescriptiveStatistics {
    // / <summary>
    // / Default constructor
    // / </summary>

    /**
     * 
     * @param k
     * @param data
     * @return
     */
    public static double[] ac(int k, double[] data) {
        double[] c = new double[k];
        double var = cov(0, data);
        for (int i = 0; i < k; ++i) {
            c[i] = cov(i + 1, data) / var;
        }
        return c;
    }

    /**
     * Computes the covariance between two arrays of doubles, which are supposed to 
     * have zero means; the arrays might contain missing values (Double.NaN); 
     * those values are omitted in the computation 
     * the covariance (and the number of observations are adjusted).
     * @param x The first array
     * @param y The second array
     * @param t The delay between the two arrays 
     * @return The covariance; cov = sum((x(i)*y(i+t)/(n-t))
     */
    public static double cov(double[] x, double[] y, int t) 
    {
        // x and y must have the same Length...
        if (t < 0) {
            return cov(y, x, -t);
        }
        double v = 0;
        int n = x.length - t;
        int nm = 0;
        for (int i = 0; i < n; ++i) {
            double xcur = x[i];
            double ycur = y[i + t];
            if (isFinite(xcur) && isFinite(ycur)) {
                v += xcur * ycur;
            }
            else {
                ++nm;
            }
        }
        int m=x.length-nm;
        if (m == 0) {
            return 0;
        }
        return v / m;
        //return v / x.length;
    }

    // compute the covariance of (x (from sx to sx+n), y(from sy to sy+n)
    /**
     * 
     * @param x
     * @param sx
     * @param y
     * @param sy
     * @param n
     * @return
     */
    public static double cov(double[] x, int sx, double[] y, int sy, int n) {
        double v = 0;
        int nm = 0;
        for (int i = 0; i < n; ++i) {
            double xcur = x[i + sx];
            double ycur = y[i + sy];
            if (isFinite(xcur) && isFinite(ycur)) {
                v += xcur * ycur;
            }
            else {
                ++nm;
            }
        }
        n -= nm;
        if (n == 0) {
            return 0;
        }
        return v / n;
    }

    /**
     * 
     * @param k
     * @param data
     * @return
     */
    public static double cov(int k, double[] data) {
        return cov(data, data, k);
    }

    /**
     * Checks if a double is finite.
     * @param d The tested value
     * @return True if the number is different of Double.NaN and of Double.Positive/NegativeInfinity
     */
    public static boolean isFinite(double d) {
//        return !(Double.isNaN(d) || Double.isInfinite(d));
        return Double.NEGATIVE_INFINITY < d && d < Double.POSITIVE_INFINITY;
    }
    
    public static final double DELTA=3.834e-20;
     
    public static boolean isSmall(double val){
        return Math.abs(val)<DELTA;
   }

    /**
     * 
     * @param ac
     * @return
     */
    public static double[] pac(double[] ac) {
        double[] pc = new double[ac.length];
        return pac(ac, pc);
    }

    /**
     * 
     * @param ac
     * @param coeff
     * @return
     */
    public static double[] pac(double[] ac, double[] coeff) {
        int kmax = ac.length;
        double[] pac = new double[kmax];
        double[] tmp = new double[kmax];

        pac[0] = coeff[0] = ac[0]; // K = 1
        for (int K = 2; K <= kmax; ++K) {
            double n = 0, d = 0;
            for (int k = 1; k <= K - 1; ++k) {
                double x = coeff[k - 1];
                n += ac[K - k - 1] * x;
                d += ac[k - 1] * x;
            }
            pac[K - 1] = coeff[K - 1] = (ac[K - 1] - n) / (1 - d);

            for (int i = 0; i < K; ++i) {
                tmp[i] = coeff[i];
            }
            for (int j = 1; j <= K - 1; ++j) {
                coeff[j - 1] = tmp[j - 1] - tmp[K - 1] * tmp[K - j - 1];
            }
        }
        return pac;
    }

    /**
     * 
     * @param k
     * @param data
     * @return
     */
    public static double[] pac(int k, double[] data) {
        return pac(ac(k, data));
    }

    /**
     * Computes the variance of x
     * @param x Data for which we compute the variance
     * @param sx Starting position in the array
     * @param n Number of data.
     * @return
     */
    public static double var(double[] x, int sx, int n) {
        double v = 0;
        int nm = 0;
        for (int i = 0; i < n; ++i) {
            double xcur = x[i + sx];
            if (isFinite(xcur)) {
                v += xcur * xcur;
            }
            else {
                ++nm;
            }
        }
        n -= nm;
        return v / n;
    }
    private double[] m_data, m_obs, m_sdata;
    private double m_sx, m_sxx = -1;
    private double m_sk, m_kr = -1;
    private int m_nm;

    /**
     *
     */
    public DescriptiveStatistics() {
    }

    // / <summary>
    // / The method essentialy sets the dataset to be analyzed.
    // / </summary>
    // / <param name="ids">An IDataSet interface pointer that represents the
    // data to be analyzed.</param>
    // / <exception cref="System.Exception">Throws an exception when the
    // parameter is null</exception>
    /**
     * 
     * @param ids
     */
    public DescriptiveStatistics(IReadDataBlock ids) {
        m_data = new double[ids.getLength()];
        ids.copyTo(m_data, 0);
        initStats();
    }

    public DescriptiveStatistics(double[] data) {
        m_data = data;
        initStats();
    }
    /**
     * 
     */
    protected void calcMoments() {
        double stdev = getStdev(), avg = getAverage();
        int n = m_data.length;

        // skweness...
        m_sk = 0.0;
        m_kr = 0.0;
        double stdev3 = stdev * stdev * stdev;
        for (int i = 0; i < n; i++) {
            double cur = m_data[i];
            if (isFinite(cur)) {
                double m3 = (cur - avg) * (cur - avg) * (cur - avg);
                m_sk += m3;
                m_kr += m3 * (cur - avg);
            }
        }
        n -= m_nm;
        m_sk /= stdev3 * n;
        m_kr /= stdev3 * stdev * n;
    }

    /**
     * Counts the number of elements between two doubles
     * 
     * @param a
     *            Lower bound (included)
     * @param b
     *            Upper bound (excluded)
     * @return The number of observations in [a,b[
     */
    public int countBetween(double a, double b) {
        if (m_sdata != null) {
            int n = m_sdata.length;
            if (n == 0) {
                return 0;
            }
            int i0 = 0;
            while (i0 < n && m_sdata[i0] < a) {
                ++i0;
            }
            int i1 = i0;
            while (i1 < n && m_sdata[i1] < b) {
                ++i1;
            }
            return i1 - i0;
        }
        else {
            int n = m_data.length;
            int m = 0;
            for (int i = 0; i < n; i++) {
                double v = m_data[i];
                if (isFinite(v) && v >= a && v < b) {
                    m++;
                }
            }
            return m;
        }
    }

    // / <Summary>
    // / A Read Only property representing the average of the data in the
    // dataset.
    // / </Summary>
    /**
     * 
     * @return
     */
    public double getAverage() {
        if (!isInitialized()) {
            initStats();
        }
        int n = m_data.length - m_nm;
        return m_sx / n;
    }

    /**
     * 
     * @return
     */
    public int getDataCount() {
        return m_data.length;
    }

    /**
     * @return The kurtosis of the white noise data
     */
    public double getKurtosis() {
        if (m_kr < 0) {
            calcMoments();
        }
        return m_kr;
    }

    // / <Summary>
    // / A Read Only property representing the maximum value in the dataset.
    // / </Summary>
    /**
     * 
     * @return
     */
    public double getMax() {
        if (m_sdata != null) {
            return m_sdata[m_sdata.length - 1];
        }
        else {
            int n = m_data.length;
            double sent = -Double.MAX_VALUE;
            for (int i = 0; i < n; i++) {
                double v = m_data[i];
                if (isFinite(v) && v > sent) {
                    sent = v;
                }
            }
            return sent;
        }
    }

    // / <Summary>
    // / A Read Only property representing the median of the data in the
    // dataset.
    // / Defined as data[n/2+1] for count is odd and (data[n/2]+data[n/2+1])/2
    // fo even count
    // / </Summary>
    /**
     * 
     * @return
     */
    public double getMedian() {
        sortObservations();
        if (m_sdata.length == 0) {
            return Double.NaN;
        }
        boolean even = (m_sdata.length % 2 == 0 ? true : false);
        if (even) {
            return ((m_sdata[m_sdata.length / 2 - 1] + m_sdata[m_sdata.length / 2]) / 2.0);
        }
        else {
            return m_sdata[m_sdata.length / 2];
        }
    }

    // / <Summary>
    // / A Read Only property representing the minimum value in the dataset.
    // / </Summary>
    /**
     * 
     * @return
     */
    public double getMin() {
        if (m_sdata != null) {
            return m_sdata[0];
        }
        else {
            int n = m_data.length;
            double sent = Double.MAX_VALUE;
            for (int i = 0; i < n; i++) {
                double v = m_data[i];
                if (isFinite(v) && v < sent) {
                    sent = v;
                }
            }
            return sent;
        }
    }

    /**
     * 
     * @return
     */
    public int getMissingValuesCount() {
        if (!isInitialized()) {
            initStats();
        }
        return m_nm;
    }

    /**
     * 
     * @return
     */
    public int getObservationsCount() {
        return m_data.length - getMissingValuesCount();
    }

    /**
     * 
     * @return
     */
    public double getSkewness() {
        if (m_kr < 0) {
            calcMoments();
        }
        return m_sk;
    }

    /**
     * 
     * @return
     */
    public double getStdev() {
        return Math.sqrt(getVar());
    }

    /**
     * 
     * @param df
     * @return
     */
    public double getStdevDF(int df) {
        return Math.sqrt(getVarDF(df));
    }

    // / <Summary>
    // / A Read Only property representing the sum of the values in the dataset.
    // / </Summary>
    /**
     * 
     * @return
     */
    public double getSum() {
        if (!isInitialized()) {
            initStats();
        }
        return m_sx;
    }

    // / <Summary>
    // / A Read Only property representing the sum of the squared values in the
    // dataset.
    // / </Summary>
    /**
     * 
     * @return
     */
    public double getSumSquare() {
        if (!isInitialized()) {
            initStats();
        }
        return m_sxx;
    }

    // / <Summary>
    // / A Read Only property representing the variance in the dataset. (divided
    // by n-1)
    // / </Summary>
    /**
     * 
     * @return
     */
    public double getVar() {
        return getVarDF(0);
    }

    // / <Summary>
    // / A Read Only property representing the variance in the dataset. (divided
    // by n-1)
    // / </Summary>
    /**
     * 
     * @param df
     * @return
     */
    public double getVarDF(int df) {
        if (!isInitialized()) {
            initStats();
        }
        int n = m_data.length - m_nm;
        // Could lead to numerical instabilities
        //return (m_sxx - m_sx * m_sx / n) / (n - df);
        double sxx = 0;
        double m=m_sx/n;
        for (int i = 0; i < m_data.length; i++) {
            double v = m_data[i];
            if (isFinite(v)) {
                double e=v-m;
                 sxx += e*e;
            }
        }
       return sxx/(n-df);
    }

    /**
     * Root mean square error
     * @return
     */
    public double getRmse() {
        if (!isInitialized()) {
            initStats();
        }
        int n = m_data.length - m_nm;
        return Math.sqrt(m_sxx / n);
    }

    /**
     * 
     * @return
     */
    public boolean hasMissingValues() {
        if (!isInitialized()) {
            initStats();
        }
        return m_nm > 0;
    }

    // / <Summary>
    // / A Read Only property that returns true if at least one element in the
    // set is equal to zero
    // / </Summary>
    /**
     * 
     * @return
     */
    public boolean hasNullValues() {
        if (m_sdata != null) {
            if (m_sdata[0] > 0 || m_sdata[m_sdata.length - 1] < 0) {
                return false;
            }
        }
        for (int i = 0; i < m_data.length; i++) {
            double v = m_data[i];
            if (isFinite(v) && v == 0) {
                return true;
            }
        }
        return false;
    }

    private void initStats() {
        m_sxx = 0;
        for (int i = 0; i < m_data.length; i++) {
            double v = m_data[i];
            if (!isFinite(v)) {
                ++m_nm;
            }
            else {
                m_sx += v;
                m_sxx += v * v;
            }
        }
    }

    /**
     * 
     * @return
     */
    public double[] internalStorage() {
        return m_data;
    }

    // / <Summary>
    // / A Read Only property to see if the data in the dataset have a constant
    // value.
    // / </Summary>
    /**
     * 
     * @return
     */
    public boolean isConstant() {
        if (m_sdata != null) {
            if (m_sdata.length == 0) {
                return true;
            }
            return m_sdata[0] == m_sdata[m_sdata.length - 1];
        }
        else {
            double sent = Double.NaN;
            for (int i = 0; i < m_data.length; i++) {
                double v = m_data[i];
                if (isFinite(v)) {
                    if (Double.isNaN(sent)) {
                        sent = v;
                    }
                    else if (v != sent) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    // / <Summary>
    // / A Read Only property that returns true if all data in the set are
    // larger than some value
    // / </Summary>
    /**
     * 
     * @param val
     * @return
     */
    public boolean isGreater(double val) {
        if (m_sdata != null) {
            return m_sdata[0] > val;
        }
        else {
            for (int i = 0; i < m_data.length; i++) {
                double v = m_data[i];
                if (isFinite(v) && v <= val) {
                    return false;
                }
            }
            return true;
        }
    }

    // / <Summary>
    // / A Read Only property that returns true if all data in the set are
    // larger than or equal to some value
    // / </Summary>
    /**
     * 
     * @param val
     * @return
     */
    public boolean isGreaterOrEqual(double val) {
        if (m_sdata != null) {
            return m_sdata[0] > val;
        }
        else {
            for (int i = 0; i < m_data.length; i++) {
                double v = m_data[i];
                if (isFinite(v) && v < val) {
                    return false;
                }
            }
            return true;
        }
    }

    private boolean isInitialized() {
        return m_sxx >= 0;
    }

    // / <Summary>
    // / A Read Only property that returns true if all data in the set are
    // strictly negative
    // / </Summary>
    /**
     * 
     * @return
     */
    public boolean isNegative() {
        return isSmaller(0.0);
    }

    // / <Summary>
    // / A Read Only property that returns true if all data in the set are
    // negative or zero
    // / </Summary>
    /**
     * 
     * @return
     */
    public boolean isNegativeOrNull() {
        return isSmallerOrEqual(0.0);
    }

    // / <Summary>
    // / A Read Only property that returns true if all data in the set are
    // strictly positive
    // / </Summary>
    /**
     * 
     * @return
     */
    public boolean isPositive() {

        return isGreater(0.0);
    }

    // / <Summary>
    // / A Read Only property that returns true if all data in the set are
    // positive or zero
    // / </Summary>
    /**
     * 
     * @return
     */
    public boolean isPositiveOrNull() {
        return isGreaterOrEqual(0.0);
    }

    // / <Summary>
    // / A Read Only property that returns true if all data in the set are
    // smaller than some value
    // / </Summary>
    /**
     * 
     * @param val
     * @return
     */
    public boolean isSmaller(double val) {
        if (m_sdata != null) {
            return m_sdata[0] > val;
        }
        else {
            for (int i = 0; i < m_data.length; i++) {
                double v = m_data[i];
                if (isFinite(v) && v >= val) {
                    return false;
                }
            }
            return true;
        }
    }

    // / <Summary>
    // / A Read Only property that returns true if all data in the set are
    // smaller than or equal to some value
    // / </Summary>
    /**
     * 
     * @param val
     * @return
     */
    public boolean isSmallerOrEqual(double val) {
        if (m_sdata != null) {
            return m_sdata[0] > val;
        }
        else {
            for (int i = 0; i < m_data.length; i++) {
                double v = m_data[i];
                if (isFinite(v) && v > val) {
                    return false;
                }
            }
            return true;
        }
    }

    // / <Summary>
    // / A Read Only property that returns true if all data in the set are
    // larger than some value
    // / </Summary>
    /**
     * 
     * @param eps
     * @return
     */
    public boolean isZero(double eps) {
        if (m_sdata != null) {
            return m_sdata[0] >= -eps && m_sdata[m_sdata.length - 1] <= eps;
        }
        else {
            for (int i = 0; i < m_data.length; i++) {
                double v = m_data[i];
                if (isFinite(v) && Math.abs(v) > eps) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * 
     * @return
     */
    public double[] observations() {
        if (m_obs == null) {
            if (!isInitialized()) {
                initStats();
            }
            if (m_nm == 0) {
                m_obs = m_data;
            }
            else {
                m_obs = new double[m_data.length - m_nm];
                for (int i = 0, j = 0; i < m_data.length; ++i) {
                    double x = m_data[i];
                    if (isFinite(x)) {
                        m_obs[j++] = x;
                    }
                }
            }
        }
        return m_obs;
    }

    // / <summary>
    // / The method splits the dataset into partitions and returns the values on
    // the boundaries
    // / as an array of doubles
    // / </summary>
    // / <param name="partitions">The number of partions</param>
    // / <returns> </returns>
    // / <exception cref="System.Exception">Throws an exception when no data are
    // available or when the number of partions
    // / is not between 2 and Count</exception>
    /**
     * 
     * @param Number of partitions
     * @return
     */
    public double[] quantiles(int partitions) {
        sortObservations();
        int n = m_sdata.length;
        if (n == 0) {
            return null;
        }

        if (partitions < 2 || n / partitions < 1) {
            return null;
        }

        int np = partitions - 1;
        double[] res = new double[np];

        int ns = ((n - 1) / partitions);

        if (ns * partitions == n - 1) // pas d'arrondis
        {
            for (int i = 0; i < np; i++) {
                res[i] = m_sdata[(i + 1) * ns];
            }
        }
        else // quelques complications. On fait la moyenne pondérée entre les
        // deux valeurs les plus proches du point de partition
        {
            double ds = ((double) (n - 1)) / partitions;
            for (int i = 0; i < np; i++) {
                double dindex = (i + 1) * ds;
                int lo = (int) dindex;
                double dlo = dindex - lo;
                res[i] = m_sdata[lo] * (1 - dlo) + m_sdata[lo + 1] * dlo;
            }
        }

        return res;
    }

    // / <summary>
    // / Returns the data as a sorted array of doubles
    // / </summary>
    /**
     * 
     * @return
     */
    public double[] sortedObservations() {
        sortObservations();
        return m_sdata;
    }

    private void sortObservations() {
        if (m_sdata == null) {
            if (!isInitialized()) {
                initStats();
            }
            m_sdata = observations().clone();
            java.util.Arrays.sort(m_sdata);
        }
    }
}
