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
package ec.satoolkit;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;

/**
 *
 * @author Jean Palate
 */
public class DefaultPreprocessingFilter implements IPreprocessingFilter {

    private static final double EPS = 1e-8;
    private PreprocessingModel model_;
    @Deprecated
    private double mean_;
    private int nf_ = -2, nb_=-2;

    public int getForecastHorizon() {
        return nf_;
    }

    public void setForecastHorizon(int nf) {
        nf_ = nf;
    }

    public int getBackcastHorizon() {
        return nb_;
    }

    public void setBackcastHorizon(int nb) {
        nb_ = nb;
    }

    @Deprecated
    public boolean isSeasonalMeanCorrection() {
        return mean_ != 0;
    }

    @Override
    public boolean process(PreprocessingModel model) {
        model_ = model;
        mean_ = seasMeanCorrection(model);
        return true;
    }

    @Deprecated
    private double seasMeanCorrection(PreprocessingModel model) {
        TsDomain domain = model.description.getSeriesDomain();
        int freq = domain.getFrequency().intValue();
        int nf = forecastLength(freq);
        int nb = backcastLength(freq);

        int n = domain.getLength() + nf;
        
        domain = new TsDomain(domain.getStart().minus(nb), n);
        TsData ccorr = model.deterministicEffect(domain, ComponentType.CalendarEffect);
        TsData scorr = model.deterministicEffect(domain, ComponentType.Seasonal);
        TsData corr = TsData.add(scorr, ccorr);
        if (corr == null) {
            return 0;
        }

        int mq = freq, nyr = (n / mq) * mq;
        DataBlock data = new DataBlock(corr.internalStorage(), 0, nyr, 1);
        double m = data.sum();
        m /= nyr;
        if (Math.abs(m) < EPS) {
            return 0;
        } else {
            return m;
        }
    }

    @Override
    public TsData getCorrectedSeries(boolean transformed) {
        ;
        boolean mul = (!transformed) && model_.isMultiplicative();
        TsData lin = model_.linearizedSeries(true);
        if (mul) {
            lin = lin.exp();
        }
        return lin;
    }

    private int forecastLength(int freq) {
        if (nf_ == 0 || freq == 0) {
            return 0;
        } else if (nf_ < 0) {
            return -freq * nf_;
        } else {
            return nf_;
        }
    }

    private int backcastLength(int freq) {
        if (nb_ == 0 || freq == 0) {
            return 0;
        } else if (nb_ < 0) {
            return -freq * nb_;
        } else {
            return nb_;
        }
    }
    
    @Override
    public TsData getCorrectedForecasts(boolean transformed) {
        if (nf_ == 0) {
            return null;
        }
        int nf = forecastLength(model_.description.getFrequency());
        TsData f = model_.linearizedForecast(nf, true);
        if ((!transformed) && model_.isMultiplicative()) {
            f.apply(x -> Math.exp(x));
        }
        return f;
    }

    @Override
    public TsData getCorrectedBackcasts(boolean transformed) {
        if (nb_ == 0) {
            return null;
        }
        int nb = backcastLength(model_.description.getFrequency());
        TsData b = model_.linearizedBackcast(nb, true);
        if ((!transformed) && model_.isMultiplicative()) {
            b.apply(x -> Math.exp(x));
        }
        return b;
    }

    @Override
    public TsData getCorrection(TsDomain domain, ComponentType type, boolean transformed) {
        switch (type) {
            case Series:
                TsData x = model_.deterministicEffect(domain, type);
                if (!transformed) {
                    model_.backTransform(x, false, false);
                }
                return x;
            case Trend:
                TsData t = model_.deterministicEffect(domain, type);
                if (!transformed) {
                    model_.backTransform(t, true, false);
                }
                return t;
            case Seasonal:
                TsData s = model_.deterministicEffect(domain, type);
                TsData c = model_.deterministicEffect(domain, ComponentType.CalendarEffect);
                TsData sc = TsData.add(s, c);
                if (!transformed) {
                    model_.backTransform(sc, false, true);
                }
                return sc;
            case SeasonallyAdjusted:
                TsData sa = model_.deterministicEffect(domain, type);
                if (!transformed) {
                    model_.backTransform(sa, false, false);
                }
                return sa;
            case Undefined:
                TsData undef = model_.deterministicEffect(domain, type);
                if (!transformed) {
                    model_.backTransform(undef, false, false);
                }
                return undef;
            case Irregular:
                TsData i = model_.deterministicEffect(domain, type);
                if (!transformed) {
                    model_.backTransform(i, false, false);
                }
                return i;

            default:
                return null;
        }

    }

    @Override
    @Deprecated
    public double getBiasCorrection(ComponentType type) {
//        switch (type) {
//            case Series:
//                return -mean_;
//            case Seasonal:
//                return -mean_;
//            default:
//                return 0;
//        }
        return 0;
    }

//    @Override
//    public TsData filter(String id, TsData data) {
//        return data;
//    }
    @Override
    public boolean isInitialized() {
        return model_ != null;
    }
}
