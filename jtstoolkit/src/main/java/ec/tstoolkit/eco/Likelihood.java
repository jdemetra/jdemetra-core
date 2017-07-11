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
package ec.tstoolkit.eco;

import ec.tstoolkit.design.Development;

/**
 * Log-Likelihood of a multi-variate gaussian distribution.
 * For a N(0, sig2*V) distribution (dim = n), the log-likelihood is given by 
 * -.5*[n*log(2*pi)+log(det(V)*sig2^n)+(1/sig2)*y'(V^-1)y] =
 * If we factorize V as LL' (L is the Cholesky factor of V) and if we write
 * e=L^-1*y, we get
 * ll=-.5*[n*log(2*pi)+log(det(V))+n*log(sig2)+(1/sig2)*e'e]
 * det(V) is then the square of the product of the main diagonal of L.
 * 
 * We consider that sig2 is concentrated out of the likelihood and that it 
 * is given by its max-likelihood estimator:
 * sig2=e'e/n where e'e= y*(V^-1)y.
 * 
 * So, we get:
 * ll=-.5[n*log(2*pi)+n*(log(ssq/n)+1)+ldet]
 * 
 * The likelihood is initialized by means of
 * - its dimension: n
 * - the log of the determinantal term: ldet
 * - the sum of the squares: ssq
 */
@Development(status = Development.Status.Release)
public class Likelihood implements ILikelihood {

    private double m_ll, m_ssqerr, m_ldet;

    private int m_n;

    private double[] m_res;

    /**
     * 
     */
    public Likelihood() {
    }

    /**
     * Aikake Information Criterion for a given number of (hyper-)parameters 
     * AIC=2*nparams-2*ll
     * @param nparams The number of parameters
     * @return The AIC. Models with lower AIC shoud be preferred.
     */
    public double AIC(final int nparams) {
	return -2 * m_ll + 2 * nparams;
    }

    /**
     * 
     * @param nparams
     * @return
     */
    public double BIC(final int nparams) {
	return -2 * m_ll + nparams * Math.log(m_n);
    }

    /**
         *
         */
    public void clear() {
	m_ll = 0;
	m_ssqerr = 0;
	m_ldet = 0;
	m_n = 0;
    }
    
    @Override
    public double getLogDeterminant(){
        return m_ldet;
    }

    /**
     * Computes the factor of the likelihood.
     * The log-likelihood is:
     * ll=-.5[n*log(2*pi)+n*(log(ssq/n)+1)+ldet]
     * =-.5[n*log(2*pi)+n+n*(log(ssq/n)+ldet/n)]
     * So, for a given n, maximizing the likelihood is equivalent to minimizing
     * sigma*factor 
     * where:
     * sigma=ssq/n
     * factor=exp(ldet/n)=exp(log(det(V)^1/n)=(det(L)^1/n)^2
     * 
     * So, the factor is the square of the geometric mean of the main diagonal
     * of the Cholesky factor.
     * @return The factor of the likelihood.
     */
    @Override
    public double getFactor() {
	return Math.exp(m_ldet / m_n);
    }

    /**
     * 
     * @return 
     */
    @Override
    public double getLogLikelihood() {
	return m_ll;
    }

    /**
     * 
     * @return 
     */
    @Override
    public int getN() {
	return m_n;
    }

    @Override
    public double[] getResiduals() {
	return m_res;
    }

    /**
     * Gets the ML estimate of the standard error of the model.
     * ser=sqrt(ssq/n)
     * @return A positive number.
     */
    public double getSer()
    {
	return Math.sqrt(m_ssqerr / m_n);
    }

    /**
     * Gets the ML estimate of the variance of the model.
     * sigma=ssq/n
     * @return A positive number.
     */
    @Override
    public double getSigma() {
	return m_ssqerr / m_n;
    }

    /**
     * Gets the sum of the squares of the (transformed) observations.
     * @return A positive number.
     */
    @Override
    public double getSsqErr() {
	return m_ssqerr;
    }

    /**
     * Adjust the likelihood if the data have been pre-multiplied by a given
     * scaling factor
     * @param factor The scaling factor
     */
    public void rescale(final double factor) {
	if (factor == 1)
	    return;
	m_ssqerr /= factor * factor;
	m_ll += m_n * Math.log(factor);
	if (m_res != null)
	    for (int i = 0; i < m_res.length; ++i)
		m_res[i] /= factor;
    }

    /**
     * Initializes the likelihood/
     * See the description of the class for further information.
     * @param ssqerr The sum of the squares of the (transformed) observations.
     * @param ldet The log of the determinantal term
     * @param ndim The number of observations
     */
    public void set(final double ssqerr, final double ldet, final int ndim) {
	m_ll = -.5
		* (ndim * Math.log(2 * Math.PI) + ndim
			* (1 + Math.log(ssqerr / ndim)) + ldet);
	m_ssqerr = ssqerr;
	m_ldet = ldet;
	m_n = ndim;
	m_res = null;
    }

    /**
     * Sets the elements used for computing ssq. Optional initialization. 
     * @param e The "(transformed) observations (or residuals)" 
     * (y pre-multiplied by the inverse of the Cholesky factor). 
     * See the description of the class for further information.
     */
    public void setRes(final double[] e) {
	if (e != null)
	    m_res = e.clone();
	else
	    m_res = null;
    }
    
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append("ll=").append(this.getLogLikelihood()).append(System.lineSeparator());
        builder.append("n=").append(this.getN()).append(System.lineSeparator());
        builder.append("ssq=").append(this.getSsqErr()).append(System.lineSeparator());
        builder.append("ldet=").append(this.getLogDeterminant()).append(System.lineSeparator());
        return builder.toString();
    }

}
