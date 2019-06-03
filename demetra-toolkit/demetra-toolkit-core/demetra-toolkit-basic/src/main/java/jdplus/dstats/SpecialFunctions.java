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
package jdplus.dstats;

import demetra.design.Development;
import jdplus.dstats.DStatException;

/**
 * Defines several special Statistical and Mathematical functions
 * @author Frank Osaer
 */
@Development(status = Development.Status.Release)
public class SpecialFunctions {
    private static final double[] c_gammaconst = { 76.18009172947146,
	    -86.50532032941677, 24.01409824083091, -1.231739572450155,
	    0.1208650973866179e-2, -0.5395239384953e-5 };

    private static final double c_sqrtpi = 2.5066282746310005;
    private static final double c_eps = 1.0e-16;
    private static final double c_sqrt2 = Math.sqrt(2.0);
    private static final double c_log2 = Math.log(2.0);
    private static int m_maxiter = 1000;

    /**
     * Implements the Beta function. Based on code in Press et all. (2002).
     * Numerical Recipes in C++ (2nd Edition). Beta(x,y) =
     * (Gamma(x)*Gamma(y))/Gamma(x+y)
     * 
     * @param x
     * @param y
     * @return B(x,y)
     */
    public static double beta(final double x, final double y) {
	return (gamma(x) * gamma(y)) / gamma(x + y);
    }

    /**
     * Returns the density for the Beta function.
     * 
     * @param x
     *            The value for which Gamma is to be computed
     * @param a
     *            The shape parameter
     * @param b
     *            The scale parameter
     * @return
     */
    public static double Beta_Density(final double x, final double a,
	    final double b) {
	double bb = logGamma(a + b) - logGamma(a) - logGamma(b);
	return Math.exp(bb + (a - 1.0) * Math.log(x) + (b - 1.0)
		* Math.log(1.0 - x));
    }

    /**
     * Computes the value of the Binomial density distribution for a given
     * number of events. Uses the definition of the Binomial density
     * distribution.
     * 
     * @param n
     *            The sample size
     * @param k
     *            The number of events
     * @param p
     *            The probability of the event occuring
     * @return
     */
    public static double binomialDensity(final double n, final double k,
	    final double p) {
	return Math.exp(logGamma(n + 1) - logGamma(k + 1) - logGamma(n - k + 1)
		+ k * Math.log(p) + (n - k) * Math.log(1.0 - p));
    }

    /**
     * Returns the probability that the ChiSquare value for a correct model is
     * less than chi. Based on code in Press et all. (2002). Numerical Recipes
     * in C++ (2nd Edition).
     * 
     * @param chi
     *            The observed ChiSquare value
     * @param v
     *            The degrees of freedom of the ChiSquare distribution
     * @return
     */
    public static double chiSquare(final double chi, final double v) {
	if (chi < 0)
	    throw new DStatException("chi value must be strictly positive",
		    "SpecialFunctions.ChiSquare");
	return incGamma((v) / 2.0, chi / 2.0);
    }

    /**
     * Returns the probability that the ChiSquare value for a correct model is
     * higher than chi. Based on code in Press et all. (2002). Numerical Recipes
     * in C++ (2nd Edition).
     * 
     * @param chi
     *            The observed ChiSquare value
     * @param v
     *            The degrees of freedom of the ChiSquare distribution
     * @return
     */
    public static double chiSquareComplement(final double chi, final double v) {
	if (chi < 0)
	    throw new DStatException("chi value must be strictly positive",
		    "SpecialFunctions.ChiSquareComplement");
	return CIncGamma((v) / 2.0, chi / 2.0);
    }

    /**
     * Returns the ChiSquare density value. The value is computed using the
     * density function definition.
     * 
     * @param x
     *            The value for which the density must be computed
     * @param v
     *            The degrees of freedom of the ChiSquare distribution
     * @return
     */
    public static double chiSquareDensity(final double x, final double v) {
	double gl = logGamma(v / 2.0);
	return Math.exp((v - 2.0) / 2 * Math.log(x) - (x / 2.0) - (v / 2.0)
		* c_log2 - gl);
    }

    /**
     * CIncGamma computes the complement of the incomplete Gamma function. The
     * value rises from unity to near zero in a range of x centered on a-1.
     * Based on code in Press et all. (2002). Numerical Recipes in C++ (2nd
     * Edition). Uses a series development for x smaller than a+1 and a
     * continued fraction computation otherwise.
     * 
     * @param a
     *            The center of the function
     * @param x
     *            The range of the function
     * @return
     */
    public static double CIncGamma(final double a, final double x) {
	if (x < a + 1)
	    return 1.0 - IGSeries(a, x);
	else
	    return IGContFract(a, x);
    }

