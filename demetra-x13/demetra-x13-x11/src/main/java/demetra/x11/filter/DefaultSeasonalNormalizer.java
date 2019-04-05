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

        ArrayList<Integer> stable_index = new ArrayList<>();
        SeasonalFilterOption[] filters = context.getFinalSeasonalFilter();
        for (int i = 0; i < context.getPeriod(); i++) {
            if (SeasonalFilterOption.Stable.equals(filters[i])) {
                stable_index.add(i);
            }
        }

        int start_period_input = (nextend + context.getFirstPeriod()) % context.getPeriod();
        SymmetricFilter filter = X11FilterFactory.makeSymmetricFilter(context.getPeriod());
        int ndrop = filter.length() / 2;

        double[] x = new double[in.length()];
        DataBlock out = DataBlock.ofInternal(x, ndrop, x.length - ndrop);
        filter.apply(i -> in.get(i), IFilterOutput.of(out, ndrop));

        // needed because series is too short for filter
        CopyEndPoints cp = new CopyEndPoints(ndrop);
        cp.process(in, DataBlock.ofInternal(x));

        if (!stable_index.isEmpty()) {
            int index = 0;
            for (int period = start_period_input; period < start_period_input + ndrop; period++) {
                if (stable_index.contains(period % context.getPeriod())) {
                    x[index] = x[index + context.getPeriod()];
                }
                index++;
            }
            int end_period_input = (in.length() - 1 - start_period_input) % context.getPeriod();
            index = in.length() - 1;
            for (int period = end_period_input; period > end_period_input - ndrop; period--) {
                if (stable_index.contains(period % context.getPeriod())) {
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
