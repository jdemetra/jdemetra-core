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
package ec.tstoolkit.arima;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.linearfilters.IRationalFilter;
import ec.tstoolkit.maths.linearfilters.RationalBackFilter;
import ec.tstoolkit.maths.linearfilters.SymmetricFilter;
import ec.tstoolkit.maths.matrices.Matrix;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public abstract class AbstractArimaModel extends AbstractLinearModel {

    /**
     *
     */
    /**
     *
     */
    protected RationalBackFilter m_pi, m_psi;

    /**
     *
     */
    protected AbstractArimaModel() {
    }

    /**
     * 
     * @param m
     */
    protected AbstractArimaModel(final AbstractArimaModel m) {
        super(m);
        m_pi = m.m_pi;
        m_psi = m.m_psi;
    }

    @Override
    protected void clearCachedObjects() {
        super.clearCachedObjects();
        m_pi = null;
        m_psi = null;
    }

    /**
     * 
     * @return
     */
    public abstract BackFilter getAR();

    /**
     * 
     * @return
     * @throws ArimaException
     */
    @Override
    public IRationalFilter getFilter() throws ArimaException {
        return getPsiWeights();
    }

    /**
     * 
     * @return
     * @throws ArimaException
     */
    public abstract BackFilter getMA() throws ArimaException;

    /**
     * 
     * @return
     * @throws ArimaException
     */
    public RationalBackFilter getPiWeights() throws ArimaException {
        if (m_pi == null) {
            m_pi = initPi();
        }
        return m_pi;
    }

    /**
     * 
     * @return
     * @throws ArimaException
     */
    public RationalBackFilter getPsiWeights() throws ArimaException {
        if (m_psi == null) {
            m_psi = initPsi();
        }
        return m_psi;
    }

    /**
     * 
     * @return
     * @throws ArimaException
     */
    @Override
    protected AutoCovarianceFunction initAcgf() throws ArimaException {
        return new AutoCovarianceFunction(getMA().getPolynomial(), getAR().getPolynomial(),
                getInnovationVariance());
    }

    /**
     * 
     * @return
     * @throws ArimaException
     */
    protected RationalBackFilter initPi() throws ArimaException {
        return new RationalBackFilter(getAR(), getMA());
    }

    /**
     * 
     * @return
     * @throws ArimaException
     */
    protected RationalBackFilter initPsi() throws ArimaException {
        return new RationalBackFilter(getMA(), getAR());
    }

    /**
     * 
     * @return
     * @throws ArimaException
     */
    @Override
    protected Spectrum initSpectrum() throws ArimaException {
        return new Spectrum(SymmetricFilter.createFromFilter(getMA()).times(
                getInnovationVariance()), SymmetricFilter.createFromFilter(getAR()));
    }

    @Override
    public String toString() {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("AR = ").append(getAR().toString()).append("; ");
            builder.append("MA = ").append(getMA().toString()).append("; ");
            builder.append("var =").append(getInnovationVariance());
            return builder.toString();
        } catch (ArimaException ex) {
            return "Invalid model";
        }
    }
    
    public Matrix covariance(int n){
        if (!isStationary())
            return null;
        AutoCovarianceFunction acf = getAutoCovarianceFunction();
        Matrix V=new Matrix(n, n);
        acf.prepare(n-1);
        V.diagonal().set(acf.get(0));
        for (int i=1; i<n; ++i){
            double w=acf.get(i);
            V.subDiagonal(i).set(w);
            V.subDiagonal(-i).set(w);
        }
        return V;
    }
}
