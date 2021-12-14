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
import demetra.modelling.SeriesInfo;
import demetra.sa.StationaryVarianceDecomposition;
import demetra.x11.X11Results;
import demetra.x13.X13Dictionary;
import jdplus.regsarima.regular.RegSarimaModel;
import jdplus.x13.X13Diagnostics;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class X13Extractor extends InformationMapping<X13Results> {

    public static final String FINAL = "";

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
        set(BasicInformationExtractor.concatenate(X13Dictionary.PREADJUST, "a1"), TsData.class, source
                -> source.getPreadjustment().getA1());
        set(BasicInformationExtractor.concatenate(X13Dictionary.PREADJUST, "a1a"), TsData.class, source
                -> source.getPreadjustment().getA1a());
        set(BasicInformationExtractor.concatenate(X13Dictionary.PREADJUST, "a1b"), TsData.class, source
                -> source.getPreadjustment().getA1b());
        set(BasicInformationExtractor.concatenate(X13Dictionary.PREADJUST, "a6"), TsData.class, source
                -> source.getPreadjustment().getA6());
        set(BasicInformationExtractor.concatenate(X13Dictionary.PREADJUST, "a7"), TsData.class, source
                -> source.getPreadjustment().getA7());
        set(BasicInformationExtractor.concatenate(X13Dictionary.PREADJUST, "a8"), TsData.class, source
                -> source.getPreadjustment().getA8());
        set(BasicInformationExtractor.concatenate(X13Dictionary.PREADJUST, "a8i"), TsData.class, source
                -> source.getPreadjustment().getA8i());
        set(BasicInformationExtractor.concatenate(X13Dictionary.PREADJUST, "a8s"), TsData.class, source
                -> source.getPreadjustment().getA8s());
        set(BasicInformationExtractor.concatenate(X13Dictionary.PREADJUST, "a8t"), TsData.class, source
                -> source.getPreadjustment().getA8t());
        set(BasicInformationExtractor.concatenate(X13Dictionary.PREADJUST, "a9"), TsData.class, source
                -> source.getPreadjustment().getA9());
        set(BasicInformationExtractor.concatenate(X13Dictionary.PREADJUST, "a9sa"), TsData.class, source
                -> source.getPreadjustment().getA9sa());
        set(BasicInformationExtractor.concatenate(X13Dictionary.PREADJUST, "a9u"), TsData.class, source
                -> source.getPreadjustment().getA9u());
        set(BasicInformationExtractor.concatenate(X13Dictionary.PREADJUST, "a9ser"), TsData.class, source
                -> source.getPreadjustment().getA9ser());

        set(SaDictionary.S, TsData.class, source
                -> source.getFinals().getD16());
        set(SaDictionary.S + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getFinals().getD16a());
        set(SaDictionary.SA, TsData.class, source
                -> source.getFinals().getD11final());
        set(SaDictionary.T, TsData.class, source
                -> source.getFinals().getD12final());
        set(SaDictionary.I, TsData.class, source
                -> source.getFinals().getD13final());
        set("d10final", TsData.class, source
                -> source.getFinals().getD10final());
        set("d11final", TsData.class, source
                -> source.getFinals().getD11final());
        set("d12final", TsData.class, source
                -> source.getFinals().getD12final());
        set("d13final", TsData.class, source
                -> source.getFinals().getD13final());
        
        set("e1", TsData.class, source
                -> source.getFinals().getE1());
        set("e2", TsData.class, source
                -> source.getFinals().getE2());
        set("e3", TsData.class, source
                -> source.getFinals().getE3());
        set("e11", TsData.class, source
                -> source.getFinals().getE11());
        
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
        
        delegate(null, X13Diagnostics.class, source -> source.getDiagnostics());
        
        delegate(SaDictionary.DECOMPOSITION, X11Results.class, source -> source.getDecomposition());
    }

    @Override
    public Class getSourceClass() {
        return X13Results.class;
    }
}
