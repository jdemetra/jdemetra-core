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

package ec.tstoolkit.modelling.arima;

import ec.tstoolkit.modelling.Variable;
import ec.tstoolkit.modelling.RegStatus;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.regression.GregorianCalendarVariables;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.LeapYearVariable;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class DefaultTradingDays implements IVariableDescriptor {

    public LengthOfPeriodType lpType = LengthOfPeriodType.None;
    public TradingDaysType tdType = TradingDaysType.None;
    public RegStatus status = RegStatus.Prespecified;

    public int addVariables(TsDomain estimationDomain, List<Variable> vars) {
        if (tdType == TradingDaysType.None) {
            return 0;
        }
        int nvar = 1;
        ITsVariable var = GregorianCalendarVariables.getDefault(tdType);
        Variable tvar = new Variable(var, ComponentType.CalendarEffect);
        tvar.status = status;
        vars.add(tvar);
        if (lpType != LengthOfPeriodType.None) {
            ++nvar;
            LeapYearVariable lp = new LeapYearVariable(lpType);
            Variable lvar = new Variable(lp, ComponentType.CalendarEffect);
            lvar.status = status;
            vars.add(lvar);
        }
        return nvar;
    }
}
