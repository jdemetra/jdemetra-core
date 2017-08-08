/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.ssf.univariate;

import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.ResultsRange;
import demetra.data.DataBlockIterator;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.data.DoubleReader;

/**
 *
 * @author Jean Palate
 */
public class DisturbanceSmoother {

    private ISsf ssf;
    private ISsfDynamics dynamics;
    private ISsfMeasurement measurement;
    private IDisturbanceSmoothingResults srslts;
    private IFilteringResults frslts;

    private double err, errVariance, esm, esmVariance, h;
    private DataBlock K, R, U;
    private Matrix N, UVar, S;
    private boolean missing, res, calcvar = true;
    private int pos, stop;
    // temporary
    private DataBlock tmp;
    private double c, v;

    public boolean process(ISsf ssf, ISsfData data) {
        this.ssf=ssf;
        if (ssf.getInitialization().isDiffuse()) {
            return false;
        }
        OrdinaryFilter filter = new OrdinaryFilter();
        DefaultFilteringResults fresults = DefaultFilteringResults.light();
        if (!filter.process(ssf, data, fresults)) {
            return false;
        }
        return process(ssf, 0, data.length(), fresults);
    }

    public boolean process(ISsf ssf, DefaultFilteringResults results) {
        this.ssf=ssf;
        if (ssf.getInitialization().isDiffuse()) {
            return false;
        }
        ResultsRange range = results.getRange();
        return process(ssf, range.getStart(), range.getEnd(), results);
    }

    public boolean process(ISsf ssf, int start, int end, IFilteringResults results) {
        this.ssf=ssf;
        IDisturbanceSmoothingResults sresults;
        boolean hasErrors = ssf.getMeasurement().hasErrors();
        if (calcvar) {
            sresults = DefaultDisturbanceSmoothingResults.full(hasErrors);
        } else {
            sresults = DefaultDisturbanceSmoothingResults.light(hasErrors);
        }

        return process(ssf, start, end, results, sresults);
    }

    public boolean process(ISsf ssf, ISsfData data, IDisturbanceSmoothingResults sresults, final int stop) {
        this.ssf=ssf;
        OrdinaryFilter filter = new OrdinaryFilter();
        DefaultFilteringResults fresults = DefaultFilteringResults.light();
        if (!filter.process(ssf, data, fresults)) {
            return false;
        }
        return process(ssf, stop, data.length(), fresults);
    }

    public boolean process(ISsf ssf, final int start, final int end, IFilteringResults results, IDisturbanceSmoothingResults sresults) {
        this.ssf=ssf;
        frslts = results;
        srslts = sresults;
        stop = start;
        pos = end;
        initFilter(ssf);
        initSmoother(ssf);
        while (--pos >= stop) {
            loadInfo();
            if (iterate()) {
                srslts.saveSmoothedTransitionDisturbances(pos, U, UVar == null ? null : UVar);
                if (res) {
                    srslts.saveSmoothedMeasurementDisturbance(pos, esm, esmVariance);
                }
            }
        }
        return true;
    }

    public boolean resume(final int start) {
        stop = start;
        while (pos >= stop) {
            loadInfo();
            if (iterate()) {
                srslts.saveSmoothedTransitionDisturbances(pos, U, UVar);
                if (res) {
                    srslts.saveSmoothedMeasurementDisturbance(pos, esm, esmVariance);
                }
            }
            pos--;
        }
        return true;
    }

    public IDisturbanceSmoothingResults getResults() {
        return srslts;
    }

    public DataBlock getFinalR() {
        return R;
    }

    public Matrix getFinalN() {
        return N;
    }

