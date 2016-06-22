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
package ec.tstoolkit.stats;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.eco.RegModel;
import ec.tstoolkit.maths.matrices.Householder;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.maths.matrices.UpperTriangularMatrix;

/**
 * Augmented Dickey-Fuller test
 *
 * @author Jean Palate
 */
public class ADFTest {

    private int k = 1; // number of lags. 
    private boolean cnt, trend;
    private Matrix x;
    private DataBlock y, b, e;
    private double t;

    public void test(IReadDataBlock data) {
        createVariables(data);
        Householder qr = new Householder(true);
        qr.decompose(x);
        qr.leastSquares(y, b, e);
        int nlast=b.getLength()-1;
        double ssq = e.ssq();
        double val = b.get(nlast);
        double std =   Math.abs(Math.sqrt(ssq / e.getLength())/qr.getRDiagonal().get(nlast));
        t = val / std;

        // for testing purposes
        double sig = ssq / e.getLength();
        Matrix bvar;
        Matrix u = UpperTriangularMatrix.inverse(qr.getR());
        bvar = SymmetricMatrix.XXt(u);
        bvar.mul(sig);
        double std2 = Math.sqrt(bvar.get(nlast, nlast));
    }

    /**
     * @return The number of lags taken into account. 1 (default) for simple
     * Dickey-Fuller test.
     */
    public int getK() {
        return k;
    }

    /**
     * @param k The number of lags to take into account. Must be greater or
     * equal to 1
     */
    public void setK(int k) {
        if (k < 1) {
            throw new java.lang.IllegalArgumentException("k should be greater or equal to 1");
        }
        this.k = k;
    }

    private void createVariables(IReadDataBlock data) {
        //
        int ndata = data.getLength();
        int ncols = k;
        if (cnt) {
            ++ncols;
        }
        if (trend) {
            ++ncols;
        }
        x = new Matrix(ndata - k, ncols);
        DataBlock all = new DataBlock(data);
        DataBlockIterator columns = x.columns();
        columns.end();
        DataBlock col = columns.getData();
        col.copy(all.extract(k - 1, ndata - k));
        columns.previous();
        all.difference();
        y = all.drop(k, 0);
        for (int i = 1; i < k; ++i) {
            col.copy(all.extract(k - i, ndata - k));
            columns.previous();
        }
        if (cnt) {
            col.set(1);
            columns.previous();
        }
        if (trend) {
            col.set(t -> t);
        }
        b = new DataBlock(ncols);
        e = new DataBlock(ndata - k - ncols);
    }

    /**
     * @return the x
     */
    public Matrix getX() {
        return x;
    }

    /**
     * @return the y
     */
    public DataBlock getY() {
        return y;
    }

    /**
     * @return the b
     */
    public DataBlock getB() {
        return b;
    }

    /**
     * @return the e
     */
    public DataBlock getE() {
        return e;
    }

    /**
     * @return the t
     */
    public double getT() {
        return t;
    }

    /**
     * @return the cnt
     */
    public boolean isConstant() {
        return cnt;
    }

    /**
     * @param cnt the cnt to set
     */
    public void setConstant(boolean cnt) {
        this.cnt = cnt;
    }

    /**
     * @return the trend
     */
    public boolean isTrend() {
        return trend;
    }

    /**
     * @param trend the trend to set
     */
    public void setTrend(boolean trend) {
        this.trend = trend;
    }
}
