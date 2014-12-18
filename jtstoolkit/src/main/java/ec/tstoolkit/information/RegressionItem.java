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

package ec.tstoolkit.information;

import java.util.Formatter;

/**
 * 
 * @author pcuser
 */
public class RegressionItem {

    /**
     *
     */
    public final String description;
    /**
     *
     */
    public final boolean prespecified;
    /**
     *
     */
    public final double coefficient;
    /**
     *
     */
    public final double stdError;

    /**
     *
     * @param desc
     * @param prespecified
     * @param coeff
     * @param err
     */
    public RegressionItem(String desc, boolean prespecified, double coeff,
	    double err) {
	this.description = desc;
	this.prespecified = prespecified;
	this.coefficient = coeff;
	this.stdError = err;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	if (description != null)
	    builder.append(description).append(':');
	builder.append(new Formatter().format("%.3f", coefficient).toString());
	if (stdError != 0)
	    builder.append('[').append(
		    new Formatter().format("%.3f", coefficient / stdError)
			    .toString()).append(']');
	return builder.toString();
    }
}
