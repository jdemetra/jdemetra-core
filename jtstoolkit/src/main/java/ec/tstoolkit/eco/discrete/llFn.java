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

package ec.tstoolkit.eco.discrete;

import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.maths.realfunctions.*;

/**
 *
 * @author Jean Palate
 */
class llFn implements IFunction {

    llFn(DiscreteModel model) {
        m_model = model;
    }
    DiscreteModel m_model;

    @Override
    public IParametersDomain getDomain() {
        return new DefaultDomain(m_model.getX().getColumnsCount(), 1e-6);
    }

    @Override
    public IFunctionDerivatives getDerivatives(IFunctionInstance point) {
        return new dllFn((DiscreteModelEvaluation) point);
    }

    @Override
    public IFunctionInstance evaluate(IReadDataBlock parameters) {
        return new DiscreteModelEvaluation(m_model, parameters);
    }
}
