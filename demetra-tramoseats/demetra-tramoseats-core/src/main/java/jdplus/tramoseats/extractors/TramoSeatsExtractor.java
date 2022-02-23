/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.extractors;

import nbbrd.design.Development;
import demetra.information.InformationMapping;
import demetra.modelling.ComponentInformation;
import demetra.modelling.ModellingDictionary;
import demetra.modelling.SeriesInfo;
import demetra.sa.ComponentType;
import demetra.sa.DecompositionMode;
import demetra.sa.SaDictionaries;
import demetra.timeseries.TsData;
import jdplus.tramoseats.TramoSeatsResults;
import demetra.information.InformationExtractor;
import jdplus.regsarima.regular.RegSarimaModel;
import jdplus.seats.SeatsResults;
import jdplus.tramoseats.TramoSeatsDiagnostics;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class TramoSeatsExtractor extends InformationMapping<TramoSeatsResults> {

    public static final String FINAL = "";

    public TramoSeatsExtractor() {
        set(SaDictionaries.MODE, DecompositionMode.class, source -> source.getFinals().getMode());

        set(ModellingDictionary.Y, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.Value));
//        set(ModellingDictionary.Y + SeriesInfo.F_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.Forecast));
//        set(ModellingDictionary.Y + SeriesInfo.EF_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.StdevForecast));
//        set(ModellingDictionary.Y + SeriesInfo.B_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.Backcast));
//        set(ModellingDictionary.Y + SeriesInfo.EB_SUFFIX, TsData.class, source
//                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.StdevBackcast));

        set(SaDictionaries.T, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.Value));
        set(SaDictionaries.T + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.Forecast));
        set(SaDictionaries.T + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.StdevForecast));
        set(SaDictionaries.T + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.Backcast));
        set(SaDictionaries.T + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.StdevBackcast));

        set(SaDictionaries.SA, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
        set(SaDictionaries.SA + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast));
        set(SaDictionaries.SA + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast));
        set(SaDictionaries.SA + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Backcast));
        set(SaDictionaries.SA + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevBackcast));

        set(SaDictionaries.S, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.Value));
        set(SaDictionaries.S + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.Forecast));
        set(SaDictionaries.S + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.StdevForecast));
        set(SaDictionaries.S + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.Backcast));
        set(SaDictionaries.S + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.StdevBackcast));

        set(SaDictionaries.I, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.Value));
        set(SaDictionaries.I + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.Forecast));
        set(SaDictionaries.I + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.StdevForecast));
        set(SaDictionaries.I + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.Backcast));
        set(SaDictionaries.I + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.StdevBackcast));

        delegate(SaDictionaries.DECOMPOSITION, SeatsResults.class, source -> source.getDecomposition());

        delegate(null, RegSarimaModel.class, source -> source.getPreprocessing());

        delegate(null, TramoSeatsDiagnostics.class, source -> source.getDiagnostics());
    }

    @Override
    public Class<TramoSeatsResults> getSourceClass() {
        return TramoSeatsResults.class;
    }
}
