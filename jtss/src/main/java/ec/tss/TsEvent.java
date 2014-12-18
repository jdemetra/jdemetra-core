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

package ec.tss;

import ec.tstoolkit.design.Development;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class TsEvent {

    /**
     *
     */
    public final Ts ts;

    /**
     *
     */
    public final TsCollection tscollection;

    /**
     *
     */
    public final TsInformationType event;

    /**
     *
     */
    public final Object source;

    TsEvent(Ts ts, TsInformationType event, Object source) {
	this.ts = ts;
	this.tscollection = null;
	this.event = event;
	this.source = source;
    }

    TsEvent(TsCollection tscollection, TsInformationType event, Object source) {
	this.ts = null;
	this.tscollection = tscollection;
	this.event = event;
	this.source = source;
    }

    /**
     * 
     * @return
     */
    public boolean isCollection()
    {
	return tscollection != null;
    }

    /**
     * 
     * @return
     */
    public boolean isSeries()
    {
	return ts != null;
    }
}
