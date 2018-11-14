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
package demetra.maths.matrices;

import demetra.data.DataBlock;
import demetra.design.Unsafe;
import java.util.Iterator;
import demetra.data.DataBlockIterator;
import demetra.data.DoubleReader;
import demetra.data.DoubleSequence;
import demetra.data.Doubles;

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

    public double apply(DoubleSequence x) {
        DataBlockIterator columns = S.columnsIterator();
        DoubleReader cell = x.reader();
        double s = 0;
        while (columns.hasNext()) {
            s += cell.next() * Doubles.dot(columns.next(), x);
        }
        return s;
    }

    public static double apply(final Matrix M, final DataBlock x) {
        double[] pm = M.getStorage();
        double[] px = x.getStorage();

        int x0 = x.getStartPosition(), x1 = x.getEndPosition(), xinc = x.getIncrement();
        int m0 = M.getStartPosition(), rinc = M.getRowIncrement(), cinc = M.getColumnIncrement();
        double s = 0;
        for (int ic = m0, id = m0, ix = x0; ix != x1; ix += xinc, id += rinc + cinc, ic += cinc) {
            double xi = px[ix];
            double z = pm[id] * xi;
            for (int ir = ic, jx = x0; jx < ix; ir += rinc, jx += xinc) {
                z += 2 * pm[ir] * px[jx];
            }
            s += z * xi;
        }
        return s;
    }

}