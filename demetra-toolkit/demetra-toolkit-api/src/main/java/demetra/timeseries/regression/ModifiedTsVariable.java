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
package demetra.timeseries.regression;

import demetra.timeseries.TimeSeriesDomain;
import nbbrd.design.Development;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.Value
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public class ModifiedTsVariable implements ITsVariable {
    
    public static interface Modifier {

        /**
         * Changes the dimension of a given variable
         *
         * @param dim Dimension of the original variable
         * @return
         */
        int redim(int dim);
    }
    
    @lombok.NonNull
    ITsVariable variable;
    @lombok.Singular
    @lombok.NonNull
    List<Modifier> modifiers;
    
    @Override
    public int dim() {
        int d = variable.dim();
        for (Modifier m : modifiers) {
            d = m.redim(d);
        }
        return d;
    }
    
    @Override
    public <D extends TimeSeriesDomain<?>> String description(D context) {
        return variable.description(context);
    }
    
}
