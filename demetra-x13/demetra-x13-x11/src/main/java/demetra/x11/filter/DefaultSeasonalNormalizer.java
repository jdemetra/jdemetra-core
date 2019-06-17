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
package demetra.x11.filter;

import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import demetra.design.Development;
import demetra.maths.linearfilters.IFilterOutput;
import demetra.maths.linearfilters.SymmetricFilter;
import demetra.x11.SeasonalFilterOption;
import demetra.x11.X11Context;
import demetra.x11.filter.endpoints.CopyEndPoints;
import demetra.x11.filter.endpoints.CopyPeriodicEndPoints;
import java.util.ArrayList;

/**
 *
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.experimental.UtilityClass
public class DefaultSeasonalNormalizer {

    public DoubleSequence normalize(DoubleSequence in, int nextend, X11Context context) {
        return normalize(in, nextend, context, 0);
    }

    public DoubleSequence normalize(DoubleSequence in, int nextend, X11Context context, int start) {

        ArrayList<Integer> stable_index = new ArrayList<>();
        SeasonalFilterOption[] filters = context.getFinalSeasonalFilter();

        // conditions for short time series
        int ny_all = in.length() / context.getPeriod();
        int nyr_all = in.length() % context.getPeriod() == 0 ? ny_all : ny_all + 1;

        int ind;
        for (int i = 0; i < context.getPeriod(); i++) {
            ind = (start + i) % context.getPeriod();
            if (SeasonalFilterOption.Stable.equals(filters[i])
                    || (ny_all < 5 || (SeasonalFilterOption.S3X15.equals(filters[i]) && nyr_all < 20))) { // condition for too short time series
                stable_index.add(ind);
            }
        }

        int start_period_input = (nextend + context.getFirstPeriod()) % context.getPeriod();
        SymmetricFilter filter = X11FilterFactory.makeSymmetricFilter(context.getPeriod());
        int ndrop = filter.length() / 2;

        double[] x = new double[in.length()];
        DataBlock out = DataBlock.ofInternal(x, ndrop, x.length - ndrop);
        filter.apply(j -> in.get(j), IFilterOutput.of(out, ndrop));

        // needed because series is too short for filter
        CopyEndPoints cp = new CopyEndPoints(ndrop);
        cp.process(in, DataBlock.ofInternal(x));

        if (!stable_index.isEmpty()) {
            int index = 0;
            for (int p = start_period_input; p < start_period_input + ndrop; p++) {
                if (stable_index.contains(p % context.getPeriod())) {
                    x[index] = x[index + context.getPeriod()];
                }
                index++;
            }
            int end_period_input = (in.length() - 1 + start_period_input) % context.getPeriod();
            index = in.length() - 1;
            for (int p = end_period_input; p > end_period_input - ndrop; p--) {
// the period of x[index]=(ndrop * context.getPeriod() + p)) 
//ndrop * context.getPeriod() is big enough that 
                if (stable_index.contains((ndrop * context.getPeriod() + p) % context.getPeriod())) {
                    x[index] = x[index - context.getPeriod()];
                }
                index--;
            }
        }
        DoubleSequence t = DoubleSequence.ofInternal(x);
        DoubleSequence tmp = context.remove(in, t);
        if (nextend == 0) {
            return tmp;
        } else {
            x = new double[x.length + 2 * nextend];
            tmp.copyTo(x, nextend);
            CopyPeriodicEndPoints cpp = new CopyPeriodicEndPoints(nextend, context.getPeriod());
            cpp.process(null, DataBlock.ofInternal(x));
            return DoubleSequence.ofInternal(x);
        }

    }
}
