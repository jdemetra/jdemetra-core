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
import ec.tstoolkit.maths.matrices.SubMatrix;

/**
 * Uni-variate state space model
 * y = X b + e
 * e ~ ssf
 * b may contain diffuse elements and/or fixed unknown elements

 * @param <F> The class of the state space model 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SsfModel<F extends ISsf> extends SsfModelData {

    /**
     *
     */
    public final F ssf;

    /**
     * Creates a new uni-variate ssf model
     * @param ssf The state space form of the residuals
     * @param data The observations
     * @param X The regression variables. May be null.
     * @param DiffuseX The 0-based positions of the diffuse regression coefficients.
     * May be null.
     */
    public SsfModel(final F ssf, final ISsfData data, final SubMatrix X,
	    final int[] DiffuseX) {
	super(data, X, DiffuseX);
	this.ssf = ssf;
    }

    /**
     * Creates a new uni-variate ssf model
     * @param ssf The state space form of the residuals
     * @param model The data of the model
     */
    public SsfModel(F ssf, SsfModelData model)
    {
	super(model.getData(), model.getX(), model.getDiffuseX());
	this.ssf = ssf;
    }
}
