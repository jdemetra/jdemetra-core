/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.modelling.regression;

import demetra.timeseries.regression.SwitchOutlier;
import jdplus.data.DataBlock;
import jdplus.math.linearfilters.BackFilter;
import jdplus.math.linearfilters.RationalBackFilter;
import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.TsPeriod;
import java.time.LocalDateTime;
import jdplus.math.matrices.Matrix;
import demetra.timeseries.TimeSeriesInterval;

/**
 *
 * @author palatej
 */
public class SwitchOutlierFactory implements IOutlierFactory {

    public static final SwitchOutlierFactory FACTORY = new SwitchOutlierFactory();

    private SwitchOutlierFactory() {
    }

    @Override
    public SwitchOutlier make(LocalDateTime position) {
        return new SwitchOutlier(position);
    }

    @Override
    public void fill(int outlierPosition, DataBlock buffer) {
        buffer.set(outlierPosition, 1);
        buffer.set(outlierPosition + 1, -1);
    }

    @Override
    public FilterRepresentation getFilterRepresentation() {
        return new FilterRepresentation(new RationalBackFilter(
                BackFilter.D1, BackFilter.ONE, 0), 0);
    }

    @Override
    public int excludingZoneAtStart() {
        return 0;
    }

    @Override
    public int excludingZoneAtEnd() {
        return 1;
    }

    @Override
    public String getCode() {
        return SwitchOutlier.CODE;
    }

}

class WOFactory implements RegressionVariableFactory<SwitchOutlier> {

    static WOFactory FACTORY = new WOFactory();

    private WOFactory() {
    }

    @Override
    public boolean fill(SwitchOutlier var, TsPeriod start, Matrix buffer) {
        TsPeriod p = start.withDate(var.getPosition());
        set(buffer.column(0), start.until(p));
        return true;
    }

    @Override
    public <P extends TimeSeriesInterval<?>, D extends TimeSeriesDomain<P>>  boolean fill(SwitchOutlier var, D domain, Matrix buffer) {
        set(buffer.column(0), (int) domain.indexOf(var.getPosition()));
        return true;
    }

    private void set(DataBlock z, int opos) {
        int last = z.length() - 1;
        if (opos < -1 || opos > last) {
            return;
        } else if (opos == -1) {
            z.set(0, -1);
        } else if (opos == last) {
            z.set(last, 1);
        } else {
            z.set(opos, 1);
            z.set(opos + 1, -1);
        }
    }
}
