/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdr.spec.tramoseats;

import ec.tstoolkit.modelling.RegressionTestType;
import ec.tstoolkit.modelling.TradingDaysSpecType;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.timeseries.calendars.GregorianCalendarManager;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;

/**
 *
 * @author Jean Palate
 */
public class TradingDaysSpec extends BaseTramoSpec {


    private ec.tstoolkit.modelling.arima.tramo.TradingDaysSpec inner() {
        return core.getRegression().getCalendar().getTradingDays();
    }

    public TradingDaysSpec(TramoSpecification spec) {
        super(spec);
    }

    public String getOption() {
        ec.tstoolkit.modelling.arima.tramo.TradingDaysSpec spec = inner();
        if (spec.isUsed()) {
            if (spec.isStockTradingDays()) {
                return TradingDaysSpecType.Stock.name();
            } else if (spec.getHolidays() != null) {
                return TradingDaysSpecType.Holidays.name();
            } else if (spec.getUserVariables() != null) {
                return TradingDaysSpecType.UserDefined.name();
            } else if (spec.isUsed()) {
                return TradingDaysSpecType.Default.name();
            }
        }
        return TradingDaysSpecType.None.name();
    }

    public void setOption(String nvalue) {
        ec.tstoolkit.modelling.arima.tramo.TradingDaysSpec spec = inner();
        TradingDaysSpecType value=TradingDaysSpecType.valueOf(nvalue);
        switch (value) {
            case None:
                spec.disable();
                break;
            case Default:
                spec.setTradingDaysType(TradingDaysType.TradingDays);
                spec.setLeapYear(true);
                spec.setHolidays(null);
                spec.setTest(true);
                break;
            case Stock:
                spec.setStockTradingDays(31);
                spec.setTest(true);
                break;
            case Holidays:
                spec.setTradingDaysType(TradingDaysType.TradingDays);
                spec.setLeapYear(true);
                spec.setHolidays(GregorianCalendarManager.DEF);
                spec.setTest(true);
                break;
            case UserDefined:
                spec.setUserVariables(new String[]{});
                spec.setTest(true);
                break;

            default:
                throw new UnsupportedOperationException("Not supported yet.");

        }
    }

    public String getRegressionTestType() {
        return inner().getRegressionTestType().name();
    }

    public void setRegressionTestType(String value) {
        inner().setRegressionTestType(RegressionTestType.valueOf(value));
    }

    public int getW() {
        return inner().getStockTradingDays();
    }

    public void setW(int w) {
        inner().setStockTradingDays(w);
    }

    public String getTradingDays() {
        return inner().getTradingDaysType().toString();
    }

    public void setTradingDays(String value) {
        inner().setTradingDaysType(TradingDaysType.valueOf(value));
    }

    public boolean getLeapYear() {
        return inner().isLeapYear();
    }

    public void setLeapYear(boolean value) {
        inner().setLeapYear(value);
    }

//    public Holidays getHolidays() {
//        return new Holidays(inner().getHolidays());
//    }
//
//    public void setHolidays(Holidays holidays) {
//        inner().setHolidays(holidays.getName());
//    }
//
    public String[] getUserVariables() {
        return inner().getUserVariables();
    }

    public void setUserVariables(String[] vars) {
        inner().setUserVariables(vars);
    }

    public String getAutomatic() {
        return inner().getAutomaticMethod().name();
    }

    public void setAutomatic(String value) {
        inner().setAutomaticMethod(ec.tstoolkit.modelling.arima.tramo.TradingDaysSpec.AutoMethod.valueOf(value));
    }
    
   public double getPftd() {
        return inner().getProbabibilityForFTest();
    }

    public void setPftd(double value) {
        inner().setProbabibilityForFTest(value);
    }
    
}