    /**
     * Returns the probability that k or more events occur in a sample of n.
     * This is actually the upper tail of the distribution. Based on code in
     * Press et all. (2002). Numerical Recipes in C++ (2nd Edition).
     * 
     * @param n
     *            The sample size (n >= 0)
     * @param k
     *            The number of events
     * @param p
     *            The probability of the event occuring
     * @return
     */
    public static double cumBinomial(final double n, final double k,
	    final double p) {
	if (k == 0.0)
	    return 1.0;

	return incBeta(k, n - k + 1.0, p);
    }

    /**
     * Return the probability that for a given lambda the number of Poisson
     * random events will be between 0 and k-1 inclusive. Based on code in Press
     * et all. (2002). Numerical Recipes in C++ (2nd Edition).
     * 
     * @param x
     *            The number of Poisson events
     * @param lambda
     *            The Lambda parameter of the Poisson distribution
     * @return A double between 0 and 1
     */
    public static double cumulativePoisson(final double x, final double lambda) {
	if (x < 0.0 || lambda < 1)
	    throw new DStatException(
		    "x must be positive and lambda must be >= 1",
		    "SpecialFunctions.CumulativePoisson");
	return CIncGamma(lambda, x);
    }

    /**
     * ErrorF computes the error function. ErrorF(0) = 0; ErrorF(inf) = 1;
     * ErrorF(-x) = -ErrorF(x) The value rises from near-zero to near unity in a
     * range of x centered on a-1. Based on code in Press et all. (2002).
     * Numerical Recipes in C++ (2nd Edition). Uses a series development for x
     * smaller than a+1 and a continued fraction computation otherwise.
     * 
     * @param x
     *            The value
     * @return
     */
    public static double errorF(final double x) {
	if (x < 0.0)
	    return (-1.0 * incGamma(0.5, x * x));
	else
	    return incGamma(0.5, x * x);
    }

    /**
     * Computes the density at val for the F-probability density function. Uses
     * the definition of the density function.
     * 
     * @param val
     *            The value for which the value is to be computed
     * @param k1
     *            Degrees of freedom of the first sample
     * @param k2
     *            Degrees of freedom of the second sample
     * @return
     */
    public static double FDensity(final double val, final double k1,
	    final double k2) {
	return Math.exp(logGamma((k1 + k2) / 2.0) - logGamma(k1 / 2.0)
		- logGamma(k2 / 2.0) + (k1 / 2.0) * Math.log(k1 / k2)
		+ ((k1 - 2.0) / 2.0) * Math.log(val) - ((k1 + k2) / 2.0)
		* Math.log(1.0 + (k1 / k2) * val));
    }

    /**
     * Calculates the probability that the value F - the ratio of the variance
     * of two samples - takes the given value when the variance of the first
     * sample is actually smaller than the variance of the second sample. Based
     * on code in Press et all. (2002). Numerical Recipes in C++ (2nd Edition).
     * 
     * @param val
     *            The opbserved value
     * @param df1
     *            The degrees of freedom of the first sample
     * @param df2
     *            The degrees of freedom of the second sample
     * @return A value between 0 and 1
     */
    public static double FProbability(final double val, final double df1,
	    final double df2) {
	return incBeta(df2 / 2.0, df1 / 2.0, df2 / (df2 + df1 * val));
    }

    /**
     * Implements the Gamma function. Based on code in Press et all. (2002).
     * Numerical Recipes in C++ (2nd Edition).
     * 
     * @param x
     *            The input parameter
     * @return double; the calculated gamma value
     */
    public static double gamma(final double x) {
	double e1 = x + 5.5;
	double ep = Math.pow(e1, x + 0.5);
	double t = Math.exp(-e1);

	double series = 1.000000000190015;
	for (int i = 0; i < 6; i++)
	    series += (c_gammaconst[i] / (x + (i + 1)));

	series *= c_sqrtpi;
	series *= ep;
	series *= t;

	return (series / x);
    }

    /**
     * Returns the density for the Gamma function. Defined as Gamma(x,a,b) =
     * (((x**(a-1))*e**(-(x/b))) / ((b**a)*G(a))
     * 
     * @param x
     *            The value for which Gamma is to be computed
     * @param a
     *            The shape parameter
     * @param b
     *            The scale parameter
     * @return
     */
    public static double gammaDensity(final double x, final double a,
	    final double b) {
	if (x < 0.0)
	    return 0;

	return Math.exp((a - 1.0) * Math.log(x) - (x / b) - a * Math.log(b)
		- logGamma(a));
    }

