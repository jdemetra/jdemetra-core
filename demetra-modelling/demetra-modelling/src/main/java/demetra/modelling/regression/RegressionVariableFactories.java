/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.regression;

import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.MatrixWindow;
import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.calendars.GenericTradingDays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class RegressionVariableFactories {

    private final Map< Class<? extends ITsVariableDefinition>, RegressionVariableFactory> FACTORIES
            = new HashMap<>();

    static {
        // Outliers
        FACTORIES.put(AdditiveOutlierDef.class, new AOFactory());
        FACTORIES.put(LevelShiftDef.class, new LSFactory());
        FACTORIES.put(TransitoryChangeDef.class, new TCFactory());

        // Trading Days
        FACTORIES.put(GenericTradingDaysDef.class, new TDFactory());

        // Moving holidays
    }

    public <D extends TimeSeriesDomain> Matrix variables(@Nonnull D domain, @Nonnull ITsVariableDefinition... vars) {
        int nvars = ITsVariableDefinition.dim(vars);
        int nobs = domain.length();
        Matrix M = Matrix.make(nobs, nvars);

        MatrixWindow wnd = M.left(0);
        if (domain instanceof TsDomain) {
            TsPeriod start = ((TsDomain) domain).getStartPeriod();
            for (int i = 0, j = 0; i < vars.length; ++i) {
                ITsVariableDefinition v = vars[i];
                MatrixWindow cur = wnd.left(v.dim());
                RegressionVariableFactory factory = FACTORIES.get(v.getClass());
                if (factory != null) {
                    factory.fill(v, start, cur);
                }
            }
        } else {
            for (int i = 0, j = 0; i < vars.length; ++i) {
                ITsVariableDefinition v = vars[i];
                MatrixWindow cur = wnd.left(v.dim());
                RegressionVariableFactory factory = FACTORIES.get(v.getClass());
                if (factory != null) {
                    factory.fill(v, domain, cur);
                }
            }
        }
        return M;
    }

}

class AOFactory implements RegressionVariableFactory<AdditiveOutlierDef> {

    @Override
    public boolean fill(AdditiveOutlierDef var, TsPeriod start, Matrix buffer) {
        TsPeriod p = start.withDate(var.getPosition());
        int opos = start.until(p);
        if (opos >= 0 && opos < buffer.getRowsCount()) {
            buffer.set(opos, 0, 1);
        }
        return true;
    }

    @Override
    public <D extends TimeSeriesDomain> boolean fill(AdditiveOutlierDef var, D domain, Matrix buffer) {
        long pos = domain.indexOf(var.getPosition());
        if (pos >= 0) {
            buffer.set((int) pos, 0, 1);
        }
        return true;
    }
}

class LSFactory implements RegressionVariableFactory<LevelShiftDef> {

    @Override
    public boolean fill(LevelShiftDef var, TsPeriod start, Matrix m) {
        TsPeriod p = start.withDate(var.getPosition());
        fill(var, start.until(p), m.column(0));
        return true;
    }

    @Override
    public <D extends TimeSeriesDomain> boolean fill(LevelShiftDef var, D domain, Matrix m) {
        fill(var, domain.indexOf(var.getPosition()), m.column(0));
        return true;
    }

    private void fill(LevelShiftDef var, int xpos, DataBlock buffer) {
        double Zero = var.isZeroEnded() ? -1 : 0, One = var.isZeroEnded() ? 0 : 1;
        int n = buffer.length();
        if (xpos == -1) {
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

class TCFactory implements RegressionVariableFactory<TransitoryChangeDef> {

    static double ZERO = 1e-15;

    @Override
    public boolean fill(TransitoryChangeDef var, TsPeriod start, Matrix m) {
        TsPeriod p = start.withDate(var.getPosition());
        fill(var, start.until(p), m.column(0));
        return true;
    }

    @Override
    public <D extends TimeSeriesDomain> boolean fill(TransitoryChangeDef var, D domain, Matrix m) {
        fill(var, domain.indexOf(var.getPosition()), m.column(0));
        return true;
    }

    public void fill(TransitoryChangeDef var, int xpos, DataBlock buffer) {
        double cur = 1;
        int n = buffer.length();
        for (int pos = xpos; pos < n; ++pos) {
            buffer.set(pos, cur);
            cur *= var.getRate();
            if (Math.abs(cur) < ZERO) {
                return;
            }
        }
    }

}

class TDFactory implements RegressionVariableFactory<GenericTradingDaysDef> {

    @Override
    public boolean fill(GenericTradingDaysDef var, TsPeriod start, Matrix buffer) {

        GenericTradingDays td;
        if (var.isContrast()) {
            td = GenericTradingDays.contrasts(var.getDayClustering());
        } else if (var.isNormalized()) {
            td = GenericTradingDays.normalized(var.getDayClustering());
        } else {
            td = GenericTradingDays.of(var.getDayClustering());
        }
        td.data(TsDomain.of(start, buffer.getRowsCount()), buffer.columnList());
        return true;
    }

    @Override
    public <D extends TimeSeriesDomain> boolean fill(GenericTradingDaysDef var, D domain, Matrix buffer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
