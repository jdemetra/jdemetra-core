/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.extractors;

import nbbrd.design.Development;
import demetra.information.InformationMapping;
import demetra.sa.SaDictionary;
import demetra.timeseries.TsData;
import jdplus.x13.X13Results;
import demetra.information.BasicInformationExtractor;
import demetra.information.InformationExtractor;
import demetra.x11.X11Results;
import jdplus.regsarima.regular.RegSarimaModel;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class X13Extractor extends InformationMapping<X13Results> {

    private final String DECOMP = "decomposition" + BasicInformationExtractor.SEP, FINAL = "";

    public X13Extractor() {
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
        set(FINAL + SaDictionary.S, TsData.class, source
                -> source.getFinals().getD10final());
        set(FINAL + SaDictionary.SA, TsData.class, source
                -> source.getFinals().getD11final());
        set(FINAL + SaDictionary.T, TsData.class, source
                -> source.getFinals().getD12final());
        set(FINAL + SaDictionary.I, TsData.class, source
                -> source.getFinals().getD13final());
        set("d10final", TsData.class, source
                -> source.getFinals().getD10final());
        set("d11final", TsData.class, source
                -> source.getFinals().getD11final());
        set("d12final", TsData.class, source
                -> source.getFinals().getD12final());
        set("d13final", TsData.class, source
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
        delegate(null, RegSarimaModel.class, source -> source.getPreprocessing());
        delegate("x11", X11Results.class, source -> source.getDecomposition());
    }

    @Override
    public Class getSourceClass() {
        return X13Results.class;
    }
}
