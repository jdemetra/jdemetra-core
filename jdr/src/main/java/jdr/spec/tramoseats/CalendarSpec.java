/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jdr.spec.tramoseats;

import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;

/**
 *
 * @author Jean Palate
 */
public class CalendarSpec extends BaseTramoSpec{

    @Override
    public String toString(){
        return "";
    }

    public CalendarSpec(TramoSpecification spec){
        super(spec);
    }

    public TradingDaysSpec getTradingDays(){
        return new TradingDaysSpec(core);
    }

    public EasterSpec getEaster(){
        return new EasterSpec(core);
    }

}
