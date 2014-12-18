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

package ec.tstoolkit.modelling.arima.tramo.seriestest;

import ec.tstoolkit.design.IntValue;
import java.util.EnumSet;

/**
 *
 * @author gianluca
 */
public enum SerType implements IntValue {
    Residuals(0),
    Xlin(1);
   public static SerType valueOf(final int value) {
	for (SerType option : EnumSet.allOf(SerType.class))
	    if (option.intValue() == value)
		return option;
	return null;
    }

    private final int value;

    static final SerType[] allFreqs = new SerType[] {
	    SerType.Residuals,SerType.Xlin};
	    

    SerType(final int value) {
	this.value = value;
    }

    /**
     * Returns the value of this SerType as an int.
     * @return 
     */
    @Override
    public int intValue() {
	return value;
    }
    
}
