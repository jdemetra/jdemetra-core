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
package jdplus.ucarima;

import jdplus.arima.ArimaModel;
import demetra.design.Development;


/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public abstract class SimpleModelDecomposer
{
    ArimaModel model, signal, noise;

    /**
     *
     */
    protected SimpleModelDecomposer() {
	model = signal = noise = null;
    }

    /**
         *
         */
    protected abstract void calc();

    /**
         *
         */
    protected void clear() {
	signal = noise = null;
    }

    /**
     * 
     * @return
     */
    public ArimaModel getModel() {
	return model;
    }

    /**
     * 
     * @return
     */
    public ArimaModel getNoise() {
	if (noise == null)
	    calc();
	return noise;
    }

    /**
     * 
     * @return
     */
    public ArimaModel getSignal() {
	if (signal == null)
	    calc();
	return signal;
    }

    /**
     * 
     * @param value
     */
    public void setModel(final ArimaModel value) {
	model = value;
	clear();
    }

}
