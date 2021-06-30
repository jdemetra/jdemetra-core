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
import demetra.sa.SaDictionary;
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

        set(SaDictionary.T_LIN, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Trend, ComponentInformation.Value));
        set(SaDictionary.T_LIN + SeriesInfo.F_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Trend, ComponentInformation.Forecast));
        set(SaDictionary.T_LIN + SeriesInfo.EF_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Trend, ComponentInformation.StdevForecast));
        set(SaDictionary.T_LIN + SeriesInfo.B_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Trend, ComponentInformation.Backcast));
        set(SaDictionary.T_LIN + SeriesInfo.EB_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Trend, ComponentInformation.StdevBackcast));

        set(SaDictionary.SA_LIN, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
        set(SaDictionary.SA_LIN + SeriesInfo.F_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast));
        set(SaDictionary.SA_LIN + SeriesInfo.EF_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast));
        set(SaDictionary.SA_LIN + SeriesInfo.B_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Backcast));
        set(SaDictionary.SA_LIN + SeriesInfo.EB_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevBackcast));

        set(SaDictionary.S_LIN, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Value));
        set(SaDictionary.S_LIN + SeriesInfo.F_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Forecast));
        set(SaDictionary.S_LIN + SeriesInfo.EF_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Seasonal, ComponentInformation.StdevForecast));
        set(SaDictionary.S_LIN + SeriesInfo.B_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Backcast));
        set(SaDictionary.S_LIN + SeriesInfo.EB_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Seasonal, ComponentInformation.StdevBackcast));

        set(SaDictionary.I_LIN, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Irregular, ComponentInformation.Value));
        set(SaDictionary.I_LIN + SeriesInfo.F_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Irregular, ComponentInformation.Forecast));
        set(SaDictionary.I_LIN + SeriesInfo.EF_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Irregular, ComponentInformation.StdevForecast));
        set(SaDictionary.I_LIN + SeriesInfo.B_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Irregular, ComponentInformation.Backcast));
        set(SaDictionary.I_LIN + SeriesInfo.EB_SUFFIX, TsData.class, source -> source.getInitialComponents().getSeries(ComponentType.Irregular, ComponentInformation.StdevBackcast));

        set(ModellingDictionary.L, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Series, ComponentInformation.Value));
        set(ModellingDictionary.L + SeriesInfo.F_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Series, ComponentInformation.Forecast));
        set(ModellingDictionary.L + SeriesInfo.EF_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Series, ComponentInformation.StdevForecast));
        set(ModellingDictionary.L + SeriesInfo.B_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Series, ComponentInformation.Backcast));
        set(ModellingDictionary.L + SeriesInfo.EB_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Series, ComponentInformation.StdevBackcast));

        set(SaDictionary.T_CMP, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Trend, ComponentInformation.Value));
        set(SaDictionary.T_CMP + SeriesInfo.F_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Trend, ComponentInformation.Forecast));
        set(SaDictionary.T_CMP + SeriesInfo.EF_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Trend, ComponentInformation.StdevForecast));
        set(SaDictionary.T_CMP + SeriesInfo.B_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Trend, ComponentInformation.Backcast));
        set(SaDictionary.T_CMP + SeriesInfo.EB_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Trend, ComponentInformation.StdevBackcast));

        set(SaDictionary.SA_CMP, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
        set(SaDictionary.SA_CMP + SeriesInfo.F_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast));
        set(SaDictionary.SA_CMP + SeriesInfo.EF_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast));
        set(SaDictionary.SA_CMP + SeriesInfo.B_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Backcast));
        set(SaDictionary.SA_CMP + SeriesInfo.EB_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevBackcast));

        set(SaDictionary.S_CMP, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Value));
        set(SaDictionary.S_CMP + SeriesInfo.F_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Forecast));
        set(SaDictionary.S_CMP + SeriesInfo.EF_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Seasonal, ComponentInformation.StdevForecast));
        set(SaDictionary.S_CMP + SeriesInfo.B_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Backcast));
        set(SaDictionary.S_CMP + SeriesInfo.EB_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Seasonal, ComponentInformation.StdevBackcast));

        set(SaDictionary.I_CMP, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Irregular, ComponentInformation.Value));
        set(SaDictionary.I_CMP + SeriesInfo.F_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Irregular, ComponentInformation.Forecast));
        set(SaDictionary.I_CMP + SeriesInfo.EF_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Irregular, ComponentInformation.StdevForecast));
        set(SaDictionary.I_CMP + SeriesInfo.B_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Irregular, ComponentInformation.Backcast));
        set(SaDictionary.I_CMP + SeriesInfo.EB_SUFFIX, TsData.class, source -> source.getFinalComponents().getSeries(ComponentType.Irregular, ComponentInformation.StdevBackcast));

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
