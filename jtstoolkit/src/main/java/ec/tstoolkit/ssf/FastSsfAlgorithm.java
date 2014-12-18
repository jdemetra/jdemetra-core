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

import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.DefaultLikelihoodEvaluation;
import ec.tstoolkit.eco.DiffuseConcentratedLikelihood;

/**
 * 
 * @param <F>
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class FastSsfAlgorithm<F extends ISsf> extends AbstractSsfAlgorithm
	implements ISsfAlgorithm<F> {

    private IFastInitializer<F> m_initializer;

    /**
     * 
     */
    public FastSsfAlgorithm()
    {
    }

    /**
     * 
     * @param initializer
     */
    public FastSsfAlgorithm(IFastInitializer<F> initializer)
    {
	m_initializer = initializer;
    }

    /**
     *
     * @param instance
     * @return
     */
    public DefaultLikelihoodEvaluation<DiffuseConcentratedLikelihood> calcNoRegs(
	    SsfModel<F> instance) {
	FastFilter<F> filter = new FastFilter<>();
	filter.setSsf(instance.ssf);
	if (m_initializer != null)
	    filter.setInitializer(m_initializer);
	DiffusePredictionErrorDecomposition pred = new DiffusePredictionErrorDecomposition(
		isUsingSsq());
	if (filter.process(instance.getData(), pred))
	    return calcLikelihood(pred);
	else
	    return null;
    }

    private DefaultLikelihoodEvaluation<DiffuseConcentratedLikelihood> calcRegs(
	    SsfModel<F> instance) {
	FastFilter<F> filter = new FastFilter<>();
	filter.setSsf(instance.ssf);
	if (m_initializer != null)
	    filter.setInitializer(m_initializer);
	DiffuseFilteringResults drslts = new DiffuseFilteringResults(true);
	if (!filter.process(instance.getData(), drslts))
	    return null;

	return calcLikelihood(instance, drslts);
    }

    /**
     *
     * @param instance
     * @return
     */
    @Override
    public DefaultLikelihoodEvaluation<DiffuseConcentratedLikelihood> evaluate(
	    SsfModel<F> instance) {
	if (instance.getX() == null)
	    return calcNoRegs(instance);
	else
	    return calcRegs(instance);
    }
}
