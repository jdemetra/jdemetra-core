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
package demetra.ssf.implementations;

import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.data.DataWindow;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.MatrixWindow;
import demetra.maths.matrices.QuadraticForm;
import demetra.ssf.univariate.ISsfMeasurement;
import demetra.data.DoubleReader;

/**
 *
 * @author Jean Palate
 */
public class ExternalEffects implements ISsfMeasurement {


    private final ISsfMeasurement m;
    private final Matrix data;
    private final int nm, nx;
    private final DataBlock tmp;

    ExternalEffects(final int dim, final ISsfMeasurement m, final Matrix data) {
        this.data = data;
        this.m = m;
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
        m.Z(pos, range.get());
        range.next(nx).copy(data.row(pos));
    }

    @Override
    public boolean hasErrors() {
        return m.hasErrors();
    }

    @Override
    public boolean hasError(int pos) {
        return m.hasError(pos);
    }

    @Override
    public double errorVariance(int pos) {
        return m.errorVariance(pos);
    }

    @Override
    public double ZX(int pos, DataBlock x) {
        DataWindow range = x.window(0, nm);
        double r = m.ZX(pos, range.get());
        return r + range.next(nx).dot(data.row(pos));
    }

    @Override
    public double ZVZ(int pos, Matrix V) {
        MatrixWindow v = V.topLeft(nm, nm);
        double v00 = m.ZVZ(pos, v);
        v.vnext(nx);
        tmp.set(0);
        double v01 = tmp.dot(data.row(pos));
        m.ZM(pos, v, tmp);
        v.hnext(nx);
        double v11 = QuadraticForm.apply(v, data.row(pos));
        return v00 + 2 * v01 + v11;
    }

    @Override
    public void VpZdZ(int pos, Matrix V, double d) {
        MatrixWindow v = V.topLeft(nm, nm);
        m.VpZdZ(pos, v, d);
        MatrixWindow vtmp = v.clone();
        vtmp.hnext(nx);
        v.vnext(nx);
        DataBlockIterator rows=v.rowsIterator();
        DataBlock xrow=data.row(pos);
        DoubleReader cell = xrow.reader();
        while (rows.hasNext()){
            m.XpZd(pos, rows.next(), d*cell.next());
        }
        vtmp.copy(v.transpose());
        v.hnext(nx);
        v.addXaXt(d, xrow);
    }

    @Override
    public void XpZd(int pos, DataBlock x, double d) {
        DataWindow range = x.window(0, nm);
        m.XpZd(pos, range.get(), d);
        range.next(nx).addAY(d, data.row(pos));
    }

}
