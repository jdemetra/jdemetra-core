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
 * @param <I>
 * @param <E>
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class FunctionInstance<I extends IParametric, E extends IEvaluation<I> >
	implements IFunctionInstance {
    private I m_instance;

    private E m_evaluation;

    /**
     * 
     * @param i
     * @param e
     */
    public FunctionInstance(I i, E e) {
	m_instance = i;
	m_evaluation = e;
    }

    E getEvaluation() {
	return m_evaluation;
    }

    I getInstance() {
	return m_instance;
    }

    /**
     * 
     * @return
     */
    @Override
    public IReadDataBlock getParameters() {
	return m_instance.getParameters();
    }

    /**
     * 
     * @return
     */
    @Override
    public double getValue() {
        if (! m_evaluation.evaluate(m_instance))
            return Double.NaN;
	return m_evaluation.getValue();
    }
}
