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


package demetra.maths.functions.ssq;

import demetra.design.Development;
import demetra.maths.functions.IFunction;
import demetra.maths.functions.IFunctionPoint;
import demetra.data.Doubles;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SsqProxyFunctionPoint implements IFunctionPoint {
    ISsqFunctionPoint fx;

    /**
     * 
     * @param f
     */
    public SsqProxyFunctionPoint(ISsqFunctionPoint f) {
	fx = f;
    }

    /**
     * 
     * @return
     */
    @Override
    public Doubles getParameters() {
	return fx.getParameters();
    }

    /**
     * 
     * @return
     */
    @Override
    public double getValue() {
	return fx.getSsqE();
    }
    
    public ISsqFunctionPoint getCore(){
        return fx;
    }

    @Override
    public IFunction getFunction() {
        return new SsqProxyFunction(fx.getSsqFunction());
    }
}
