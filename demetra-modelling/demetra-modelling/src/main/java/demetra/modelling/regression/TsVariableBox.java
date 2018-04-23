/*
 * Copyright 2013 National Bank of Belgium
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
package demetra.modelling.regression;

import demetra.data.DataBlock;
import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.TsDomain;
import java.util.List;
import javax.annotation.Nonnull;

/**
 *
 * @author Jean Palate
 * @param <D>
 */
public abstract class TsVariableBox<D extends TimeSeriesDomain<?>> implements ITsVariable<D> {

    private final ITsVariable<D> var;
    private final String name;

    protected TsVariableBox(@Nonnull ITsVariable var, @Nonnull String name) {
        this.var = var;
        this.name = name;
    }

    @Override
    public void data(D domain, List<DataBlock> data) {
        var.data(domain, data);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription(D context) {
        return var.getDescription(context);
    }

    @Override
    public int getDim() {
        return var.getDim();
    }

    @Override
    public String getItemDescription(int idx, D context) {
        return var.getItemDescription(idx, context);
    }

    ITsVariable<D> getCore() {
        return var;
    }

    public static ITradingDaysVariable tradingDays(ITsVariable var) {
        return new TradingDays(var, null);
    }

    static class TradingDays extends TsVariableBox<TsDomain> implements ITradingDaysVariable {

        TradingDays(ITsVariable<TsDomain> var, String name) {
            super(var, name);
        }

        @Override
        public TradingDays rename(String newName) {
            return new TradingDays(getCore(), newName);
        }
    }

    public static ILengthOfPeriodVariable leapYear(ITsVariable var) {
        return new LeapYear(var, null);
    }

    static class LeapYear extends TsVariableBox<TsDomain> implements ILengthOfPeriodVariable {

        LeapYear(ITsVariable var, String name) {
            super(var, name);
        }

        @Override
        public LeapYear rename(String newName) {
            return new LeapYear(getCore(), newName);
        }
    }

    public static IMovingHolidayVariable movingHoliday(ITsVariable var) {
        return new MovingHoliday(var, null);
    }

    static class MovingHoliday extends TsVariableBox<TsDomain> implements IMovingHolidayVariable {

        MovingHoliday(ITsVariable var, String name) {
            super(var, name);
        }

        @Override
        public MovingHoliday rename(String newName) {
            return new MovingHoliday(getCore(), newName);
        }
    }
}
