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
public class LevelShiftFactory implements IOutlierFactory {

    public static final LevelShiftFactory FACTORY_ZEROENDED = new LevelShiftFactory(true),
            FACTORY_ZEROSTARTED = new LevelShiftFactory(false);

    private final boolean zeroEnded;

    private LevelShiftFactory(boolean zeroEnded) {
        this.zeroEnded = zeroEnded;
    }

    @Override
    public LevelShift make(LocalDateTime position) {
        return new LevelShift(position, zeroEnded);
    }

    @Override
    public void fill(int xpos, DataBlock buffer) {
        int n = buffer.length();
        double Zero = zeroEnded ? -1 : 0, One = zeroEnded ? 0 : 1;
        buffer.range(0, xpos).set(Zero);
        buffer.range(xpos, n).set(One);
    }

    @Override
    public IOutlierFactory.FilterRepresentation getFilterRepresentation() {
        return new IOutlierFactory.FilterRepresentation(new RationalBackFilter(
                BackFilter.ONE, BackFilter.D1, 0), zeroEnded ? -1 : 0);
    }

    @Override
    public int excludingZoneAtStart() {
        return 1;
    }

    @Override
    public int excludingZoneAtEnd() {
        return 1;
    }

    @Override
    public String getCode() {
        return LevelShift.CODE;
    }
}


class LSFactory implements RegressionVariableFactory<LevelShift> {

    static LSFactory FACTORY=new LSFactory();

    private LSFactory(){}

    @Override
    public boolean fill(LevelShift var, TsPeriod start, Matrix m) {
        TsPeriod p = start.withDate(var.getPosition());
        fill(var, start.until(p), m.column(0));
        return true;
    }

    @Override
    public <D extends TimeSeriesDomain> boolean fill(LevelShift var, D domain, Matrix m) {
        fill(var, domain.indexOf(var.getPosition()), m.column(0));
        return true;
    }

    private void fill(LevelShift var, int xpos, DataBlock buffer) {
        double Zero = var.isZeroEnded() ? -1 : 0, One = var.isZeroEnded() ? 0 : 1;
        int n = buffer.length();
        if (xpos < 0) {
            buffer.set(One);
        } else {
            int lpos = xpos >= 0 ? xpos : -xpos;
            if (lpos >= n) {
                buffer.set(Zero);
            } else {
                buffer.range(0, lpos).set(Zero);
                buffer.range(lpos, n).set(One);
            }
        }
    }
}

