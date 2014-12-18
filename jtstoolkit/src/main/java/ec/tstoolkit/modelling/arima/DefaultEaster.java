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
import ec.tstoolkit.timeseries.regression.EasterVariable;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class DefaultEaster implements IVariableDescriptor {

    @Override
    public int addVariables(TsDomain estimationDomain, List<Variable> vars) {
        EasterVariable easter = new EasterVariable();
        easter.setDuration(duration);
        easter.setType(type);
        easter.includeEaster(hasEaster);
        easter.includeEasterMonday(hasEasterMonday);
        Variable var = new Variable(easter, ComponentType.CalendarEffect);
        var.status = status;
        vars.add(var);
        return 1;
    }

    public boolean hasEaster, hasEasterMonday;
    public int duration=6;
    public EasterVariable.Type type = EasterVariable.Type.Theoretical;
    public RegStatus status = RegStatus.Prespecified;
}
