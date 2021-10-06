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
import demetra.sa.SaDictionary;
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
        set(SaDictionary.MODE, DecompositionMode.class, source -> source.getFinals().getMode());

        set(FINAL + ModellingDictionary.Y, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.Value));
        set(FINAL + ModellingDictionary.Y + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.Forecast));
        set(FINAL + ModellingDictionary.Y + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.StdevForecast));
        set(FINAL + ModellingDictionary.Y + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.Backcast));
        set(FINAL + ModellingDictionary.Y + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.StdevBackcast));

        set(FINAL + SaDictionary.T, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.Value));
        set(FINAL + SaDictionary.T + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.Forecast));
        set(FINAL + SaDictionary.T + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.StdevForecast));
        set(FINAL + SaDictionary.T + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.Backcast));
        set(FINAL + SaDictionary.T + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.StdevBackcast));

        set(FINAL + SaDictionary.SA, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
        set(FINAL + SaDictionary.SA + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast));
        set(FINAL + SaDictionary.SA + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast));
        set(FINAL + SaDictionary.SA + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Backcast));
        set(FINAL + SaDictionary.SA + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevBackcast));

        set(FINAL + SaDictionary.S, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.Value));
        set(FINAL + SaDictionary.S + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.Forecast));
        set(FINAL + SaDictionary.S + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.StdevForecast));
        set(FINAL + SaDictionary.S + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.Backcast));
        set(FINAL + SaDictionary.S + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.StdevBackcast));

        set(FINAL + SaDictionary.I, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.Value));
        set(FINAL + SaDictionary.I + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.Forecast));
        set(FINAL + SaDictionary.I + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.StdevForecast));
        set(FINAL + SaDictionary.I + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.Backcast));
        set(FINAL + SaDictionary.I + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.StdevBackcast));

        delegate(SaDictionary.DECOMPOSITION, SeatsResults.class, source -> source.getDecomposition());

        delegate(null, RegSarimaModel.class, source -> source.getPreprocessing());

        delegate(null, TramoSeatsDiagnostics.class, source -> source.getDiagnostics());
    }

    @Override
    public Class<TramoSeatsResults> getSourceClass() {
        return TramoSeatsResults.class;
    }
}
