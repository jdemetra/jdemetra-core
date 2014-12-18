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
public class ValidityPeriod {
    private Day beg_, end_;

    public ValidityPeriod() { }

    public ValidityPeriod(Day beg, Day end) {
	beg_ = beg;
	end_ = end;
    }

    public Day getStart() {
	return beg_;
    }
    public void setStart(Day value) {
        beg_ = value;
    }

    public Day getEnd() {
	return end_;
    }
    public void setEnd(Day value) {
        end_ = value;
    }

    public boolean isStartSpecified() {
        return beg_ != Day.BEG;
    }

    public boolean isEndSpecified() {
        return end_ != Day.END;
    }
}
