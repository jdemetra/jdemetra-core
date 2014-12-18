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
package ec.tstoolkit.ssf;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.Determinant;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class ResidualsCumulator {

    Determinant m_det = new Determinant();

    double m_ssqerr;

    int m_n;

    /** Creates a new instance of PredictionErrorDecomposition */
    public ResidualsCumulator() {
    }

    /**
     * 
     * @param e
     * @param var
     */
    public void add(final double e, final double var)
    {
        if (Math.abs(var)<State.EPS){
            if (Math.abs(e)<State.EPS)
                return;            
        }
        
	m_det.add(var);
	m_ssqerr += e * e / var;
	++m_n;
    }

   /**
     * 
     */
    public void clear()
    {
	m_ssqerr = 0;
	m_det.clear();
	m_n = 0;
    }

    /**
     * 
     * @return
     */
    public double getLogDeterminant()
    {
	return m_det.getLogDeterminant();
    }

    /**
     * 
     * @return
     */
    public int getObsCount()
    {
	return m_n;
    }

    /**
     * 
     * @return
     */
    public double getSsqErr()
    {
	return m_ssqerr;
    }
}
