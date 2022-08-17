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
@lombok.Builder(toBuilder = true)
public class ModifiedTsVariable implements ITsVariable {

    public static interface Modifier {

        /**
         * Gets the number of output for one input
         *
         * @return
         */
        int dim();

        /**
         * Changes the dimension of a given variable
         *
         * @param n Dimension of the original or modified variable
         * @return
         */
        default int redim(int n) {
            return n * dim();
        }

        /**
         * Gets additional description for the block of modifications
         *
         * @return
         */
        String description();

        /**
         * Get additional description
         *
         * @param idx Index of the output of the modifier
         * @return
         */
        String description(int idx);
    }

    @lombok.NonNull
    ITsVariable variable;
    @lombok.Singular
    @lombok.NonNull
    List<Modifier> modifiers;

    @Override
    public int dim() {
        return mdim() * variable.dim();
    }

    private int mdim() {
        int d = 1;
        for (Modifier m : modifiers) {
            d *= m.dim();
        }
        return d;

    }

    @Override
    public <D extends TimeSeriesDomain<?>> String description(D context) {
        StringBuilder builder = new StringBuilder();
        builder.append(variable.description(context));
        for (Modifier m : modifiers) {
            builder.append(m.description());
        }
        return builder.toString();
    }

    @Override
    public <D extends TimeSeriesDomain<?>> String description(int idx, D context) {
        int dim = mdim();
        String desc = variable.description(idx / dim, context);
        idx /= variable.dim();
        StringBuilder builder = new StringBuilder();
        builder.append(desc);
        for (Modifier m : modifiers) {
            int cdim = m.dim();
            dim /= cdim;
            builder.append(m.description(idx / dim));
            idx /= cdim;
        }
        return builder.toString();
    }

}
