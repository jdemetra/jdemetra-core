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
package demetra.timeseries.regression;

import java.util.Arrays;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Root of all regression variable definition. All definitions must contain
 * enough information for generating the actual regression variables in a given
 * context (corresponding to a ModellingContext).
 *
 * @author palatej
 */
public interface ITsVariable {

    int dim();

    public static int dim(@NonNull ITsVariable... vars) {
        return dim(Arrays.stream(vars));
    }

    public static int dim(@NonNull Stream<ITsVariable> vars) {
        return vars.mapToInt(var->var.dim()).sum();
    }

    public static String nextName(String name) {
        int pos0 = name.lastIndexOf('('), pos1 = name.lastIndexOf(')');
        if (pos0 > 0 && pos1 > 0) {
            String prefix = name.substring(0, pos0);
            int cur = 1;
            try {
                String num = name.substring(pos0 + 1, pos1);
                cur = Integer.parseInt(num) + 1;
            } catch (NumberFormatException err) {

            }
            StringBuilder builder = new StringBuilder();
            builder.append(prefix).append('(').append(cur).append(')');
            return builder.toString();
        } else {
            return name + "(1)";
        }
    }

}
