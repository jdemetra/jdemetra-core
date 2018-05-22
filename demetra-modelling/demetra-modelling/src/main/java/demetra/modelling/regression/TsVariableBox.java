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
import demetra.modelling.ComponentType;
import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.TsDomain;
import java.util.List;
import java.util.Objects;
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
        return new TradingDays(var, ITradingDaysVariable.defaultName(var.getDim()));
    }
    
    static class TradingDays extends TsVariableBox<TsDomain> implements ITradingDaysVariable {
        
        TradingDays(@Nonnull ITsVariable<TsDomain> var, @Nonnull String name) {
            super(var, name);
        }
        
        @Override
        public TradingDays rename(String newName) {
            return new TradingDays(getCore(), newName);
        }
    }
    
    public static ILengthOfPeriodVariable leapYear(ITsVariable var) {
        return new LeapYear(var, ILengthOfPeriodVariable.NAME);
    }
    
    static class LeapYear extends TsVariableBox<TsDomain> implements ILengthOfPeriodVariable {
        
        LeapYear(@Nonnull ITsVariable var, @Nonnull String name) {
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
    
    public static IUserTsVariable<TsDomain> user(@Nonnull ITsVariable var, @Nonnull String name, @Nonnull ComponentType type) {
        return new User(var, name, type);
    }
    
    static class User extends TsVariableBox<TsDomain> implements IUserTsVariable<TsDomain> {
        
        private final ComponentType type;
        
        User(@Nonnull ITsVariable var, @Nonnull String name, ComponentType type) {
            super(var, name);
            this.type = type;
        }
        
        @Override
        public User rename(String newName) {
            return new User(getCore(), newName, type);
        }
        
        @Override
        public ComponentType getComponentType() {
            return type;
        }
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof TsVariableBox) {
            TsVariableBox x = (TsVariableBox) other;
            return x.var.equals(var);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.var);
        return hash;
    }
    
}
