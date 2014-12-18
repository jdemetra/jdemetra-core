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

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;

/**
 *
 * @author pcuser
 */
public class DefaultSsqFunctionInstance implements IFunctionInstance, ISsqFunctionInstance {

    private final IReadDataBlock params_;
    private final DataBlock e_;

    public DefaultSsqFunctionInstance(IReadDataBlock parameters, IReadDataBlock e) {
        params_ = parameters;
        e_ = new DataBlock(e);
    }

    public DefaultSsqFunctionInstance(IReadDataBlock parameters, double[] e) {
        params_ = parameters;
        e_ = new DataBlock(e);
    }

    @Override
    public IReadDataBlock getParameters() {
        return params_;
    }

    @Override
    public double getValue() {
        return e_.ssq();
    }

    @Override
    public double[] getE() {
        return e_.getData();
    }

    @Override
    public double getSsqE() {
        return e_.ssq();
    }
}
