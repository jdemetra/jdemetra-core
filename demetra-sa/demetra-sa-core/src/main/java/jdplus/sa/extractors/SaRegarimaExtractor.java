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
package jdplus.sa.extractors;

import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import demetra.modelling.SeriesInfo;
import demetra.sa.ComponentType;
import demetra.sa.SaDictionaries;
import demetra.sa.SaVariable;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import jdplus.regarima.ami.ModellingUtility;
import static jdplus.regarima.extractors.RegSarimaModelExtractors.NBCAST;
import static jdplus.regarima.extractors.RegSarimaModelExtractors.NFCAST;
import jdplus.regsarima.regular.RegSarimaModel;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(InformationExtractor.class)
public class SaRegarimaExtractor extends InformationMapping<RegSarimaModel> {

    @Override
    public Class getSourceClass() {
        return RegSarimaModel.class;
    }

    private static TsData outlier(RegSarimaModel source, ComponentType type, TsDomain domain) {
        TsData s = source.deterministicEffect(domain, v -> ModellingUtility.isOutlier(v) && v.isAttribute(SaVariable.REGEFFECT, type.name()));
        return source.backTransform(s, false);
    }

    private static TsData det(RegSarimaModel source, ComponentType type, TsDomain domain, boolean lpcorr) {
        TsData s = source.deterministicEffect(domain, v -> v.isAttribute(SaVariable.REGEFFECT, type.name()));
        return source.backTransform(s, lpcorr);
    }

    private static TsData reg(RegSarimaModel source, ComponentType type, TsDomain domain) {
        TsData s = source.deterministicEffect(domain, v -> ModellingUtility.isUser(v) && v.isAttribute(SaVariable.REGEFFECT, type.name()));
        return source.backTransform(s, false);
    }

