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

import jdplus.data.DataBlock;
import demetra.design.Development;
import demetra.maths.linearfilters.SymmetricFilter;
import demetra.x11.X11Context;
import demetra.x11.filter.endpoints.CopyEndPoints;
import demetra.x11.filter.endpoints.CopyPeriodicEndPoints;
import demetra.data.DoubleSeq;

/**
 *
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.experimental.UtilityClass
public class DefaultSeasonalNormalizer {

    public DoubleSeq normalize(DoubleSeq in, int nextend, X11Context context) {
        SymmetricFilter filter = X11FilterFactory.makeSymmetricFilter(context.getPeriod());
        int ndrop = filter.length() / 2;

        double[] x = new double[in.length()];
        DataBlock out = DataBlock.of(x, ndrop, x.length - ndrop);
        filter.apply(in, out);

        CopyEndPoints cp = new CopyEndPoints(ndrop);
        cp.process(in, DataBlock.of(x));
        DoubleSeq t = DoubleSeq.of(x);
        DoubleSeq tmp = context.remove(in, t);
        if (nextend == 0) {
            return tmp;
        } else {
            x = new double[x.length + 2 * nextend];
            tmp.copyTo(x, nextend);
            CopyPeriodicEndPoints cpp = new CopyPeriodicEndPoints(nextend, context.getPeriod());
            cpp.process(null, DataBlock.of(x));
            return DoubleSeq.of(x);
        }
    }
}
