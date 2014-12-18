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
import ec.tstoolkit.eco.DiffuseLikelihood;
import ec.tstoolkit.eco.Likelihood;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public final class LikelihoodEvaluation {

    /**
     *
     * @param rslts
     * @param ll
     */
    public static void evaluate(final DiffuseFilteringResults rslts,
	    final DiffuseLikelihood ll) {
	int n = rslts.getObsCount(), d = rslts.getDiffuseCount();
	double ssqerr = rslts.getSsqErr(), ldet = rslts.getLogDeterminant(), lddet = rslts
		.getDiffuseLogDeterminant();
	ll.set(ssqerr, ldet, lddet, n + d, d);
	ll.setRes(rslts.getFilteredData().data(true, true));
    }

    /**
     *
     * @param rslts
     * @param ll
     */
    public static void evaluate(
	    final DiffusePredictionErrorDecomposition rslts,
	    final DiffuseLikelihood ll) {
	int n = rslts.getObsCount(), d = rslts.getDiffuseCount();
	double ssqerr = rslts.getSsqErr(), ldet = rslts.getLogDeterminant(), lddet = rslts
		.getDiffuseLogDeterminant();
	ll.set(ssqerr, ldet, lddet, n + d, d);
	ll.setRes(rslts.residuals(true));
    }

    /**
     *
     * @param rslts
     * @param ll
     */
    public static void evaluate(final ResidualsCumulator rslts,
	    final DiffuseLikelihood ll) {
	int n = rslts.getObsCount();
	double ssqerr = rslts.getSsqErr(), ldet = rslts.getLogDeterminant();
	ll.set(ssqerr, ldet, 0, n, 0);
    }

    /**
     *
     * @param rslts
     * @param ll
     */
    public static void evaluate(final ResidualsCumulator rslts,
	    final Likelihood ll) {
	int n = rslts.getObsCount();
	double ssqerr = rslts.getSsqErr(), ldet = rslts.getLogDeterminant();
	ll.set(ssqerr, ldet, n);
    }

    private LikelihoodEvaluation() {
    }
}
