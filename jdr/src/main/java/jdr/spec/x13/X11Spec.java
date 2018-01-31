/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdr.spec.x13;

import ec.satoolkit.DecompositionMode;
import ec.satoolkit.x11.CalendarSigma;
import ec.satoolkit.x11.SeasonalFilterOption;
import ec.satoolkit.x11.SigmavecOption;
import ec.satoolkit.x11.X11Exception;
import ec.satoolkit.x11.X11Specification;

/**
 *
 * @author Kristof Bayens
 */
public class X11Spec {

    /**
     * @return the freq
     */
    public int getFreq() {
        return freq;
    }

    /**
     * @param freq the freq to set
     */
    public void setFreq(int freq) {
        this.freq = freq;
    }

    private final X11Specification core;
    private int freq;
    private final boolean preprocessing;

    public X11Spec(X11Specification spec, int freq, boolean preprocessing) {
        this.core = spec;
        this.freq = freq;
        this.preprocessing = preprocessing;
    }
    
    

    public String getMode() {
        return core.getMode().name();
    }

    public void setMode(String value) {
        core.setMode(DecompositionMode.valueOf(value));
    }

//    public boolean isUseForecast() {
//        return core.getForecastHorizon() != 0;
//    }
    public int getForecastHorizon() {
        return core.getForecastHorizon();
    }

    public int getBackcastHorizon() {
        return core.getBackcastHorizon();
    }

    public boolean isSeasonal() {
        return core.isSeasonal();
    }

    public void setSeasonal(boolean value) {
        core.setSeasonal(value);
    }

    public void setForecastHorizon(int value) {
        core.setForecastHorizon(value);
    }

    public void setBackcastHorizon(int value) {
        core.setBackcastHorizon(value);
    }
//    public void setUseForecast(boolean value) {
//        if (value) {
//            core.setForecastHorizon(-1);
//        } else {
//            core.setForecastHorizon(0);
//        }
//    }

    public double getLSigma() {
        return core.getLowerSigma();
    }

    public void setLSigma(double value) {
        core.setLowerSigma(value);
    }

    public double getUSigma() {
        return core.getUpperSigma();
    }

    public void setUSigma(double value) {
        core.setUpperSigma(value);
    }

    public String getSeasonalMA() {
//        if (hasSeasDetails()) {
//            return null;
//        }
//        else
        if (core.getSeasonalFilters() == null) {
            return SeasonalFilterOption.Msr.name();
        } else {
            return core.getSeasonalFilters()[0].name();
        }
    }

    public void setSeasonalMA(String ma) {
        core.setSeasonalFilter(SeasonalFilterOption.valueOf(ma));
    }

    public String[] getFullSeasonalMA() {
        SeasonalFilterOption[] filters = core.getSeasonalFilters();
        if (filters == null || freq == 0) {
            return null;
        }
        if (filters.length == freq) {
            String[] nfilters = new String[freq];
            for (int i = 0; i < freq; ++i) {
                nfilters[i] = filters[i].name();
            }
            return nfilters;
        }
        String option = filters[0].name();
        String[] nfilters = new String[freq];
        for (int i = 0; i < freq; ++i) {
            nfilters[i] = option;
        }
        return nfilters;

    }

    public void setFullSeasonalMA(String[] value) {
            SeasonalFilterOption[] filters = new SeasonalFilterOption[value.length];
            for (int i = 0; i < filters.length; ++i) {
                filters[i] = SeasonalFilterOption.valueOf(value[i]);
            }
        core.setSeasonalFilters(filters);
    }

    public boolean isAutoTrendMA() {
        return core.isAutoHenderson();
    }

    public void setAutoTrendMA(boolean value) {
        if (value) {
            core.setHendersonFilterLength(0);
        } else {
            core.setHendersonFilterLength(13);
        }
    }

    public int getTrendMA() {
        return core.getHendersonFilterLength() == 0 ? 13 : core.getHendersonFilterLength();
    }

    public void setTrendMA(int value) {
        if (value <= 1 || value > 101 || value % 2 == 0) {
            throw new X11Exception("Invalid value for henderson filter");
        } else {
            core.setHendersonFilterLength(value);
        }
    }

    public CalendarSigma getCalendarSigma() {
        return core.getCalendarSigma();
    }

//    public void setCalendarSigma(CalendarSigma calendarsigma) {
//        core.setCalendarSigma(calendarsigma);
//        if (calendarsigma.Select == CalendarSigma.Select && core.getSigmavec() == null) {
//            this.setSigmavec(this.getSigmavec());
//        };
//    }
//
//    public SigmavecOption[] getSigmavec() {
//        SigmavecOption[] groups = core.getSigmavec();
//        int len = freq_.intValue();
//        if (groups != null && groups.length == len) {
//            return groups;
//        }
//        //Sigmavec option = groups == null ? Sigmavec.group1 : groups[0];
//        //   Sigmavec option = Sigmavec.group1;
//        groups = new SigmavecOption[len];
//        for (int i = 0; i < len; ++i) {
//            groups[i] = SigmavecOption.Group1;
//        }
//        return groups;
//
//    }
//
//    public void setSigmavec(SigmavecOption[] sigmavec) {
//        core.setSigmavec(sigmavec);
//    }

    public void setExcludefcst(boolean value) {
        core.setExcludefcst(value);
    }

    public boolean isExcludefcst() {
        return core.isExcludefcst();
    }

}
