/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.regression;

import demetra.data.DataBlock;
import demetra.maths.linearfilters.BackFilter;
import demetra.maths.linearfilters.RationalBackFilter;
import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.TsPeriod;
import java.time.LocalDateTime;
import demetra.maths.matrices.Matrix;

/**
 *
 * @author palatej
 */
public class AdditiveOutlierFactory implements IOutlierFactory {

    public static final AdditiveOutlierFactory FACTORY = new AdditiveOutlierFactory();

    private AdditiveOutlierFactory() {
    }

    @Override
    public AdditiveOutlier make(LocalDateTime position) {
        return new AdditiveOutlier(position);
    }

    @Override
    public void fill(int outlierPosition, DataBlock buffer) {
        buffer.set(outlierPosition, 1);
    }

    @Override
    public FilterRepresentation getFilterRepresentation() {
        return new FilterRepresentation(new RationalBackFilter(
                BackFilter.ONE, BackFilter.ONE, 0), 0);
    }

    @Override
    public int excludingZoneAtStart() {
        return 0;
    }

    @Override
    public int excludingZoneAtEnd() {
        return 0;
    }

    @Override
    public String getCode() {
        return AdditiveOutlier.CODE;
    }
}

class AOFactory implements RegressionVariableFactory<AdditiveOutlier> {

    static AOFactory FACTORY=new AOFactory();

    private AOFactory(){}

    @Override
    public boolean fill(AdditiveOutlier var, TsPeriod start, Matrix buffer) {
        TsPeriod p = start.withDate(var.getPosition());
        int opos = start.until(p);
        if (opos >= 0 && opos < buffer.getRowsCount()) {
            buffer.set(opos, 0, 1);
        }
        return true;
    }

    @Override
    public <D extends TimeSeriesDomain> boolean fill(AdditiveOutlier var, D domain, Matrix buffer) {
        long pos = domain.indexOf(var.getPosition());
        if (pos >= 0) {
            buffer.set((int) pos, 0, 1);
        }
        return true;
    }
}
