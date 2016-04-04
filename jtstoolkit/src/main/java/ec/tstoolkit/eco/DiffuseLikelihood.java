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

/**
 * 
 * @author Jean Palate
 */
public class DiffuseLikelihood implements ILikelihood {
    private double m_ll, m_ssqerr, m_ldet, m_lddet;

    private int m_n, m_d;

    private double[] m_res;

    /**
	 *
	 */
    public DiffuseLikelihood() {
    }

    /**
     * 
     * @param nparams
     * @return
     */
    public double AIC(final int nparams) {
	return -2 * getUncorrectedLogLikelihood() + 2 * nparams;
    }

    /**
     * 
     * @param nparams
     * @return
     */
    public double BIC(final int nparams) {
	return -2 * getUncorrectedLogLikelihood() + nparams * Math.log(m_n - m_d);
    }

    /**
         *
         */
    public void clear() {
	m_ll = 0;
	m_ssqerr = 0;
	m_ldet = 0;
	m_lddet = 0;
	m_n = 0;
	m_d = 0;
    }

    /**
     * 
     * @return
     */
    public int getD() {
	return m_d;
    }

    @Override
    public double getFactor() {
	return Math.exp((m_ldet + m_lddet) / (m_n - m_d));
    }

    @Override
    public double getLogLikelihood() {
	return m_ll;
    }

    @Override
    public int getN() {
	return m_n;
    }

    @Override
    public double[] getResiduals() {
	return m_res;
    }
    
    @Override
    public double getLogDeterminant(){
        return m_ldet;
    }

    public double getDiffuseLogDeterminant(){
        return m_lddet;
    }

    public double getUncorrectedLogLikelihood() {
	return m_ll-getDiffuseCorrection();
    }
    /**
     * 
     * @return
     */
    public double getSer() {
	return Math.sqrt(m_ssqerr / (m_n - m_d));
    }

    @Override
    public double getSigma() {
	return m_ssqerr / (m_n - m_d);
    }

    @Override
    public double getSsqErr() {
	return m_ssqerr;
    }

    /**
     * Adjust the likelihood if the data have been pre-multiplied by a given
     * scaling factor
     * 
     * @param factor
     *            The scaling factor
     */
    public void rescale(final double factor) {
	if (factor == 1)
	    return;
	m_ssqerr /= factor * factor;
	m_ll += (m_n - m_d) * Math.log(factor);
	if (m_res != null)
	    for (int i = 0; i < m_res.length; ++i)
		m_res[i] /= factor;
    }
    
    public double getDiffuseCorrection(){
        return -.5*(m_lddet+m_d * Math.log(2 * Math.PI));
    }

    /**
     * 
     * @param ssqerr
     * @param ldet
     * @param lddet
     * @param n
     * @param d
     */
    public void set(final double ssqerr, final double ldet, final double lddet,
	    final int n, final int d) {
	m_ll = -.5
		* (n * Math.log(2 * Math.PI) + (n - d)
			* (1 + Math.log(ssqerr / (n - d))) + ldet + lddet);
	m_ssqerr = ssqerr;
	m_ldet = ldet;
	m_lddet = lddet;
	m_n = n;
	m_d = d;
	m_res = null;
    }

    /**
     * 
     * @param res
     */
    public void setRes(final double[] res) {
	if (res != null)
	    m_res = res.clone();
	else
	    m_res = null;
    }

}
