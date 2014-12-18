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
public class SsqProxyFunctionInstance implements IFunctionInstance {
    ISsqFunctionInstance m_f;

    /**
     * 
     * @param f
     */
    public SsqProxyFunctionInstance(ISsqFunctionInstance f) {
	m_f = f;
    }

    /**
     * 
     * @return
     */
    @Override
    public IReadDataBlock getParameters() {
	return m_f.getParameters();
    }

    /**
     * 
     * @return
     */
    @Override
    public double getValue() {
	return m_f.getSsqE();
    }
    
    public ISsqFunctionInstance getCore(){
        return m_f;
    }
}
