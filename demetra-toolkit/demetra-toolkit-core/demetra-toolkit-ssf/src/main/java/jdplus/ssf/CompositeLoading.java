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
package jdplus.ssf;

import jdplus.data.DataBlock;
import jdplus.data.DataWindow;
import jdplus.math.matrices.MatrixWindow;
import jdplus.data.DataBlockIterator;
import demetra.data.DoubleSeqCursor;
import jdplus.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public class CompositeLoading implements ISsfLoading {

    private final ISsfLoading[] loadings;
    private final int[] dim;
    private final DataBlock tmp;

    public CompositeLoading(final int[] dim, final ISsfLoading[] ms) {
        this.loadings = ms;
        this.dim = dim;
        int n = ms.length;
        int tdim = 0;
        for (int i = 0; i < n; ++i) {
            tdim += dim[i];
        }
        tmp = DataBlock.make(tdim);
    }

    @Override
    public boolean isTimeInvariant() {
        for (int i = 0; i < loadings.length; ++i) {
            if (!loadings[i].isTimeInvariant()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void Z(int pos, DataBlock z) {
        DataWindow cur = z.left();
        for (int i = 0; i < loadings.length; ++i) {
            loadings[i].Z(pos, cur.next(dim[i]));
        }
    }

    @Override
    public double ZX(int pos, DataBlock m) {
        DataWindow cur = m.window(0, dim[0]);
        double x = loadings[0].ZX(pos, cur.get());
        for (int i = 1; i < loadings.length; ++i) {
            x += loadings[i].ZX(pos, cur.next(dim[i]));
        }
        return x;
    }

    @Override
    public double ZVZ(int pos, FastMatrix v) {
        MatrixWindow D = v.topLeft(0, 0);
        double x = 0;
        for (int i = 0; i < loadings.length; ++i) {
            int ni = dim[i];
            tmp.set(0);
            DataWindow wnd = tmp.left();
            FastMatrix nD = D.next(ni, ni);
            x += loadings[i].ZVZ(pos, nD);
            MatrixWindow C = MatrixWindow.of(nD);
            for (int j = i + 1; j < loadings.length; ++j) {
                int nj = dim[j];
                DataBlock cur = wnd.next(nj);
                loadings[j].ZM(pos, C.vnext(nj), cur);
                x += 2 * loadings[i].ZX(pos, cur);
            }
        }
        return x;
    }

    @Override
    public void VpZdZ(int pos, FastMatrix V, double d) {
        if (d == 0) {
            return;
        }
        tmp.set(0);
        Z(pos, tmp);
        DataBlockIterator cols = V.columnsIterator();
        DoubleSeqCursor.OnMutable cell = tmp.cursor();
        while (cols.hasNext()) {
            cols.next().addAY(cell.getAndNext(), tmp);
        }
    }

    @Override
    public void XpZd(int pos, DataBlock x, double d) {
        DataWindow cur = x.left();
        for (int i = 0; i < loadings.length; ++i) {
            loadings[i].XpZd(pos, cur.next(dim[i]), d);
        }
    }

}
