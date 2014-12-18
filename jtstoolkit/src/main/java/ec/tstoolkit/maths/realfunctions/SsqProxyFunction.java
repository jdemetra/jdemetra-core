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
import ec.tstoolkit.design.Development;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SsqProxyFunction implements IFunction {
    ISsqFunction m_fn;

    /**
     * 
     * @param fn
     */
    public SsqProxyFunction(ISsqFunction fn) {
	m_fn = fn;
    }

    /**
     * 
     * @param parameters
     * @return
     */
    @Override
    public IFunctionInstance evaluate(IReadDataBlock parameters) {
	return new SsqProxyFunctionInstance(m_fn.ssqEvaluate(parameters));
    }

    /**
     * 
     * @param point
     * @return
     */
    @Override
    public IFunctionDerivatives getDerivatives(IFunctionInstance point) {
	return new NumericalDerivatives(this, point, true);
    }

    /**
     * 
     * @return
     */
    @Override
    public IParametersDomain getDomain() {
	return m_fn.getDomain();
    }
}
