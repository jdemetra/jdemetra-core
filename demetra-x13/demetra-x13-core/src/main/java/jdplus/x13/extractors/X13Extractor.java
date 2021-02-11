/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.extractors;

import demetra.information.InformationExtractor;
import nbbrd.design.Development;
import demetra.information.InformationMapping;
import demetra.sa.SaDictionary;
import demetra.timeseries.TsData;
import jdplus.regarima.extractors.ModelEstimationExtractor;
import jdplus.x13.X13Results;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@lombok.experimental.UtilityClass
public class X13Extractor {
    private final InformationMapping<X13Results> MAPPING = new InformationMapping<>(X13Results.class);
    private final String DECOMP = "decomposition" + InformationExtractor.SEP, FINAL = "";

    static {
//         MAPPING.set(FINAL + ModellingDictionary.Y, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.Value));
//        MAPPING.set(FINAL + ModellingDictionary.Y + SeriesInfo.F_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.Forecast));
//        MAPPING.set(FINAL + ModellingDictionary.Y + SeriesInfo.EF_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.StdevForecast));
//        MAPPING.set(FINAL + ModellingDictionary.Y + SeriesInfo.B_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.Backcast));
//        MAPPING.set(FINAL + ModellingDictionary.Y + SeriesInfo.EB_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.StdevBackcast));
//
//        MAPPING.set(FINAL + SaDictionary.T, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.Value));
//        MAPPING.set(FINAL + SaDictionary.T + SeriesInfo.F_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.Forecast));
//        MAPPING.set(FINAL + SaDictionary.T + SeriesInfo.EF_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.StdevForecast));
//        MAPPING.set(FINAL + SaDictionary.T + SeriesInfo.B_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.Backcast));
//        MAPPING.set(FINAL + SaDictionary.T + SeriesInfo.EB_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.StdevBackcast));
//
        MAPPING.set(FINAL + SaDictionary.S, TsData.class, source
                -> source.getFinals().getD10final());
        MAPPING.set(FINAL + SaDictionary.SA, TsData.class, source
                -> source.getFinals().getD11final());
        MAPPING.set(FINAL + SaDictionary.T, TsData.class, source
                -> source.getFinals().getD12final());
        MAPPING.set(FINAL + SaDictionary.I, TsData.class, source
                -> source.getFinals().getD13final());
        MAPPING.set("d10final", TsData.class, source
                -> source.getFinals().getD10final());
        MAPPING.set("d11final", TsData.class, source
                -> source.getFinals().getD11final());
        MAPPING.set("d12final", TsData.class, source
                -> source.getFinals().getD12final());
        MAPPING.set("d13final", TsData.class, source
                -> source.getFinals().getD13final());
//        MAPPING.set(FINAL + SaDictionary.SA + SeriesInfo.F_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast));
//        MAPPING.set(FINAL + SaDictionary.SA + SeriesInfo.EF_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast));
//        MAPPING.set(FINAL + SaDictionary.SA + SeriesInfo.B_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Backcast));
//        MAPPING.set(FINAL + SaDictionary.SA + SeriesInfo.EB_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevBackcast));
//
//        MAPPING.set(FINAL + SaDictionary.S, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.Value));
//        MAPPING.set(FINAL + SaDictionary.S + SeriesInfo.F_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.Forecast));
//        MAPPING.set(FINAL + SaDictionary.S + SeriesInfo.EF_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.StdevForecast));
//        MAPPING.set(FINAL + SaDictionary.S + SeriesInfo.B_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.Backcast));
//        MAPPING.set(FINAL + SaDictionary.S + SeriesInfo.EB_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.StdevBackcast));
//
//        MAPPING.set(FINAL + SaDictionary.I, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.Value));
//        MAPPING.set(FINAL + SaDictionary.I + SeriesInfo.F_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.Forecast));
//        MAPPING.set(FINAL + SaDictionary.I + SeriesInfo.EF_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.StdevForecast));
//        MAPPING.set(FINAL + SaDictionary.I + SeriesInfo.B_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.Backcast));
//        MAPPING.set(FINAL + SaDictionary.I + SeriesInfo.EB_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.StdevBackcast));
        MAPPING.delegate("preprocessing", ModelEstimationExtractor.getMapping(), source -> source.getPreprocessing());
        MAPPING.delegate("x11", X11Extractor.getMapping(), source -> source.getDecomposition());
   }

    public InformationMapping<X13Results> getMapping() {
        return MAPPING;
    }
}
