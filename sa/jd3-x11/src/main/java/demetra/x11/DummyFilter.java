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

package demetra.x11;

import demetra.maths.linearfilters.SymmetricFilter;
import demetra.timeseries.simplets.TsData;
import demetra.timeseries.simplets.TsDomain;

/**
 * The dummy filter will set the filtered series to O or 1 (multiplicative case).
 * @author Jean Palate
 */
public class DummyFilter implements IFiltering{
    
    public static final String NAME="None";
    
    private final boolean mul;
    
    
    public DummyFilter(boolean mul){
        this.mul=mul;
    }

    @Override
    public SymmetricFilter getCentralFilter() {
        return null;
    }

    @Override
    public String getDescription() {
        return NAME;
    }

    @Override
    public TsData process(TsData s, TsDomain domain) {
        return new TsData(domain, mul ? 1 : 0);
    }
    
}
