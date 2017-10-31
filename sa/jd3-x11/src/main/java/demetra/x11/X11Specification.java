/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package demetra.x11;

import demetra.design.Development;
import demetra.maths.linearfilters.SymmetricFilter;
import demetra.sa.DecompositionMode;

/**
 *
 * @author Frank Osaer, Jean Palate, BAYENSK
 */
@Development(status = Development.Status.Alpha)
@lombok.Data
public final class X11Specification implements Cloneable {

    public static final double DEF_LSIGMA = 1.5, DEF_USIGMA = 2.5;
    public static final int DEF_FCASTS = -1, DEF_BCASTS = 0;

    private int period;
    private DecompositionMode mode = DecompositionMode.Undefined;
    private boolean seasonal = true;
    private SeasonalFilterOption[] seasonalFilters;
    private double lSigma = DEF_LSIGMA, uSigma = DEF_USIGMA;
    private int henderson_ = 0;
    private int forecasts = DEF_FCASTS, backcasts = DEF_BCASTS;
    private CalendarSigma calendarSigma = CalendarSigma.None;
    private SigmavecOption[] sigmaVector; 
    private boolean excludeForecasts= false;

    /**
     * Number of forecasts used in X11. By default, 0. When pre-processing is
     * used, the number of forecasts corresponds usually to 1 year.
     * Negative values correspond to full years (-3 = 3 years)
     *
     * @return the forecastsHorizon
     */
    public int getForecastHorizon() {
        return forecasts;
    }

    /**
     * Number of backcasts used in X11. By default, 0.
     * Negative values correspond to full years (-3 = 3 years)
     *
     * @return the backcastsHorizon
     */
    public int getBackcastHorizon() {
        return backcasts;
    }
    /**
     * Length of the Henderson filter [trendma option in X12-Arima]. When the
     * length is 0, an automatic estimation of the length of the Henderson
     * filter is computed by the algorithm.
     *
     * @return
     */
    public int getHendersonFilterLength() {
        return henderson_;
    }

    /**
     * Option of Calendarsigma[X12], specifies the calculation of the standard
     * error calculation used for outlier detection in the X11 part
     *
     * @return
     */
    public CalendarSigma getCalendarSigma() {
        return calendarSigma;
    }
    
    public SigmavecOption[] getSigmavec(){
        return sigmaVector ;
    }
    
    public double getLowerSigma() {
        return lSigma;
    }

    public double getUpperSigma() {
        return uSigma;
    }

    public DecompositionMode getMode() {
        return mode;
    }

    public SymmetricFilter getTrendFilter() {
        return TrendCycleFilterFactory.makeHendersonFilter(13);
    }

    public SeasonalFilterOption[] getSeasonalFilters() {
        return seasonalFilters;
    }

    public boolean isSeasonal() {
        return seasonal;
    }

    public void setSeasonal(boolean seas) {
        seasonal = seas;
    }
    
    public boolean isExcludefcst(){
    return excludeForecasts;
    }
    
    /**
     *
     * @param excludefcst default is false; true then the forcasts are ignored for the extreme value calculation
     */
    public void setExcludefcst(boolean excludefcst){
    excludeForecasts=excludefcst;
    }

    public boolean isDefault() {
        if (!seasonal || mode != DecompositionMode.Multiplicative) {
            return false;
        }

        if (calendarSigma != CalendarSigma.None) {
            return false;
        }
        
        if (forecasts != DEF_FCASTS) {
            return false;
        }
        if (seasonalFilters != null) {
            for (int i = 0; i < seasonalFilters.length; ++i) {
                if (seasonalFilters[i] != SeasonalFilterOption.Msr) {
                    return false;
                }
            }
        }
        
        if (sigmaVector != null) {
            for (int i = 0; i < sigmaVector.length; ++i) {
                if (sigmaVector[i] != SigmavecOption.Group1) {
                    return false;
                }
            }
        }
        if (lSigma != DEF_LSIGMA) {
            return false;
        }
        if (uSigma != DEF_USIGMA) {
            return false;
        }
        
        if (!excludeForecasts){
        return false;}
        
        return isAutoHenderson();
        
        
    }

