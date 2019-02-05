/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.ssf.univariate;

import demetra.ssf.UpdateInformation;
import demetra.ssf.DataResults;
import demetra.ssf.State;
import demetra.ssf.StateInfo;

/**
 *
 * @author Jean Palate
 */
public class FilteringErrors implements IFilteringResults {

    private final DataResults e, f;
    private final boolean normalized_;

    public FilteringErrors(boolean normalized) {
        normalized_ = normalized;
        e = new DataResults();
        f = new DataResults();
    }

    public boolean isNormalized() {
        return normalized_;
    }

    public void prepare(final int start, final int end) {
        e.prepare(start, end);
        f.prepare(start, end);
    }

    @Override
    public void save(final int t, final State state, final StateInfo info) {
    }

    @Override
    public void save(int t, UpdateInformation pe) {
        if (pe.isMissing()) {
            return;
        }
        double x = pe.get();
        double v = pe.getVariance();

        if (normalized_) {
            double s = Math.sqrt(v);
            e.save(t, x / s);
            f.save(t, s);
        } else {
            e.save(t, x);
            f.save(t, v);
        }
    }

    @Override
    public void clear() {
        e.clear();
        f.clear();
    }

}
