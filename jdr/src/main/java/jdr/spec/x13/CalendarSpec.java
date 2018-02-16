/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jdr.spec.x13;

import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;

/**
 *
 * @author Kristof Bayens
 */
public class CalendarSpec extends BaseRegArimaSpec {

    public CalendarSpec(RegArimaSpecification spec){
        super(spec);
    }

    public TradingDaysSpec getTradingDays(){
        return new TradingDaysSpec(core);
    }

    public void setTradingDays(TradingDaysSpec spec){

    }

    public EasterSpec getEaster(){
        return new EasterSpec(core);
    }

    public void setEaster(EasterSpec spec){

    }

}