    public boolean isAutoHenderson() {
        return henderson_ <= 0;
    }

    /**
     * Number of forecasts used in X11. By default, 0. When pre-processing is
     * used, the number of forecasts corresponds usually to 1 year.
     *
     * @param forecastsHorizon The forecasts horizon to set When
     * forecastsHorizon is negative, its absolute value corresponds to the
     * number of years of forecasting. For example, setForecastHorizon(-1) is
     * equivalent to setForecastHorizon(12) for monthly data and to
     * setForecastHorizon(4) for quarterly data.
     */
    public void setForecastHorizon(int forecastsHorizon) {
        this.forecasts = forecastsHorizon;
    }

    /**
     * Number of forecasts used in X11. By default, 0. When pre-processing is
     * used, the number of forecasts corresponds usually to 1 year.
     *
     * @param backcastsHorizon The backcasts horizon to set When
     * backcastsHorizon is negative, its absolute value corresponds to the
     * number of years of backcasting. For example, setBackcastHorizon(-1) is
     * equivalent to setBackcastHorizon(12) for monthly data and to
     * setBackcastHorizon(4) for quarterly data.
     */
    public void setBackcastHorizon(int backcastsHorizon) {
        this.backcasts = backcastsHorizon;
    }
    /**
     * Option of Calendarsigma[X12], specifies the calculation of the standard
     * error calculation used for outlier detection in the X11 part
     *
     * @param calendarsigma
     */
    public void setCalendarSigma(CalendarSigma calendarsigma) {
        calendarSigma = calendarsigma;
    }

    public void setSigmavec(SigmavecOption[] sigmavec){
        sigmaVector= sigmavec.clone();
    }
              
      
     /**
     * Parameters for extreme values detection [sigmalim option in X12-arima].
     *
     * @param lsigma Lower sigma value for extreme values detection
     * @param usigma Upper sigma value for extreme values detection lsigma
     * should be lower than usigma and higher than .5.
     */
    public void setSigma(double lsigma, double usigma) {
        if (usigma <= lsigma || lsigma <= 0.5) {
            throw new X11Exception("Invalid sigma options");
        }
        lSigma = lsigma;
        uSigma = usigma;
    }

    public void setLowerSigma(double lsigma) {
        if (uSigma <= lsigma) {
            setSigma(lsigma, lsigma + .5);
        } else {
            setSigma(lsigma, uSigma);
        }
    }

    public void setUpperSigma(double usigma) {
        if (usigma <= lSigma) {
            setSigma(usigma - .5, usigma);
        } else {
            setSigma(lSigma, usigma);
        }
    }

    /**
     * Length of the Henderson filter [trendma option in X12-arima]
     *
     * @param len Length of the Henderson filter. When the length is 0, an
     * automatic estimation is made by the program. Otherwise, the length should
     * be an odd number in the range [1, 101].
     */
    public void setHendersonFilterLength(int len) {
        if (len < 0 || len > 101 || (len != 0 && len % 2 == 0)) {
            throw new X11Exception("Invalid henderson length");
        }
        henderson_ = len;
    }

    public void setSeasonalFilters(SeasonalFilterOption[] seasonalFilter) {
        seasonalFilters = seasonalFilter.clone();
    }

    public void setSeasonalFilter(SeasonalFilterOption seasonalFilter) {
        seasonalFilters = new SeasonalFilterOption[]{seasonalFilter};
    }

    @Override
    public X11Specification clone() {
        try {
            X11Specification cspec = (X11Specification) super.clone();
            if (seasonalFilters != null) {
                cspec.seasonalFilters = seasonalFilters.clone();
            }
            if (sigmaVector != null) {
                cspec.sigmaVector = sigmaVector.clone();
            }
            return cspec;
        } catch (CloneNotSupportedException err) {
            throw new AssertionError();
        }
    }


}
