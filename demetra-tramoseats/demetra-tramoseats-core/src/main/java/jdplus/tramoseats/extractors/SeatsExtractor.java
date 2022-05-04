/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.extractors;

import demetra.information.InformationExtractor;
import nbbrd.design.Development;
import demetra.information.InformationMapping;
import demetra.modelling.ComponentInformation;
import demetra.modelling.ModellingDictionary;
import demetra.modelling.SeriesInfo;
import demetra.sa.ComponentType;
import demetra.sa.SaDictionaries;
import demetra.timeseries.TsData;
import jdplus.arima.IArimaModel;
import jdplus.sarima.SarimaModel;
import jdplus.seats.SeatsResults;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class SeatsExtractor extends InformationMapping<SeatsResults>{

    public SeatsExtractor(){
        set(ModellingDictionary.Y_LIN, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Series, ComponentInformation.Value));
        set(ModellingDictionary.Y_LIN + SeriesInfo.F_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Series, ComponentInformation.Forecast));
        set(ModellingDictionary.Y_LIN + SeriesInfo.EF_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Series, ComponentInformation.StdevForecast));
        set(ModellingDictionary.Y_LIN + SeriesInfo.B_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Series, ComponentInformation.Backcast));
        set(ModellingDictionary.Y_LIN + SeriesInfo.EB_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Series, ComponentInformation.StdevBackcast));

        set(SaDictionaries.T_LIN, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Trend, ComponentInformation.Value));
        set(SaDictionaries.T_LIN_E, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Trend, ComponentInformation.Stdev));
        set(SaDictionaries.T_LIN + SeriesInfo.F_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Trend, ComponentInformation.Forecast));
        set(SaDictionaries.T_LIN + SeriesInfo.EF_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Trend, ComponentInformation.StdevForecast));
        set(SaDictionaries.T_LIN + SeriesInfo.B_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Trend, ComponentInformation.Backcast));
        set(SaDictionaries.T_LIN + SeriesInfo.EB_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Trend, ComponentInformation.StdevBackcast));

        set(SaDictionaries.SA_LIN, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
        set(SaDictionaries.SA_LIN_E, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Stdev));
        set(SaDictionaries.SA_LIN + SeriesInfo.F_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast));
        set(SaDictionaries.SA_LIN + SeriesInfo.EF_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast));
        set(SaDictionaries.SA_LIN + SeriesInfo.B_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Backcast));
        set(SaDictionaries.SA_LIN + SeriesInfo.EB_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevBackcast));

        set(SaDictionaries.S_LIN, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Value));
        set(SaDictionaries.S_LIN_E, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Stdev));
        set(SaDictionaries.S_LIN + SeriesInfo.F_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Forecast));
        set(SaDictionaries.S_LIN + SeriesInfo.EF_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Seasonal, ComponentInformation.StdevForecast));
        set(SaDictionaries.S_LIN + SeriesInfo.B_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Backcast));
        set(SaDictionaries.S_LIN + SeriesInfo.EB_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Seasonal, ComponentInformation.StdevBackcast));

        set(SaDictionaries.I_LIN, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Irregular, ComponentInformation.Value));
        set(SaDictionaries.I_LIN_E, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Irregular, ComponentInformation.Stdev));
        set(SaDictionaries.I_LIN + SeriesInfo.F_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Irregular, ComponentInformation.Forecast));
        set(SaDictionaries.I_LIN + SeriesInfo.EF_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Irregular, ComponentInformation.StdevForecast));
        set(SaDictionaries.I_LIN + SeriesInfo.B_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Irregular, ComponentInformation.Backcast));
        set(SaDictionaries.I_LIN + SeriesInfo.EB_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Irregular, ComponentInformation.StdevBackcast));

        set(SaDictionaries.Y_CMP, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Series, ComponentInformation.Value));
        set(SaDictionaries.Y_CMP + SeriesInfo.F_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Series, ComponentInformation.Forecast));
        set(SaDictionaries.Y_CMP + SeriesInfo.EF_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Series, ComponentInformation.StdevForecast));
        set(SaDictionaries.Y_CMP + SeriesInfo.B_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Series, ComponentInformation.Backcast));
        set(SaDictionaries.Y_CMP + SeriesInfo.EB_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Series, ComponentInformation.StdevBackcast));

        set(SaDictionaries.T_CMP, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Trend, ComponentInformation.Value));
        set(SaDictionaries.T_CMP + SeriesInfo.F_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Trend, ComponentInformation.Forecast));
        set(SaDictionaries.T_CMP + SeriesInfo.EF_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Trend, ComponentInformation.StdevForecast));
        set(SaDictionaries.T_CMP + SeriesInfo.B_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Trend, ComponentInformation.Backcast));
        set(SaDictionaries.T_CMP + SeriesInfo.EB_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Trend, ComponentInformation.StdevBackcast));

        set(SaDictionaries.SA_CMP, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
        set(SaDictionaries.SA_CMP + SeriesInfo.F_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast));
        set(SaDictionaries.SA_CMP + SeriesInfo.EF_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast));
        set(SaDictionaries.SA_CMP + SeriesInfo.B_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Backcast));
        set(SaDictionaries.SA_CMP + SeriesInfo.EB_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevBackcast));

        set(SaDictionaries.S_CMP, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Value));
        set(SaDictionaries.S_CMP + SeriesInfo.F_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Forecast));
        set(SaDictionaries.S_CMP + SeriesInfo.EF_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Seasonal, ComponentInformation.StdevForecast));
        set(SaDictionaries.S_CMP + SeriesInfo.B_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Backcast));
        set(SaDictionaries.S_CMP + SeriesInfo.EB_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Seasonal, ComponentInformation.StdevBackcast));

        set(SaDictionaries.I_CMP, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Irregular, ComponentInformation.Value));
        set(SaDictionaries.I_CMP + SeriesInfo.F_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Irregular, ComponentInformation.Forecast));
        set(SaDictionaries.I_CMP + SeriesInfo.EF_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Irregular, ComponentInformation.StdevForecast));
        set(SaDictionaries.I_CMP + SeriesInfo.B_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Irregular, ComponentInformation.Backcast));
        set(SaDictionaries.I_CMP + SeriesInfo.EB_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Irregular, ComponentInformation.StdevBackcast));

        delegate("model", IArimaModel.class, source -> source.getUcarimaModel().getModel());
        delegate("tmodel", IArimaModel.class, source -> source.getUcarimaModel().getComponent(0));
        delegate("smodel", IArimaModel.class, source -> source.getUcarimaModel().getComponent(1));
        delegate("samodel", IArimaModel.class, source -> source.getUcarimaModel().getComponent(1));
        delegate("transitorymodel", IArimaModel.class, source -> source.getUcarimaModel().getComponent(2));
        delegate("imodel", IArimaModel.class, source -> source.getUcarimaModel().getComponent(3));

        delegate("initialmodel", SarimaModel.class, source -> source.getOriginalModel());
        delegate("finalmodel", SarimaModel.class, source -> source.getFinalModel());
    }


    @Override
    public Class<SeatsResults> getSourceClass() {
        return SeatsResults.class;
    }

}
