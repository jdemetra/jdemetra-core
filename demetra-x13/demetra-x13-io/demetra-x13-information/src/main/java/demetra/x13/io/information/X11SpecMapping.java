/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.x13.io.information;

import demetra.information.InformationSet;
import demetra.sa.DecompositionMode;
import demetra.x11.BiasCorrection;
import demetra.x11.CalendarSigmaOption;
import demetra.x11.SeasonalFilterOption;
import demetra.x11.SigmaVecOption;
import demetra.x11.X11Spec;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class X11SpecMapping {

    public final String MODE = "mode",
            SEASONAL = "seasonal",
            LSIGMA = "lsigma",
            USIGMA = "usigma",
            TRENDMA = "trendma",
            SEASONALMA = "seasonalma",
            BCASTS = "bcasts",
            FCASTS = "fcasts",
            CALENDARSIGMA = "calendarsigma",
            SIGMAVEC = "sigmavec",
            EXCLUDEFCAST = "excludeforcast",
            BIAS = "bias";

    public InformationSet write(X11Spec spec, boolean verbose) {
        if (!verbose && spec.isDefault()) {
            return null;
        }
        InformationSet info = new InformationSet();
        if (verbose || spec.getMode() != DecompositionMode.Undefined) {
            info.add(MODE, spec.getMode().name());
        }
        if (verbose || !spec.isSeasonal()) {
            info.add(SEASONAL, spec.isSeasonal());
        }
        if (verbose || spec.getLowerSigma() != X11Spec.DEFAULT_LOWER_SIGMA) {
            info.add(LSIGMA, spec.getLowerSigma());
        }
        if (verbose || spec.getUpperSigma() != X11Spec.DEFAULT_UPPER_SIGMA) {
            info.add(USIGMA, spec.getUpperSigma());
        }
        if (verbose || spec.getHendersonFilterLength() != 0) {
            info.add(TRENDMA, spec.getHendersonFilterLength());
        }
        SeasonalFilterOption[] filters = spec.getFilters();
        if (filters != null) {
            String[] sfilters = new String[filters.length];
            for (int i = 0; i < filters.length; ++i) {
                sfilters[i] = filters[i].name();
            }
            info.add(SEASONALMA, sfilters);
        }
        if (verbose || spec.getForecastHorizon() != X11Spec.DEFAULT_FORECAST_HORIZON) {
            info.add(FCASTS, spec.getForecastHorizon());
        }
        if (verbose || spec.getBackcastHorizon() != X11Spec.DEFAULT_BACKCAST_HORIZON) {
            info.add(BCASTS, spec.getBackcastHorizon());
        }

        if (verbose || spec.getCalendarSigma() != CalendarSigmaOption.None) {
            info.add(CALENDARSIGMA, spec.getCalendarSigma().name());
        }
        SigmaVecOption[] vsig = spec.getSigmaVec();
        if (vsig != null) {
            String[] sigmavec = new String[vsig.length];
            for (int i = 0; i < vsig.length; ++i) {
                sigmavec[i] = vsig[i].name();
            }
            info.add(SIGMAVEC, sigmavec);
        }

        if (verbose || spec.isExcludeForecast()) {
            info.add(EXCLUDEFCAST, spec.isExcludeForecast());
        }
        if (verbose || spec.getBias() != BiasCorrection.Legacy) {
            info.add(BIAS, spec.getBias().name());
        }
        return info;
    }
    
    public X11Spec read(InformationSet info) {
        if (info == null)
            return X11Spec.DEFAULT_UNDEFINED;
        X11Spec.Builder builder = X11Spec.builder();

        String mode = info.get(MODE, String.class);
             if (mode != null) {
                builder.mode(DecompositionMode.valueOf(mode));
            }
            Boolean seasonal = info.get(SEASONAL, Boolean.class);
            if (seasonal != null) {
                builder.seasonal(seasonal);
            }
            Double lsig = info.get(LSIGMA, Double.class);
            if (lsig != null) {
                builder.lowerSigma(lsig);
            }
            Double usig = info.get(USIGMA, Double.class);
            if (usig != null) {
                builder.upperSigma(usig);
            }
            Integer trendma = info.get(TRENDMA, Integer.class);
            if (trendma != null) {
                builder.hendersonFilterLength(trendma);
            }
            Integer fcasts = info.get(FCASTS, Integer.class);
            if (fcasts != null) {
                builder.forecastHorizon(fcasts);
            }
            Integer bcasts = info.get(BCASTS, Integer.class);
            if (bcasts != null) {
                builder.backcastHorizon(bcasts);
            }
            String[] sfilters = info.get(SEASONALMA, String[].class);
            if (sfilters != null) {
                SeasonalFilterOption[] filters = new SeasonalFilterOption[sfilters.length];
                for (int i = 0; i < sfilters.length; ++i) {
                    filters[i] = SeasonalFilterOption.valueOf(sfilters[i]);
                }
                builder.filters(filters);
            }

            String calendarsigma = info.get(CALENDARSIGMA, String.class);
            if (calendarsigma != null) {
                builder.calendarSigma(CalendarSigmaOption.valueOf(calendarsigma));
            }

            String[] sigmavec = info.get(SIGMAVEC, String[].class);
            if (sigmavec != null) {
                SigmaVecOption[] vsig = new SigmaVecOption[sigmavec.length];
                for (int i = 0; i < sigmavec.length; ++i) {
                    vsig[i] = SigmaVecOption.valueOf(sigmavec[i]);
                }
                builder.sigmaVec(vsig);
            }

            Boolean excludefcst = info.get(EXCLUDEFCAST, Boolean.class);
            if (excludefcst != null) {
                builder.excludeForecast(excludefcst);
            }

            String sbias = info.get(BIAS, String.class);
            if (sbias != null) {
                builder.bias(BiasCorrection.valueOf(sbias));
            } 
            return builder.build();

     }

}
