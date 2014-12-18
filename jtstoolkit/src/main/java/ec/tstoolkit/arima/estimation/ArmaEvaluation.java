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

package ec.tstoolkit.arima.estimation;

import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.eco.DefaultLikelihoodEvaluation;
import ec.tstoolkit.maths.realfunctions.IFunctionInstance;
import ec.tstoolkit.maths.realfunctions.ISsqFunctionInstance;

/**
 * @author Jean Palate
 * @param <S> Specific arima model type
 */
@Development(status = Development.Status.Alpha)
public class ArmaEvaluation<S extends IArimaModel> implements ISsqFunctionInstance,
	IFunctionInstance {

    final ArmaFunction<S> fn;
    final S arma;
    private DefaultLikelihoodEvaluation<ConcentratedLikelihood> m_ll;
    private boolean m_failed = false;

    public ArmaEvaluation(ArmaFunction<S> fn, S arma) {
	this.fn = fn;
	this.arma = arma;
    }

    private void calc() {
	ConcentratedLikelihoodEstimation cll = new ConcentratedLikelihoodEstimation(
		fn.filter);
	if (cll.estimate(fn.dmodel, fn.d, fn.missings, arma)) {
	    m_ll = new DefaultLikelihoodEvaluation<>(cll
		    .getLikelihood());
	    m_ll.useML(fn.ml);
            m_ll.useLogLikelihood(fn.llog);
	} else
	    m_failed = true;
    }

    @Override
    public double[] getE() {
	if (m_ll == null && !m_failed)
	    calc();
	return m_ll.getE();
    }

    public ConcentratedLikelihood getLikelihood() {
	if (m_ll == null && !m_failed)
	    calc();
	return m_ll.getLikelihood();
    }

    @Override
    public IReadDataBlock getParameters() {
	return fn.mapper.map(arma);
    }

    @Override
    public double getSsqE() {
	if (m_ll == null && !m_failed)
	    calc();
	return m_ll.getSsqValue();
    }

    @Override
    public double getValue() {
	if (m_ll == null && !m_failed)
	    calc();
	return m_ll.getValue();
    }
}