    /**
     * Gets the maximum number of iterations for the functions
     * 
     * @return
     */
    public static int getMaxIter() {
	return m_maxiter;
    }

    /**
     * Computes the density for the hypergeometric probability function.
     * 
     * @param n
     *            The population size
     * @param m
     *            The number of marked items in the population
     * @param l
     *            The sample size
     * @param k
     *            The number of marked items in the sample
     * @return
     */
    public static double hyperGeometricDensity(final double n, final double m,
	    final double l, final double k) {
	if (!((0.0 <= l) && (l <= n)))
	    throw new DStatException(
		    "Population and sample size must be positive and sample size cannot be larger than population size",
		    "SpecialFunctions.HyperGeometricDensity");
	if (!((Math.max(0.0, l - (n - m)) <= k) && (k <= Math.min(l, m))))
	    throw new DStatException(
		    "k must satisfy max(0, l-(n-m)) <= k <= min(l, m)",
		    "SpecialFunctions.HyperGeometricDensity");
	return Math.exp(logGamma(m + 1) + logGamma(n - m + 1) + logGamma(l + 1)
		+ logGamma(n - l + 1) - logGamma(k + 1) - logGamma(m - k + 1)
		- logGamma(l - k + 1) - logGamma(n - m - l + k + 1)
		- logGamma(n + 1));
    }

    /**
     * Returns the probability that k or less events occur in a sample of n.
     * This is actually the lower tail of the distribution. The value is found
     * as follows Hyp(x; l,m,N) = Hyp(x-1;l,m,N)*((m-k+1)*(n-k+1))/(k*(N-m-n+k))
     * 
     * @param n
     *            The population size (n >= 0)
     * @param m
     *            The number of marked items in the population
     * @param l
     *            The sample size
     * @param k
     *            The number of marked items in the sample
     * @return
     */
    public static double hyperGeometricProbability(final double n,
	    final double m, final double l, final double k) {
	if (!((0.0 <= l) && (l <= n)))
	    throw new DStatException(
		    "Population and sample size must be positive and sample size cannot be larger than population size",
		    "SpecialFunctions.HyperGeometricProbability");

	if (!((Math.max(0.0, l - (n - m)) <= k) && (k <= Math.min(l, m))))
	    throw new DStatException(
		    "k must satisfy max(0, l-(n-m)) <= k <= min(l, m)",
		    "SpecialFunctions.HyperGeometricProbability");
	double lm = Math.min(l, m);
	if (k == lm)
	    return 1.0;

	if (k < (lm / 2)) {
	    double hout = hyperGeometricDensity(n, m, l, 0);
	    for (int i = 1; i <= k; i++)
		hout += hout * ((m - k + 1) * (l - k + 1))
			/ (k * (n - m - l + k));
	    return hout;
	} else {
	    double hout = hyperGeometricDensity(n, m, l, lm);
	    for (int i = (int) lm - 1; i > (int) k; i--)
		hout += hout / ((m - k + 1) * (l - k + 1))
			/ (k * (n - m - l + k));
	    return 1.0 - hout;
	}
    }

    static double IGContFract(final double a, double x) {
	if (a <= 0.0 || x < 0.0)
	    throw new DStatException(
		    "Wrong parameter values: use a > 0 and x >= 0",
		    "SpecialFunctions.IGContFract");

	double FPMIN = 1.0e-65;
	double b = x + 1.0 - a;
	double c = 1.0 / FPMIN;
	double d = 1.0 / b;
	double h = d;

	boolean stopped = false;
	for (int i = 1; i < m_maxiter; i++) {
	    double an = -i * (i - a);
	    b += 2.0;
	    d = an * d + b;
	    if (Math.abs(d) < FPMIN)
		d = FPMIN;
	    c = b + an / c;
	    if (Math.abs(c) < FPMIN)
		c = FPMIN;
	    d = 1.0 / d;
	    double del = d * c;
	    h *= del;
	    if (Math.abs(del - 1.0) <= c_eps) {
		stopped = true;
		break;
	    }
	}

	if (!stopped)
	    throw new DStatException(DStatException.ERR_ITER,
		    "SpecialFunctions.IGContFract");

	double ga = logGamma(a);
	double dout = (Math.exp(-x + a * Math.log(x) - ga)) * h;
	return dout;
    }

