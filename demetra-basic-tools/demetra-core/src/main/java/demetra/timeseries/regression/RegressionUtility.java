/*
 * Copyright 2017 National Bank of Belgium
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

import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import demetra.timeseries.TimeSeriesDomain;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class RegressionUtility {

    public <D extends TimeSeriesDomain<?>> int size(List<ITsVariable<D>> vars) {
        int n = 0;
        for (ITsVariable<D> var : vars) {
            n += var.getDim();
        }
        return n;
    }

    public <D extends TimeSeriesDomain<?>> Matrix data(List<ITsVariable<D>> vars, D domain) {
        Matrix M = Matrix.make(domain.length(), size(vars));
        int col = 0;
        ArrayList<DataBlock> cols = new ArrayList<>();
        for (ITsVariable<D> var : vars) {
            int n = var.getDim();
            if (n == 1) {
                var.data(domain, Collections.singletonList(M.column(col)));
                ++col;
            } else {
                cols.clear();
                for (int i=0; i<n; ++i)
                    cols.add(M.column(col++));
                var.data(domain, cols);
            }
        }
        return M;
    }

}
