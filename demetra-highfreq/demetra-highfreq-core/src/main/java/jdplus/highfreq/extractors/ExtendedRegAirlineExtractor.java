/*
 * Copyright 2022 National Bank of Belgium
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
package jdplus.highfreq.extractors;

import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import demetra.modelling.SeriesInfo;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.IOutlier;
import demetra.timeseries.regression.MissingValueEstimation;
import demetra.timeseries.regression.RegressionItem;
import demetra.timeseries.regression.TrendConstant;
import demetra.timeseries.regression.UserVariable;
import demetra.toolkit.dictionaries.Dictionary;
import demetra.toolkit.dictionaries.RegArimaDictionaries;
import demetra.toolkit.dictionaries.RegressionDictionaries;
import jdplus.highfreq.regarima.HighFreqRegArimaModel;
import jdplus.modelling.GeneralLinearModel;
import nbbrd.design.Development;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class ExtendedRegAirlineExtractor extends InformationMapping<HighFreqRegArimaModel> {

    public final int NFCAST = 50, NBCAST = 0;

    private String regressionItem(String key) {
        return Dictionary.concatenate(RegArimaDictionaries.REGRESSION, key);
    }

    public ExtendedRegAirlineExtractor() {
        delegate(null, GeneralLinearModel.class, source -> source);

//            setArray(RegressionDictionaries.Y_F, NFCAST, TsData.class, (source, i) -> source.forecasts(i).getForecasts());
//            setArray(RegressionDictionaries.Y_B, NBCAST, TsData.class, (source, i) -> source.backcasts(i).getForecasts());
//            setArray(RegressionDictionaries.Y_EF, NFCAST, TsData.class, (source,i) -> source.forecasts(i).getForecastsStdev());
//            setArray(RegressionDictionaries.Y_EB, NBCAST, TsData.class, (source,i) -> source.backcasts(i).getForecastsStdev());
        set(RegressionDictionaries.YC, TsData.class, source -> source.interpolatedSeries(false));
        set(RegressionDictionaries.L, TsData.class, source -> source.linearizedSeries());
        set(RegressionDictionaries.DET, TsData.class, (HighFreqRegArimaModel source) -> {
            TsData det = source.deterministicEffect(null, v -> true);
            return source.backTransform(det, true);
        });
        setArray(RegressionDictionaries.DET + SeriesInfo.F_SUFFIX, NFCAST, TsData.class,
                (source, i) -> {
                    TsData det = source.deterministicEffect(source.forecastDomain(i), v -> true);
                    return source.backTransform(det, true);
                });
        setArray(RegressionDictionaries.DET + SeriesInfo.B_SUFFIX, NBCAST, TsData.class,
                (source, i) -> {
                    TsData det = source.deterministicEffect(source.backcastDomain(i), v -> true);
                    return source.backTransform(det, true);
                });

// All calendar effects
        set(RegressionDictionaries.CAL, TsData.class, source -> source.getCalendarEffect(null));
        setArray(RegressionDictionaries.CAL + SeriesInfo.F_SUFFIX, NFCAST, TsData.class,
                (source, i) -> source.getCalendarEffect(source.forecastDomain(i)));
        setArray(RegressionDictionaries.CAL + SeriesInfo.B_SUFFIX, NBCAST, TsData.class,
                (source, i) -> source.getCalendarEffect(source.backcastDomain(i)));

// All moving holidays effects
        set(RegressionDictionaries.MHE, TsData.class, source -> source.getMovingHolidayEffect(null));
        setArray(RegressionDictionaries.MHE_F, NFCAST, TsData.class,
                (source, i) -> source.getMovingHolidayEffect(source.forecastDomain(i)));
        setArray(RegressionDictionaries.MHE_B, NBCAST, TsData.class,
                (source, i) -> source.getMovingHolidayEffect(source.backcastDomain(i)));

// Easter effect
        set(RegressionDictionaries.EE, TsData.class, source -> source.getEasterEffect(null));
        setArray(RegressionDictionaries.EE_F, NFCAST, TsData.class,
                (source, i) -> source.getEasterEffect(source.forecastDomain(i)));
        setArray(RegressionDictionaries.EE_B, NBCAST, TsData.class,
                (source, i) -> source.getEasterEffect(source.backcastDomain(i)));

// Other moving holidays effects
        set(RegressionDictionaries.OMHE, TsData.class, source -> source.getOtherMovingHolidayEffect(null));
        setArray(RegressionDictionaries.OMHE_F, NFCAST, TsData.class,
                (source, i) -> source.getOtherMovingHolidayEffect(source.forecastDomain(i)));
        setArray(RegressionDictionaries.OMHE_B, NBCAST, TsData.class,
                (source, i) -> source.getOtherMovingHolidayEffect(source.backcastDomain(i)));

// All Outliers effect
        set(RegressionDictionaries.OUT, TsData.class, source -> source.getOutliersEffect(null));
        setArray(RegressionDictionaries.OUT + SeriesInfo.F_SUFFIX, NFCAST, TsData.class,
                (source, i) -> source.getOutliersEffect(source.forecastDomain(i)));
        setArray(RegressionDictionaries.OUT + SeriesInfo.B_SUFFIX, NBCAST, TsData.class,
                (source, i) -> source.getOutliersEffect(source.backcastDomain(i)));

        set(regressionItem(RegressionDictionaries.MU), RegressionItem.class,
                source -> source.regressionItem(v -> v instanceof TrendConstant, 0));
        setArray(regressionItem(RegressionDictionaries.OUTLIERS), 1, 31, RegressionItem.class,
                (source, i) -> source.regressionItem(v -> v instanceof IOutlier, i - 1));
        setArray(regressionItem(RegressionDictionaries.USER), 1, 30, RegressionItem.class,
                (source, i) -> source.regressionItem(v -> v instanceof UserVariable, i - 1));
        setArray(regressionItem(RegressionDictionaries.MISSING), 1, 100, MissingValueEstimation.class,
                (source, i) -> {
                    MissingValueEstimation[] missing = source.getEstimation().getMissing();
                    return i <= 0 || i > missing.length ? null : missing[i - 1];
                });

    }

    @Override
    public Class<HighFreqRegArimaModel> getSourceClass() {
        return HighFreqRegArimaModel.class;
    }
}
