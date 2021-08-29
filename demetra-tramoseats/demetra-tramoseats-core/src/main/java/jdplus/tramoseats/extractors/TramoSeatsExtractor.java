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
import demetra.information.BasicInformationExtractor;
import demetra.information.InformationExtractor;
import jdplus.arima.IArimaModel;
import jdplus.regsarima.regular.RegSarimaModel;
import jdplus.sarima.SarimaModel;
import jdplus.tramoseats.TramoSeatsDiagnostics;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class TramoSeatsExtractor extends InformationMapping<TramoSeatsResults> {

    private final String DECOMP = "decomposition" + BasicInformationExtractor.SEP, FINAL = "";

    public TramoSeatsExtractor() {
        set(SaDictionary.MODE, DecompositionMode.class, source -> source.getFinals().getMode());
        set(DECOMP + ModellingDictionary.Y_LIN, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Series, ComponentInformation.Value));
        set(DECOMP + ModellingDictionary.Y_LIN + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Series, ComponentInformation.Forecast));
        set(DECOMP + ModellingDictionary.Y_LIN + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Series, ComponentInformation.StdevForecast));
        set(DECOMP + ModellingDictionary.Y_LIN + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Series, ComponentInformation.Backcast));
        set(DECOMP + ModellingDictionary.Y_LIN + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Series, ComponentInformation.StdevBackcast));

        set(DECOMP + SaDictionary.T_LIN, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Trend, ComponentInformation.Value));
        set(DECOMP + SaDictionary.T_LIN + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Trend, ComponentInformation.Forecast));
        set(DECOMP + SaDictionary.T_LIN + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Trend, ComponentInformation.StdevForecast));
        set(DECOMP + SaDictionary.T_LIN + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Trend, ComponentInformation.Backcast));
        set(DECOMP + SaDictionary.T_LIN + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Trend, ComponentInformation.StdevBackcast));

        set(DECOMP + SaDictionary.SA_LIN, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
        set(DECOMP + SaDictionary.SA_LIN + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast));
        set(DECOMP + SaDictionary.SA_LIN + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast));
        set(DECOMP + SaDictionary.SA_LIN + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Backcast));
        set(DECOMP + SaDictionary.SA_LIN + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevBackcast));

        set(DECOMP + SaDictionary.S_LIN, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Value));
        set(DECOMP + SaDictionary.S_LIN + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Forecast));
        set(DECOMP + SaDictionary.S_LIN + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Seasonal, ComponentInformation.StdevForecast));
        set(DECOMP + SaDictionary.S_LIN + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Backcast));
        set(DECOMP + SaDictionary.S_LIN + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Seasonal, ComponentInformation.StdevBackcast));

        set(DECOMP + SaDictionary.I_LIN, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Irregular, ComponentInformation.Value));
        set(DECOMP + SaDictionary.I_LIN + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Irregular, ComponentInformation.Forecast));
        set(DECOMP + SaDictionary.I_LIN + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Irregular, ComponentInformation.StdevForecast));
        set(DECOMP + SaDictionary.I_LIN + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Irregular, ComponentInformation.Backcast));
        set(DECOMP + SaDictionary.I_LIN + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Irregular, ComponentInformation.StdevBackcast));

        set(DECOMP + ModellingDictionary.L, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Series, ComponentInformation.Value));
        set(DECOMP + ModellingDictionary.L + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Series, ComponentInformation.Forecast));
        set(DECOMP + ModellingDictionary.L + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Series, ComponentInformation.StdevForecast));
        set(DECOMP + ModellingDictionary.L + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Series, ComponentInformation.Backcast));
        set(DECOMP + ModellingDictionary.L + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Series, ComponentInformation.StdevBackcast));

        set(DECOMP + SaDictionary.T_CMP, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Trend, ComponentInformation.Value));
        set(DECOMP + SaDictionary.T_CMP + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Trend, ComponentInformation.Forecast));
        set(DECOMP + SaDictionary.T_CMP + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Trend, ComponentInformation.StdevForecast));
        set(DECOMP + SaDictionary.T_CMP + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Trend, ComponentInformation.Backcast));
        set(DECOMP + SaDictionary.T_CMP + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Trend, ComponentInformation.StdevBackcast));

        set(DECOMP + SaDictionary.SA_CMP, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
        set(DECOMP + SaDictionary.SA_CMP + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast));
        set(DECOMP + SaDictionary.SA_CMP + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast));
        set(DECOMP + SaDictionary.SA_CMP + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Backcast));
        set(DECOMP + SaDictionary.SA_CMP + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevBackcast));

        set(DECOMP + SaDictionary.S_CMP, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Value));
        set(DECOMP + SaDictionary.S_CMP + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Forecast));
        set(DECOMP + SaDictionary.S_CMP + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Seasonal, ComponentInformation.StdevForecast));
        set(DECOMP + SaDictionary.S_CMP + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Backcast));
        set(DECOMP + SaDictionary.S_CMP + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Seasonal, ComponentInformation.StdevBackcast));

        set(DECOMP + SaDictionary.I_CMP, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Irregular, ComponentInformation.Value));
        set(DECOMP + SaDictionary.I_CMP + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Irregular, ComponentInformation.Forecast));
        set(DECOMP + SaDictionary.I_CMP + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Irregular, ComponentInformation.StdevForecast));
        set(DECOMP + SaDictionary.I_CMP + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Irregular, ComponentInformation.Backcast));
        set(DECOMP + SaDictionary.I_CMP + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Irregular, ComponentInformation.StdevBackcast));

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

        delegate(DECOMP + "model", IArimaModel.class, source
                -> source.getDecomposition().getUcarimaModel().getModel());
        delegate(DECOMP + "tmodel", IArimaModel.class, source
                -> source.getDecomposition().getUcarimaModel().getComponent(0));
        delegate(DECOMP + "smodel", IArimaModel.class, source
                -> source.getDecomposition().getUcarimaModel().getComponent(1));
        delegate(DECOMP + "samodel", IArimaModel.class, source
                -> source.getDecomposition().getUcarimaModel().getComplement(1));
        delegate(DECOMP + "transitorymodel", IArimaModel.class, source
                -> source.getDecomposition().getUcarimaModel().getComponent(2));
        delegate(DECOMP + "imodel", IArimaModel.class, source
                -> source.getDecomposition().getUcarimaModel().getComponent(3));

        delegate(DECOMP + "initialmodel", SarimaModel.class, source
                -> source.getDecomposition().getOriginalModel());
        delegate(DECOMP + "finalmodel", SarimaModel.class, source
                -> source.getDecomposition().getFinalModel());

        delegate(null, RegSarimaModel.class, source -> source.getPreprocessing());

        delegate(null, TramoSeatsDiagnostics.class, source -> source.getDiagnostics());
    }

    @Override
    public Class<TramoSeatsResults> getSourceClass() {
        return TramoSeatsResults.class;
    }
}
