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
package ec.tstoolkit.timeseries;

import ec.tstoolkit.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public final class Factory {

    /**
     * 
     * @param days
     * @return
     */
    public static IDomain makeDays(final Day[] days) {
	return new Days(days);
    }

    /*
     * public IDomain MakeDomain(Nbb.SimpleTS.TSPeriod start, int n) { return
     * new RegularDomain(start, n); }
     */

    /**
     * 
     * @param domain
     * @return
     */
    public static GeneralTsData makeTS(final IDomain domain) {
	return new GeneralTsData(domain);
    }

    /**
     * 
     * @param start
     * @param end
     * @return
     */
    public static IDomain makeWeeks(final Day start, final Day end) {
	Week w0 = new Week(start, DayOfWeek.Monday);
	Week w1 = new Week(end, DayOfWeek.Monday);

	return new Weeks(w0, 1 + (w1.getId() - w0.getId()) / 7);
    }

    private Factory() {
    }

}
