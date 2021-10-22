/*
 * Copyright 2013-2014 National Bank of Belgium
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
package jdplus.ssf.univariate;

import jdplus.ssf.DataResults;
import jdplus.ssf.StateInfo;
import jdplus.ssf.StateStorage;
import demetra.data.DoubleSeq;
import jdplus.data.DataBlock;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.MatrixStorage;
import jdplus.ssf.DataBlockResults;
import jdplus.ssf.MatrixResults;

/**
 * Contains at position t: R(t-1), N(t-1), u(t), M(t)
 * (R(n)=0, N(n)=0)
 * @author Jean Palate
 */
public class DefaultSmoothingResults extends StateStorage implements ISmoothingResults {

    private final DataResults e, f;
    private final MatrixResults N;
    private final DataBlockResults R;

    protected DefaultSmoothingResults(final boolean cov, final boolean err) {
        super(StateInfo.Smoothed, cov);
        if (err) {
            e = new DataResults();
            f = new DataResults();
            N = new MatrixResults();
            R = new DataBlockResults();
        } else {
            e = null;
            f = null;
            N = null;
            R = null;
        }
    }

    @Override
    public void prepare(int dim, int start, int end) {
        super.prepare(dim, start, end);

        if (e != null) {
            e.prepare(start, end);
            f.prepare(start, end);
            N.prepare(dim, start, end);
            R.prepare(dim, start, end);
        }
    }

    @Override
    public void rescaleVariances(double factor) {
        super.rescaleVariances(factor);
        if (f != null) {
            f.rescale(factor);
            N.rescale(factor);
            e.rescale(factor);
            R.rescale(factor);
        }
    }

    public static DefaultSmoothingResults full() {
        return new DefaultSmoothingResults(true, true);
    }

    public static DefaultSmoothingResults light() {
        return new DefaultSmoothingResults(false, false);
    }

    @Override
    public void saveSmoothation(int t, double err, double v) {
        if (e == null) {
            return;
        }
        e.save(t, err);
        f.save(t, v);
    }

    @Override
    public void saveR(int pos, DataBlock r, FastMatrix rvar) {
        if (N == null){
            return;
        }
        R.save(pos, r);
        N.save(pos, rvar);
    }

    public DoubleSeq errors() {
        return e == null ? null : e.asDoublesReader(true);
    }

    public DoubleSeq errorVariances() {
        return f == null ? null : f.asDoublesReader(true);
    }

    @Override
    public DoubleSeq R(int pos) {
        if (R == null) {
            throw new java.lang.UnsupportedOperationException();
        }
        return R.datablock(pos);
    }

    @Override
    public FastMatrix RVariance(int pos) {
        if (N == null) {
            throw new java.lang.UnsupportedOperationException();
        }
        return N.matrix(pos);
    }

    @Override
    public double smoothation(int pos) {
        if (e == null) {
            throw new java.lang.UnsupportedOperationException();
        }
        return e.get(pos);
    }

    @Override
    public DoubleSeq smoothations() {
        if (e == null) {
            throw new java.lang.UnsupportedOperationException();
        }
        return e.all();
    }

    @Override
    public double smoothationVariance(int pos) {
        if (f == null) {
            throw new java.lang.UnsupportedOperationException();
        }
        return f.get(pos);
    }

}