    static double IGSeries(final double a, double x) {
	if (a <= 0.0 || x < 0.0)
	    throw new DStatException(
		    "Wrong parameter values: use a > 0 and x >= 0",
		    "SpecialFunctions.IGSeries");

	double div = 1.0 / a;
	double sum = div;
	double aa = a;
	// first iteration is done
	for (int i = 0; i < m_maxiter; i++) {
	    ++aa;
	    div *= x / aa;
	    sum += div;
	    if (Math.abs(div) < Math.abs(sum) * c_eps) {
		double gser = sum
			* Math.exp(-x + a * Math.log(x) - logGamma(a));
		return gser;
	    }
	}

	throw new DStatException(DStatException.ERR_ITER,
		"SpecialFunctions.IGSeries");
    }

    /**
     * The incomplete Beta functions. Based on code in Press et all. (2002).
     * Numerical Recipes in C++ (2nd Edition).
     * 
     * @param a
     *            The shape of the function
     * @param b
     *            The scale of the function
     * @param x
     *            The value to be computed
     * @return
     */
    public static double incBeta(final double a, final double b, final double x) {
	if (a < 0.0 || b < 0.0)
	    throw new DStatException(
		    "Parameters a and b must be strictly positive",
		    "SpecialFunctions.IncBeta");
	if (x < 0.0 || x > 1.0)
	    throw new DStatException(
		    "Parameter x must be between 0 and 1 inclusive",
		    "SpecialFunctions.IncBeta");

	double corr = 0.0;
	if (x == 0.0 || x == 1.0)
	    corr = 0.0;
	else
	    corr = Math.exp(logGamma(a + b) - logGamma(a) - logGamma(b) + a
		    * Math.log(x) + b * Math.log(1.0 - x));

	if (x < (a + 1.0) / (a + b + 2.0))
	    return corr * incBetaCf(a, b, x) / a;
	else
	    return 1.0 - corr * incBetaCf(b, a, 1.0 - x) / b;
    }

    static double incBetaCf(final double a, final double b, final double x) {
	double FPMIN = 1.0e-65;
	double am1 = a - 1.0;
	double ap1 = a + 1.0;
	double apb = a + b;

	double c = 1.0;
	double d = 1.0 - (apb * x) / ap1;
	if (Math.abs(d) < FPMIN)
	    d = FPMIN;
	d = 1.0 / d;
	double h = d;

	boolean stopped = false;

	for (int i = 1; i < m_maxiter; i++) {
	    double m2 = i * 2;

	    // even terms in cf
	    double aa = (i * (b - i) * x) / ((am1 + m2) * (a + m2));
	    d = 1.0 + aa * d;
	    if (Math.abs(d) < FPMIN)
		d = FPMIN;
	    c = 1.0 + aa / c;
	    if (Math.abs(c) < FPMIN)
		c = FPMIN;
	    d = 1.0 / d;
	    h *= c * d;

	    // odd terms
	    aa = -(a + i) * (apb + i) * x / ((a + m2) * (ap1 + m2));
	    d = 1.0 + aa * d;
	    if (Math.abs(d) < FPMIN)
		d = FPMIN;
	    c = 1.0 + aa / c;
	    if (Math.abs(c) < FPMIN)
		c = FPMIN;
	    d = 1.0 / d;
	    double del = c * d;
	    h *= del;
	    if (Math.abs(del - 1.0) < c_eps) {
		stopped = true;
		break;
	    }
	}

	if (!stopped)
	    throw new DStatException(DStatException.ERR_ITER,
		    "SpecialFunctions.IncBetaCf");

	return h;
    }

    /**
     * IncGamma computes the incomplete Gamma function. The value rises from
     * near-zero to near unity in a range of x centered on a-1. Based on code in
     * Press et all (2002). Numerical Recipes in C++ (2nd Edition). Uses a
     * series development for x smaller than a+1 and a continued fraction
     * computation otherwise.
     * 
     * @param a
     *            The center of the function
     * @param x
     *            The range of the function
     * @return
     */
    public static double incGamma(final double a, final double x) {
	if (x < a + 1.0)
	    return IGSeries(a, x);
	else
	    return 1.0 - IGContFract(a, x);
    }