    public SaRegarimaExtractor() {
        set(SaDictionaries.OUT_I, TsData.class, source -> outlier(source, ComponentType.Irregular, null));
        setArray(SaDictionaries.OUT_I + SeriesInfo.F_SUFFIX, NFCAST, TsData.class,
                (source, i) -> outlier(source, ComponentType.Irregular, source.forecastDomain(i)));
        setArray(SaDictionaries.OUT_I + SeriesInfo.B_SUFFIX, NBCAST, TsData.class,
                (source, i) -> outlier(source, ComponentType.Irregular, source.backcastDomain(i)));
        set(SaDictionaries.OUT_T, TsData.class, source -> outlier(source, ComponentType.Trend, null));
        setArray(SaDictionaries.OUT_T + SeriesInfo.F_SUFFIX, NFCAST, TsData.class,
                (source, i) -> outlier(source, ComponentType.Trend, source.forecastDomain(i)));
        setArray(SaDictionaries.OUT_T + SeriesInfo.B_SUFFIX, NBCAST, TsData.class,
                (source, i) -> outlier(source, ComponentType.Trend, source.backcastDomain(i)));
        set(SaDictionaries.OUT_S, TsData.class, source -> outlier(source, ComponentType.Seasonal, null));
        setArray(SaDictionaries.OUT_S + SeriesInfo.F_SUFFIX, NFCAST, TsData.class,
                (source, i) -> outlier(source, ComponentType.Seasonal, source.forecastDomain(i)));
        setArray(SaDictionaries.OUT_S + SeriesInfo.B_SUFFIX, NBCAST, TsData.class,
                (source, i) -> outlier(source, ComponentType.Seasonal, source.backcastDomain(i)));

        set(SaDictionaries.REG_I, TsData.class, source -> reg(source, ComponentType.Irregular, null));
        setArray(SaDictionaries.REG_I + SeriesInfo.F_SUFFIX, NFCAST, TsData.class,
                (source, i) -> reg(source, ComponentType.Irregular, source.forecastDomain(i)));
        setArray(SaDictionaries.REG_I + SeriesInfo.B_SUFFIX, NBCAST, TsData.class,
                (source, i) -> reg(source, ComponentType.Irregular, source.backcastDomain(i)));
        set(SaDictionaries.REG_T, TsData.class, source -> reg(source, ComponentType.Trend, null));
        setArray(SaDictionaries.REG_T + SeriesInfo.F_SUFFIX, NFCAST, TsData.class,
                (source, i) -> reg(source, ComponentType.Trend, source.forecastDomain(i)));
        setArray(SaDictionaries.REG_T + SeriesInfo.B_SUFFIX, NBCAST, TsData.class,
                (source, i) -> reg(source, ComponentType.Trend, source.backcastDomain(i)));
        set(SaDictionaries.REG_S, TsData.class, source -> reg(source, ComponentType.Seasonal, null));
        setArray(SaDictionaries.REG_S + SeriesInfo.F_SUFFIX, NFCAST, TsData.class,
                (source, i) -> reg(source, ComponentType.Seasonal, source.forecastDomain(i)));
        setArray(SaDictionaries.REG_S + SeriesInfo.B_SUFFIX, NBCAST, TsData.class,
                (source, i) -> reg(source, ComponentType.Seasonal, source.backcastDomain(i)));
        set(SaDictionaries.REG_SA, TsData.class, source -> reg(source, ComponentType.SeasonallyAdjusted, null));
        setArray(SaDictionaries.REG_SA + SeriesInfo.F_SUFFIX, NFCAST, TsData.class,
                (source, i) -> reg(source, ComponentType.SeasonallyAdjusted, source.forecastDomain(i)));
        setArray(SaDictionaries.REG_SA + SeriesInfo.B_SUFFIX, NBCAST, TsData.class,
                (source, i) -> reg(source, ComponentType.SeasonallyAdjusted, source.backcastDomain(i)));
        set(SaDictionaries.REG_Y, TsData.class, source -> reg(source, ComponentType.Series, null));
        setArray(SaDictionaries.REG_Y + SeriesInfo.F_SUFFIX, NFCAST, TsData.class,
                (source, i) -> reg(source, ComponentType.Series, source.forecastDomain(i)));
        setArray(SaDictionaries.REG_Y + SeriesInfo.B_SUFFIX, NBCAST, TsData.class,
                (source, i) -> reg(source, ComponentType.Series, source.backcastDomain(i)));
        set(SaDictionaries.REG_U, TsData.class, source -> reg(source, ComponentType.Undefined, null));
        setArray(SaDictionaries.REG_U + SeriesInfo.F_SUFFIX, NFCAST, TsData.class,
                (source, i) -> reg(source, ComponentType.Undefined, source.forecastDomain(i)));
        setArray(SaDictionaries.REG_U + SeriesInfo.B_SUFFIX, NBCAST, TsData.class,
                (source, i) -> reg(source, ComponentType.Undefined, source.backcastDomain(i)));

        set(SaDictionaries.DET_I, TsData.class, source -> det(source, ComponentType.Irregular, null, false));
        setArray(SaDictionaries.DET_I + SeriesInfo.F_SUFFIX, NFCAST, TsData.class,
                (source, i) -> det(source, ComponentType.Irregular, source.forecastDomain(i), false));
        setArray(SaDictionaries.DET_I + SeriesInfo.B_SUFFIX, NBCAST, TsData.class,
                (source, i) -> det(source, ComponentType.Irregular, source.backcastDomain(i), false));
        set(SaDictionaries.DET_T, TsData.class, source -> det(source, ComponentType.Trend, null, false));
        setArray(SaDictionaries.DET_T + SeriesInfo.F_SUFFIX, NFCAST, TsData.class,
                (source, i) -> det(source, ComponentType.Trend, source.forecastDomain(i), false));
        setArray(SaDictionaries.DET_T + SeriesInfo.B_SUFFIX, NBCAST, TsData.class,
                (source, i) -> det(source, ComponentType.Trend, source.backcastDomain(i), false));
        set(SaDictionaries.DET_S, TsData.class, source -> det(source, ComponentType.Seasonal, null, true));
        setArray(SaDictionaries.DET_S + SeriesInfo.F_SUFFIX, NFCAST, TsData.class,
                (source, i) -> det(source, ComponentType.Seasonal, source.forecastDomain(i), true));
        setArray(SaDictionaries.DET_S + SeriesInfo.B_SUFFIX, NBCAST, TsData.class,
                (source, i) -> det(source, ComponentType.Seasonal, source.backcastDomain(i), true));
        set(SaDictionaries.DET_SA, TsData.class, source -> det(source, ComponentType.SeasonallyAdjusted, null, false));
        setArray(SaDictionaries.DET_SA + SeriesInfo.F_SUFFIX, NFCAST, TsData.class,
                (source, i) -> det(source, ComponentType.SeasonallyAdjusted, source.forecastDomain(i), false));
        setArray(SaDictionaries.DET_SA + SeriesInfo.B_SUFFIX, NBCAST, TsData.class,
                (source, i) -> det(source, ComponentType.SeasonallyAdjusted, source.backcastDomain(i), false));
        set(SaDictionaries.DET_Y, TsData.class, source -> det(source, ComponentType.Series, null, false));
        setArray(SaDictionaries.DET_Y + SeriesInfo.F_SUFFIX, NFCAST, TsData.class,
                (source, i) -> det(source, ComponentType.Series, source.forecastDomain(i), false));
        setArray(SaDictionaries.DET_Y + SeriesInfo.B_SUFFIX, NBCAST, TsData.class,
                (source, i) -> det(source, ComponentType.Series, source.backcastDomain(i), false));
        set(SaDictionaries.DET_U, TsData.class, source -> det(source, ComponentType.Undefined, null, false));
        setArray(SaDictionaries.DET_U + SeriesInfo.F_SUFFIX, NFCAST, TsData.class,
                (source, i) -> det(source, ComponentType.Undefined, source.forecastDomain(i), false));
        setArray(SaDictionaries.DET_U + SeriesInfo.B_SUFFIX, NBCAST, TsData.class,
                (source, i) -> det(source, ComponentType.Undefined, source.backcastDomain(i), false));
    }
}
