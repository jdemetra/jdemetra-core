/*
 * Copyright 2016 National Bank of Belgium
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
package jdplus.ssf.implementations;

import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.data.DataWindow;
import jdplus.maths.matrices.MatrixWindow;
import jdplus.maths.matrices.QuadraticForm;
import demetra.data.DoubleSeqCursor;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.univariate.ISsfMeasurement;
import jdplus.maths.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public class ExternalEffects implements ISsfLoading {


    private final ISsfLoading loading;
    private final FastMatrix data;
    private final int nm, nx;
    private final DataBlock tmp;

    ExternalEffects(final int dim, final ISsfLoading loading, final FastMatrix data) {
        this.data = data;
        this.loading = loading;
        nm = dim;
        nx = data.getColumnsCount();
        tmp = DataBlock.make(nx);
    }

    @Override
    public boolean isTimeInvariant() {
        return false;
    }

    @Override
    public void Z(int pos, DataBlock z) {
        DataWindow range = z.window(0, nm);
        loading.Z(pos, range.get());
        range.next(nx).copy(data.row(pos));
    }

    @Override
    public double ZX(int pos, DataBlock x) {
        DataWindow range = x.window(0, nm);
        double r = loading.ZX(pos, range.get());
        return r + range.next(nx).dot(data.row(pos));
    }

    @Override
    public double ZVZ(int pos, FastMatrix V) {
        MatrixWindow v = V.topLeft(nm, nm);
        double v00 = loading.ZVZ(pos, v);
        v.vnext(nx);
        tmp.set(0);
        double v01 = tmp.dot(data.row(pos));
        loading.ZM(pos, v, tmp);
        v.hnext(nx);
        double v11 = QuadraticForm.apply(v, data.row(pos));
        return v00 + 2 * v01 + v11;
    }

    @Override
    public void VpZdZ(int pos, FastMatrix V, double d) {
        MatrixWindow v = V.topLeft(nm, nm);
        loading.VpZdZ(pos, v, d);
        MatrixWindow vtmp = v.clone();
        vtmp.hnext(nx);
        v.vnext(nx);
        DataBlockIterator rows=v.rowsIterator();
        DataBlock xrow=data.row(pos);
        DoubleSeqCursor cell = xrow.cursor();
        while (rows.hasNext()){
            loading.XpZd(pos, rows.next(), d*cell.getAndNext());
        }
        vtmp.copy(v.transpose());
        v.hnext(nx);
        v.addXaXt(d, xrow);
    }

    @Override
    public void XpZd(int pos, DataBlock x, double d) {
        DataWindow range = x.window(0, nm);
        loading.XpZd(pos, range.get(), d);
        range.next(nx).addAY(d, data.row(pos));
    }

}