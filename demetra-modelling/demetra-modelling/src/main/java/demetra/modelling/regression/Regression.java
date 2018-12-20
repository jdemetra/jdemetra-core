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
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class Regression {

    private final Map< Class<? extends ITsVariable>, RegressionVariableFactory> FACTORIES
            = new HashMap<>();

    public <V extends ITsVariable, W extends V> boolean register(Class<W> wclass, RegressionVariableFactory<V> factory) {
        synchronized (FACTORIES) {
            if (FACTORIES.containsKey(wclass)) {
                return false;
            }
            FACTORIES.put(wclass, factory);
            return true;
        }
    }

    public <V extends ITsVariable> boolean unregister(Class<V> vclass) {
        synchronized (FACTORIES) {
            RegressionVariableFactory removed = FACTORIES.remove(vclass);
            return removed != null;
        }
    }

    static {
        synchronized (FACTORIES) {
            // Outliers
            FACTORIES.put(AdditiveOutlier.class, AOFactory.FACTORY);
            FACTORIES.put(LevelShift.class, LSFactory.FACTORY);
            FACTORIES.put(TransitoryChange.class, TCFactory.FACTORY);
            FACTORIES.put(SwitchOutlier.class, WOFactory.FACTORY);
            FACTORIES.put(PeriodicOutlier.class, SOFactory.FACTORY);

            // Trading Days
            FACTORIES.put(LengthOfPeriod.class, LPFactory.FACTORY);
            FACTORIES.put(GenericTradingDaysVariable.class, TDFactory.FACTORY);
            FACTORIES.put(StockTradingDays.class, StockTDFactory.FACTORY);

            // Moving holidays
            FACTORIES.put(EasterVariable.class, EasterFactory.FACTORY);
            FACTORIES.put(JulianEasterVariable.class, JulianEasterFactory.FACTORY);

            // Others
            FACTORIES.put(Ramp.class, RampFactory.FACTORY);
            FACTORIES.put(InterventionVariable.class, IVFactory.FACTORY);
            FACTORIES.put(PeriodicDummies.class, PeriodicDummiesFactory.FACTORY);
            FACTORIES.put(PeriodicContrasts.class, PeriodicContrastsFactory.FACTORY);
            FACTORIES.put(TrigonometricVariables.class, TrigonometricVariablesFactory.FACTORY);

            FACTORIES.put(TsVariable.class, TsVariableFactory.FACTORY);
            FACTORIES.put(UserVariable.class, TsVariableFactory.FACTORY);
            FACTORIES.put(UserMovingHoliday.class, TsVariableFactory.FACTORY);
            FACTORIES.put(TsVariables.class, TsVariablesFactory.FACTORY);
            FACTORIES.put(UserVariables.class, TsVariablesFactory.FACTORY);
            FACTORIES.put(UserTradingDays.class, TsVariablesFactory.FACTORY);
        }
    }

    public <D extends TimeSeriesDomain> Matrix matrix(@Nonnull D domain, @Nonnull ITsVariable... vars) {
        int nvars = ITsVariable.dim(vars);
        int nobs = domain.length();
        Matrix M = Matrix.make(nobs, nvars);

        MatrixWindow wnd = M.left(0);
        if (domain instanceof TsDomain) {
            TsPeriod start = ((TsDomain) domain).getStartPeriod();
            for (int i = 0, j = 0; i < vars.length; ++i) {
                ITsVariable v = vars[i];
                MatrixWindow cur = wnd.left(v.dim());
                RegressionVariableFactory factory = FACTORIES.get(v.getClass());
                if (factory != null) {
                    factory.fill(v, start, cur);
                }
            }
        } else {
            for (int i = 0, j = 0; i < vars.length; ++i) {
                ITsVariable v = vars[i];
                MatrixWindow cur = wnd.left(v.dim());
                RegressionVariableFactory factory = FACTORIES.get(v.getClass());
                if (factory != null) {
                    factory.fill(v, domain, cur);
                }
            }
        }
        return M;
    }

    public <D extends TimeSeriesDomain> DataBlock x(@Nonnull D domain, @Nonnull ITsVariable vars) {
        if (vars.dim() != 1) {
            throw new IllegalArgumentException();
        }
        Matrix m = matrix(domain, vars);
        return DataBlock.ofInternal(m.getStorage());
    }

}
