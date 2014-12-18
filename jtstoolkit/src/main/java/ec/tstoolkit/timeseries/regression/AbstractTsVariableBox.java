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
package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class AbstractTsVariableBox {

    private final ITsVariable var;

    protected AbstractTsVariableBox(ITsVariable var) {
        this.var = var;
    }

    @Deprecated
    public void data(TsDomain domain, List<DataBlock> data, int start) {
        var.data(domain, data, start);
    }

    public void data(TsDomain domain, List<DataBlock> data) {
        var.data(domain, data);
    }

    public TsDomain getDefinitionDomain() {
        return var.getDefinitionDomain();
    }

    public TsFrequency getDefinitionFrequency() {
        return var.getDefinitionFrequency();
    }

    public String getDescription() {
        return var.getDescription();
    }

    public int getDim() {
        return var.getDim();
    }

    public String getItemDescription(int idx) {
        return var.getItemDescription(idx);
    }

    public boolean isSignificant(TsDomain domain) {
        return var.isSignificant(domain);
    }
    
    public static ITradingDaysVariable tradingDays(ITsVariable var){
        return new TradingDays(var);
    }

    static class TradingDays extends AbstractTsVariableBox implements ITradingDaysVariable{
        TradingDays(ITsVariable var){
            super(var);
        }
    }
    
    public static ILengthOfPeriodVariable leapYear(ITsVariable var){
        return new LeapYear(var);
    }

    static class LeapYear extends AbstractTsVariableBox implements ILengthOfPeriodVariable{
        LeapYear(ITsVariable var){
            super(var);
        }
    }
    
    public static IMovingHolidayVariable movingHoliday(ITsVariable var){
        return new MovingHoliday(var);
    }

    static class MovingHoliday extends AbstractTsVariableBox implements IMovingHolidayVariable{
        MovingHoliday(ITsVariable var){
            super(var);
        }
    }
}
