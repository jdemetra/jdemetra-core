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
import demetra.maths.functions.IParametersDomain;
import demetra.data.DoubleSeq;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SsqProxyFunction implements IFunction {
    ISsqFunction ssqFn;

    /**
     * 
     * @param fn
     */
    public SsqProxyFunction(ISsqFunction fn) {
	ssqFn = fn;
    }

    /**
     * 
     * @param parameters
     * @return
     */
    @Override
    public IFunctionPoint evaluate(DoubleSeq parameters) {
	return new SsqProxyFunctionPoint(ssqFn.ssqEvaluate(parameters));
    }

    /**
     * 
     * @return
     */
    @Override
    public IParametersDomain getDomain() {
	return ssqFn.getDomain();
    }
}
