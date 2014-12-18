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
 * Computes the objective function for a given state space model
 * @param <F> The class of the state space framework
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface ISsfAlgorithm<F extends ISsf> {

    /**
     * Computes the objective function for a given state space model. 
     * @param instance The state space model
     * @return Returns an evaluation function based on the likelihood of the model.
     * See the class DefaultLikelihoodEvaluation for further information.
     */
    DefaultLikelihoodEvaluation<DiffuseConcentratedLikelihood> evaluate(
	    SsfModel<F> instance);
}
