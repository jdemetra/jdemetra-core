/*
 * Copyright 2016 National Bank of Belgium
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
 /*
 */
package demetra.ucarima.ssf;

import demetra.arima.ssf.SsfArima;
import demetra.ssf.implementations.CompositeSsf;
import demetra.ssf.implementations.Loading;
import jdplus.ucarima.UcarimaModel;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class SsfUcarima {

    public CompositeSsf of(final UcarimaModel ucm) {
        UcarimaModel ucmc = ucm.simplify();
        int n = ucmc.getComponentsCount();
        CompositeSsf.Builder builder = CompositeSsf.builder();
        for (int i = 0; i < n; ++i) {
            builder.add(SsfArima.componentOf(ucmc.getComponent(i)), Loading.fromPosition(0));
        }
        return builder.build();
    }
}
