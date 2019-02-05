/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.ssf.univariate;

import demetra.ssf.ISsfLoading;
import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.maths.linearfilters.ILinearProcess;
import demetra.maths.matrices.Matrix;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.ResultsRange;
import demetra.data.DoubleReader;
import demetra.data.DoubleSequence;
import demetra.ssf.State;

/**
 *
 * @author Jean Palate
 */
public class FastFilter implements ILinearProcess {

    private final DefaultFilteringResults frslts;
    private final ISsf ssf;
    private final ISsfLoading loading;
    private final ISsfDynamics dynamics;
    private final int start, end;
    private Matrix states;
    // temporaries
    private DataBlock tmp;
    private DataBlockIterator scols;

    public FastFilter(ISsf ssf, DefaultFilteringResults frslts, ResultsRange range) {
        this.ssf = ssf;
        this.frslts = frslts;
        loading = ssf.measurement().loading();
        dynamics = ssf.dynamics();
        start = range.getStart();
        end = range.getEnd();
    }

    public boolean filter(Matrix x) {
        if (end - start < x.getRowsCount()) {
            return false;
        }
        int dim = ssf.getStateDim();
        states = Matrix.make(dim, x.getColumnsCount());
        prepareTmp();
        DataBlockIterator rows = x.rowsIterator();
        int pos = start;
        while (pos < end && rows.hasNext()) {
            iterate(pos++, rows.next());
        }
        return true;
    }

    private void prepareTmp() {
        int nvars = states.getColumnsCount();
        tmp = DataBlock.make(nvars);
        scols = states.columnsIterator();
    }

    private void iterate(int i, DataBlock row) {
        // retrieve the current information
        boolean missing = !Double.isFinite(frslts.error(i));
        double f = frslts.errorVariance(i);
        loading.ZM(i, states, tmp);
        row.sub(tmp);
        // update the states
        if (f > 0) {
            if (!missing) {
                DataBlock C = frslts.M(i);
                // process by column
                scols.reset();
                DoubleReader r = row.reader();
                while (scols.hasNext()) {
                    scols.next().addAY(r.next() / f, C);
                }
            }
            row.mul(1 / Math.sqrt(f));
        } else {
            row.apply(q -> Math.abs(q) > State.ZERO ? Double.NaN : 0);
        }
        dynamics.TM(i, states);
        //  
    }

    @Override
    public boolean transform(DoubleSequence in, DataBlock out) {
        if (in.length() > end - start) {
            return false;
        }
        int dim = ssf.getStateDim(), n = in.length();
        DataBlock state = DataBlock.make(dim);
        int pos = start, ipos = 0, opos = 0;
        do {
            boolean missing = !Double.isFinite(frslts.error(pos));
            if (!missing) {
                double f = frslts.errorVariance(pos);
                double e = in.get(ipos) - loading.ZX(pos, state);
                if (f > 0) {
                    out.set(opos++, e / Math.sqrt(f));
                    // update the state
                    DataBlock C = frslts.M(pos);
                    // process by column
                    state.addAY(e / f, C);
                } else {
                    out.set(opos++, Math.abs(e) > State.ZERO ? Double.NaN : 0);
                }
            }
            dynamics.TX(pos++, state);
        } while (++ipos < n);
        return true;
    }

    @Override
    public int getOutputLength(int inputLength) {
        int n = 0;
        int imax = start + inputLength;
        if (imax > end) {
            return -1;
        }
        for (int i = start; i < imax; ++i) {
            double e = frslts.error(i), v = frslts.errorVariance(i);
            if (Double.isFinite(e) && v != 0) {
                ++n;
            }
        }
        return n;
    }
}