    private void initSmoother(ISsf ssf) {
        int dim = ssf.getStateDim();
        int resdim = dynamics.getInnovationsDim();

        R = DataBlock.make(dim);
        K = DataBlock.make(dim);
        U = DataBlock.make(resdim);
        S = Matrix.make(dim, resdim);
        if (calcvar) {
            N = Matrix.square(dim);
            tmp = DataBlock.make(dim);
            UVar = Matrix.square(resdim);
            if (measurement.isTimeInvariant()) {
                h = measurement.errorVariance(0);
            }
        }
        if (dynamics.isTimeInvariant()) {
            dynamics.S(0, S);
        }
    }

    private void loadInfo() {
        err = frslts.error(pos);
        missing = !Double.isFinite(err);
        if (!missing) {
            errVariance = frslts.errorVariance(pos);
            K.setAY(1 / errVariance, frslts.M(pos));
            dynamics.TX(pos, K);
        }
        if (!dynamics.isTimeInvariant()) {
            dynamics.S(pos, S);
        }
        if (!measurement.isTimeInvariant()) {
            h = measurement.errorVariance(pos);
        }
    }

    private boolean iterate() {
        iterateR();
        if (calcvar) {
            iterateN();
        }
        // updates the smoothed disturbances
        if (res) {
            esm = c * h;
        }
        dynamics.XS(pos, R, U);
        if (calcvar) {
            if (res) {
                esmVariance = h - h * h * v;
            }
            // v(U) = I-S'NS
            SymmetricMatrix.XtSX(N, S, UVar);
            UVar.chs();
            UVar.diagonal().add(1);
        }
        return true;
    }
    // 

    /**
     *
     */
    private void iterateN() {
        if (!missing && errVariance != 0) {
            // N(t-1) = Z'(t)*Z(t)/f(t) + L'(t)*N(t)*L(t)
            // = Z'(t)*Z(t)/f(t) + (T' - Z'K')N(T - KZ)
            // =  Z'(t)*Z(t)(1/f(t) + K'NK) + T'NT - <T'NKZ>
            // 1. NK 
            tmp.product(N.rowsIterator(), K);
            // 2. v
            v = 1 / errVariance + tmp.dot(K);
            // 3. T'NK
            dynamics.XT(pos, tmp);
            // TNT
            tvt(N);
            measurement.VpZdZ(pos, N, v);
            subZ(N.rowsIterator(), tmp);
            subZ(N.columnsIterator(), tmp);
        } else {
            tvt(N);
        }
        SymmetricMatrix.reenforceSymmetry(N);
    }

    /**
     *
     */
    private void iterateR() {
        // R(t-1)=(v/f + R(t)*K)Z + R(t)*T
        // R(t-1)=esm*Z +  R(t)*T
        if (!missing && errVariance != 0) {
            // RT
            c = (err / errVariance - R.dot(K));
            dynamics.XT(pos, R);
            measurement.XpZd(pos, R, c);
        } else {
            dynamics.XT(pos, R);
            c = Double.NaN;
        }
    }

    private void initFilter(ISsf ssf) {
        dynamics = ssf.getDynamics();
        measurement = ssf.getMeasurement();
        res = measurement.hasErrors();
    }

    public void setCalcVariances(boolean b) {
        calcvar = b;
    }

    public boolean isCalcVariances() {
        return calcvar;
    }

    private void tvt(Matrix N) {
        N.columns().forEach(col -> dynamics.XT(pos, col));
        N.rows().forEach(row -> dynamics.XT(pos, row));
    }

    private void subZ(DataBlockIterator rows, DataBlock b) {
        DoubleReader x = b.reader();
        while (rows.hasNext()) {
            double cur = x.next();
            if (cur != 0) {
                measurement.XpZd(pos, rows.next(), -cur);
            }
        }
    }

    public DataBlock firstSmoothedState() {
        int n = ssf.getStateDim();
        // initial state
        DataBlock a = DataBlock.make(n);
        Matrix Pf0 = Matrix.square(n);
        ssf.getInitialization().a0(a);
        ssf.getInitialization().Pf0(Pf0);
        // stationary initialization
        a.addProduct(R, Pf0.columnsIterator());
        return a;
    }
}
