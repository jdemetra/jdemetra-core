/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.tramoseats.spi;

import demetra.seats.SeatsResults;

/**
 *
 * @author palatej
 */

@lombok.experimental.UtilityClass
public class ApiUtility {
    public demetra.seats.SeatsResults toApi(jdplus.seats.SeatsResults rslts){
        
        return SeatsResults.builder()
                .initialModel(jdplus.modelling.ApiUtility.toApi(rslts.getOriginalModel(), "original"))
                .finalModel(jdplus.modelling.ApiUtility.toApi(rslts.getFinalModel(), "final"))
                .decomposition(jdplus.modelling.ApiUtility.toApi(rslts.getUcarimaModel(), null))
                .initialComponents(rslts.getInitialComponents())
                .finalComponents(rslts.getFinalComponents())
                .build();
        
    }
}
