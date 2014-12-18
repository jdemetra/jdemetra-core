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

import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.DefaultLikelihoodEvaluation;
import ec.tstoolkit.eco.DiffuseConcentratedLikelihood;
import ec.tstoolkit.maths.realfunctions.IFunctionInstance;
import ec.tstoolkit.maths.realfunctions.ISsqFunctionInstance;

/**
 * 
 * @param <S>
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SsfFunctionInstance<S extends ISsf> implements
	ISsqFunctionInstance, IFunctionInstance {

    /**
     *
     */
    public final SsfFunction<S> fn;

    /**
     *
     */
    public final S ssf;
    private DefaultLikelihoodEvaluation<DiffuseConcentratedLikelihood> m_ll;

    /**
     * 
     * @param fn
     * @param p
     */
    public SsfFunctionInstance(SsfFunction<S> fn, IReadDataBlock p)
    {
	this.fn = fn;
	// compute the new ssf, if need be
	if (p != null) {
	    this.ssf = fn.mapper.map(p);
	    SsfModel<S> m = new SsfModel<>(ssf, fn.model);
	    // compute the likelihood
	    m_ll = fn.algorithm.evaluate(m);
	} else {
	    this.ssf = fn.model.ssf;
	    m_ll = fn.algorithm.evaluate(fn.model);
	}
    }

    @Override
    public double[] getE() {
	return m_ll.getE();
    }

    /**
     * 
     * @return
     */
    public DiffuseConcentratedLikelihood getLikelihood()
    {
	return m_ll == null ? null : m_ll.getLikelihood();
    }

    @Override
    public IReadDataBlock getParameters() {
	return fn.mapper.map(ssf);
    }

    @Override
    public double getSsqE() {
	return m_ll != null ? m_ll.getSsqValue() : Double.NaN;
    }

    @Override
    public double getValue() {
	return (m_ll == null) ? Double.NaN : m_ll.getValue();
    }
}
