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

package jdplus.math.functions;

import demetra.design.Development;
import java.util.function.IntSupplier;


/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public enum ParamValidation implements IntSupplier {

    /**
     *
     */
    Valid(1),
    /**
     * 
     */
    Changed(0),
    /**
     *
     */
    Invalid(-1);
    private final int value;

    ParamValidation(final int value) {
	this.value = value;
    }

    /**
     * Returns the value of this ParamValidation as an int.
     * @return
     */
    @Override
    public int getAsInt() {
	return value;
    }
}
