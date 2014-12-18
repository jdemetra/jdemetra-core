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

package ec.tstoolkit.maths.realfunctions;

import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.IDataBlock;
import ec.tstoolkit.design.Development;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class DefaultDomain implements IParametersDomain {

    private int m_n;
    private double m_eps;

    /**
     * 
     * @param n
     * @param eps
     */
    public DefaultDomain(int n, double eps) {
	m_n = n;
	m_eps = eps;
    }

    /**
     * 
     * @param inparams
     * @return
     */
    @Override
    public boolean checkBoundaries(IReadDataBlock inparams) {
	return true;
    }

    @Override
    public double epsilon(IReadDataBlock inparams, int idx) {
	return inparams.get(idx) * m_eps;
    }

    @Override
    public int getDim() {
	return m_n;
    }

    @Override
    public String getDescription(int idx) {
         return PARAM+idx; 
    }
    /**
     * 
     * @param idx
     * @return
     */
    @Override
    public double lbound(int idx) {
	return Double.NEGATIVE_INFINITY;
    }

    @Override
    public double ubound(int idx) {
	return Double.POSITIVE_INFINITY;
    }

    @Override
    public ParamValidation validate(IDataBlock ioparams) {
	return ParamValidation.Valid;
    }
}