    /**
     * Implements the LogGamma function. Based on code in Press et all. (2002).
     * Numerical Recipes in C++ (2nd Edition).
     * 
     * @param x
     *            The input parameter must be strictly positive
     * @return double; the calculated loggamma value.
     */
    public static double logGamma(final double x) {
	if (x <= 0.0)
	    throw new DStatException(
		    "Invalid argument to LogGamma - must be > 0.0",
		    "SpecialFunctions.LogGamma");

	double arg = x + 5.5;
	double t = (x + 0.5) * Math.log(arg) - arg;

	double series = 1.000000000190015;
	for (int i = 0; i < 6; i++)
	    series += (c_gammaconst[i] / (x + (i + 1)));

	series = Math.log(series);
	series += Math.log(c_sqrtpi);

	t += series;
	return (t - Math.log(x));
    }

    /**
     * Returns the LogNormal density for an x given the log of the median t50
     * and the standard deviation s
     * 
     * @param x
     *            The argument x (must be larger than zero)
     * @param t50
     *            The median of the distribution (its logarithm)
     * @param s
     *            The standard deviation of the distribution
     * @return
     */
    public static double logNormalDensity(final double x, final double t50,
	    final double s) {
	if (x <= 0.0)
	    throw new DStatException("Argument must be larger than 0.0",
		    "SpecialFunctions.LogNormalDensity");

	double larg = Math.log(x);
	double exp = (larg - t50) * (larg - t50) * (-1.0 / (2.0 * s * s));
	double res = Math.exp(exp - Math.log(c_sqrtpi * s * x));
	return res;
    }

    /**
     * NormalC computes the probability of x for the cumulative standard normal
     * distribution (�=0 and s^2 = 1). Based on code in Press et all. (2002).
     * Numerical Recipes in C++ (2nd Edition).
     * 
     * @param x
     *            The argument (between -inf and inf)
     * @return A value between 0 and 1
     */
    public static double normalC(double x) {
	return (0.5 * (1.0 - errorF(-x / c_sqrt2)));
    }

    /**
     * Computes the density for the normal probability distribution. Uses the
     * definition of the normal density function
     * 
     * @param x
     *            The value for which the density is to be computed
     * @param m
     *            The mean of the distribution
     * @param s
     *            The standard deviation of the distribution
     * @return The density at x given m and s
     */
    public static double normalDensity(final double x, final double m,
	    final double s) {
	double arg = Math.pow((x - m) / s, 2.0) * -0.5;
	return Math.exp(arg - Math.log(s) - Math.log(c_sqrtpi));
    }

    /**
     * Calculates the probability of observing x for a Poisson distribution with
     * lambda = mean. The computation uses the definition for the Poisson
     * probability distribution.
     * 
     * @param x
     *            A double representing the value observed
     * @param lambda
     *            The Lambda parameter of the Poisson distribution
     * @return Thrown when x+1 is not strictly positive
     */
    public static double poissonProbability(final double x, final double lambda) {
	return Math.exp(x * Math.log(lambda) - lambda - logGamma(x + 1.0));
    }

    /**
     * Sets the maximum number of iterations for the functions
     * 
     * @param maxIter
     */
    public static void setMaxIter(final int maxIter) {
	m_maxiter = maxIter;
    }

    /**
     * Calculates the value of the T or Student probability distribution
     * function for a given value. Uses the definition of the distribution.
     * 
     * @param val
     *            The value for which the student value is to be computed
     * @param df
     *            The degrees of freedom of the distribution
     * @return
     */
    public static double studentDensity(final double val, final double df) {
	if (df <= 0.0)
	    throw new DStatException(
		    "The T-probability distribution requires df > 0",
		    "SpecialFunctions.StudentDensity");

	return Math.exp(logGamma((df + 1.0) / 2.0) - ((df + 1.0) / 2.0)
		* Math.log(1.0 + (val * val) / df)
		- Math.log(Math.sqrt(Math.PI * df)) - logGamma(df / 2));
    }

    /**
     * Returns the two tail probability that val (the difference of two means)
     * is smaller than the observed value if both means are actually the same.
     * Based on code in Press et all. (2002). Numerical Recipes in C++ (2nd
     * Edition).
     * 
     * @param val
     *            The T-value
     * @param df
     *            The degrees of freedom
     * @return A value between 0 and 1
     */
    public static double studentProbability(final double val, final double df) {
	double bb = incBeta(df / 2.0, 0.5, df / (df + val * val));
	double tm = (1.0 - bb) / 2.0;
	if (val < 0.0)
	    return 0.5 - tm;
	else
	    return 0.5 + tm;
    }

    /**
     * Default constructor
     */
    public SpecialFunctions() {
    }

}
