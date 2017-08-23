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


package demetra.sarima.estimation;

import demetra.arima.regarima.internals.RegArmaModel;
import demetra.design.Development;
import demetra.sarima.SarimaModel;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@FunctionalInterface
public interface IarmaInitializer {
    /**
     * 
     * @param regarma
     * @return
     */
    SarimaModel initialize(RegArmaModel<SarimaModel> regarma);

    public static IarmaInitializer defaultInitializer(){
        return regarma->SarimaModel.builder(regarma.getArma().specification()).setDefault(-.1, -.2).build();
    }
    
    public static IarmaInitializer defaultInitializer(final double ar, final double ma){
        return regarma->SarimaModel.builder(regarma.getArma().specification()).setDefault(ar, ma).build();
    }
}
