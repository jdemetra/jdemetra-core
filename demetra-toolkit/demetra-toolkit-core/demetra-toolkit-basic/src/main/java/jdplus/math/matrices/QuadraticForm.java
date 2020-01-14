/*
 * Copyright 2016 National Bank copyOf Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions copyOf the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy copyOf the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.math.matrices;

import jdplus.data.DataBlock;
import demetra.design.Unsafe;
import jdplus.data.DataBlockIterator;
import demetra.data.DoubleSeqCursor;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
public class QuadraticForm {

    private final Matrix S;

    private QuadraticForm(Matrix s) {
        S = s;
    }

    public static QuadraticForm of(Matrix s) {
        if (!s.isSymmetric(0)) {
            return null;
        } else {
            return new QuadraticForm(s);
        }
    }

    @Unsafe
    public static QuadraticForm ofSymmetric(Matrix s) {
        return new QuadraticForm(s);
    }

    public double apply(DoubleSeq x) {
        DataBlockIterator columns = S.columnsIterator();
        DoubleSeqCursor cell = x.cursor();
        double s = 0;
        while (columns.hasNext()) {
            s += cell.getAndNext() * columns.next().dot(x);
        }
        return s;
    }

    public static double apply(final Matrix M, final DataBlock x) {
        double[] pm = M.getStorage();
        double[] px = x.getStorage();

        int x0 = x.getStartPosition(), x1 = x.getEndPosition(), xinc = x.getIncrement();
        int m0 = M.getStartPosition(), cinc = M.getColumnIncrement();
        double s = 0;
        for (int ic = m0, id = m0, ix = x0; ix != x1; ix += xinc, id += 1 + cinc, ic += cinc) {
            double xi = px[ix];
            double z = pm[id] * xi;
            for (int ir = ic, jx = x0; jx < ix; ir ++, jx += xinc) {
                z += 2 * pm[ir] * px[jx];
            }
            s += z * xi;
        }
        return s;
    }

}
