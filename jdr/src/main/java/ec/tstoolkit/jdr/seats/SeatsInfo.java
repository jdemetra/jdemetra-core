/*
 * Copyright 2017 National Bank of Belgium
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
package ec.tstoolkit.jdr.seats;

import demetra.information.InformationMapping;
import ec.satoolkit.seats.SeatsResults;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.jdr.mapping.ArimaInfo;
import ec.tstoolkit.modelling.ComponentInformation;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.modelling.SeriesInfo;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class SeatsInfo {

    final InformationMapping<SeatsResults> MAPPING = new InformationMapping<>(SeatsResults.class);
    public static final String CUTOFF = "parameterscutoff", CHANGED = "modelchanged", SEAS = "seasonality", AR_ROOT = "arroot";

    static {
        MAPPING.set(ModellingDictionary.Y_LIN, TsData.class, source -> source.getComponents().getSeries(ComponentType.Series, ComponentInformation.Value));
        MAPPING.set(ModellingDictionary.Y_LIN+SeriesInfo.F_SUFFIX, TsData.class, source -> source.getComponents().getSeries(ComponentType.Series, ComponentInformation.Forecast));
        MAPPING.set(ModellingDictionary.Y_LIN+SeriesInfo.EF_SUFFIX, TsData.class, source -> source.getComponents().getSeries(ComponentType.Series, ComponentInformation.StdevForecast));
        MAPPING.set(ModellingDictionary.T_LIN, TsData.class, source -> source.getComponents().getSeries(ComponentType.Trend, ComponentInformation.Value));
        MAPPING.set(ModellingDictionary.T_LIN + SeriesInfo.F_SUFFIX, TsData.class,
                source -> source.getComponents().getSeries(ComponentType.Trend, ComponentInformation.Forecast));
        MAPPING.set(ModellingDictionary.T_LIN + SeriesInfo.E_SUFFIX, TsData.class,
                source -> source.getComponents().getSeries(ComponentType.Trend, ComponentInformation.Stdev));
        MAPPING.set(ModellingDictionary.T_LIN + SeriesInfo.EF_SUFFIX, TsData.class,
                source -> source.getComponents().getSeries(ComponentType.Trend, ComponentInformation.StdevForecast));
        MAPPING.set(ModellingDictionary.SA_LIN, TsData.class, source -> source.getComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
        MAPPING.set(ModellingDictionary.SA_LIN + SeriesInfo.F_SUFFIX, TsData.class,
                source -> source.getComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast));
        MAPPING.set(ModellingDictionary.SA_LIN + SeriesInfo.E_SUFFIX, TsData.class,
                source -> source.getComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Stdev));
        MAPPING.set(ModellingDictionary.SA_LIN + SeriesInfo.EF_SUFFIX, TsData.class,
                source -> source.getComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast));
        MAPPING.set(ModellingDictionary.S_LIN, TsData.class, source -> source.getComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Value));
        MAPPING.set(ModellingDictionary.S_LIN + SeriesInfo.F_SUFFIX, TsData.class,
                source -> source.getComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Forecast));
        MAPPING.set(ModellingDictionary.S_LIN + SeriesInfo.E_SUFFIX, TsData.class,
                source -> source.getComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Stdev));
        MAPPING.set(ModellingDictionary.S_LIN + SeriesInfo.EF_SUFFIX, TsData.class,
                source -> source.getComponents().getSeries(ComponentType.Seasonal, ComponentInformation.StdevForecast));
        MAPPING.set(ModellingDictionary.I_LIN, TsData.class, source -> source.getComponents().getSeries(ComponentType.Irregular, ComponentInformation.Value));
        MAPPING.set(ModellingDictionary.I_LIN + SeriesInfo.F_SUFFIX, TsData.class,
                source -> source.getComponents().getSeries(ComponentType.Irregular, ComponentInformation.Forecast));
        MAPPING.set(ModellingDictionary.I_LIN + SeriesInfo.E_SUFFIX, TsData.class,
                source -> source.getComponents().getSeries(ComponentType.Irregular, ComponentInformation.Stdev));
        MAPPING.set(ModellingDictionary.I_LIN + SeriesInfo.EF_SUFFIX, TsData.class,
                source -> source.getComponents().getSeries(ComponentType.Irregular, ComponentInformation.StdevForecast));
        MAPPING.set(ModellingDictionary.Y_CMP, TsData.class, source -> source.getSeriesDecomposition().getSeries(ComponentType.Series, ComponentInformation.Value));
        MAPPING.set(ModellingDictionary.Y_CMP + SeriesInfo.F_SUFFIX, TsData.class,
                source -> source.getSeriesDecomposition().getSeries(ComponentType.Series, ComponentInformation.Forecast));
        MAPPING.set(ModellingDictionary.T_CMP, TsData.class, source -> source.getSeriesDecomposition().getSeries(ComponentType.Trend, ComponentInformation.Value));
        MAPPING.set(ModellingDictionary.T_CMP + SeriesInfo.F_SUFFIX, TsData.class,
                source -> source.getSeriesDecomposition().getSeries(ComponentType.Trend, ComponentInformation.Forecast));
        MAPPING.set(ModellingDictionary.SA_CMP, TsData.class, source -> source.getSeriesDecomposition().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
        MAPPING.set(ModellingDictionary.SA_CMP + SeriesInfo.F_SUFFIX, TsData.class,
                source -> source.getSeriesDecomposition().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast));
        MAPPING.set(ModellingDictionary.S_CMP, TsData.class, source -> source.getSeriesDecomposition().getSeries(ComponentType.Seasonal, ComponentInformation.Value));
        MAPPING.set(ModellingDictionary.S_CMP + SeriesInfo.F_SUFFIX, TsData.class,
                source -> source.getSeriesDecomposition().getSeries(ComponentType.Seasonal, ComponentInformation.Forecast));
        MAPPING.set(ModellingDictionary.I_CMP, TsData.class, source -> source.getSeriesDecomposition().getSeries(ComponentType.Irregular, ComponentInformation.Value));
        MAPPING.set(ModellingDictionary.I_CMP + SeriesInfo.F_SUFFIX, TsData.class,
                source -> source.getSeriesDecomposition().getSeries(ComponentType.Irregular, ComponentInformation.Forecast));
        MAPPING.set(ModellingDictionary.I_CMP + SeriesInfo.E_SUFFIX, TsData.class,
                source -> source.getSeriesDecomposition().getSeries(ComponentType.Irregular, ComponentInformation.Stdev));
        MAPPING.set(ModellingDictionary.T_CMP + SeriesInfo.E_SUFFIX, TsData.class,
                source -> source.getSeriesDecomposition().getSeries(ComponentType.Trend, ComponentInformation.Stdev));
        MAPPING.set(ModellingDictionary.S_CMP + SeriesInfo.E_SUFFIX, TsData.class,
                source -> source.getSeriesDecomposition().getSeries(ComponentType.Seasonal, ComponentInformation.Stdev));
        MAPPING.set(ModellingDictionary.SA_CMP + SeriesInfo.E_SUFFIX, TsData.class,
                source -> source.getSeriesDecomposition().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Stdev));
        MAPPING.set(ModellingDictionary.I_CMP + SeriesInfo.EF_SUFFIX, TsData.class,
                source -> source.getSeriesDecomposition().getSeries(ComponentType.Irregular, ComponentInformation.StdevForecast));
        MAPPING.set(ModellingDictionary.T_CMP + SeriesInfo.EF_SUFFIX, TsData.class,
                source -> source.getSeriesDecomposition().getSeries(ComponentType.Trend, ComponentInformation.StdevForecast));
        MAPPING.set(ModellingDictionary.S_CMP + SeriesInfo.EF_SUFFIX, TsData.class,
                source -> source.getSeriesDecomposition().getSeries(ComponentType.Seasonal, ComponentInformation.StdevForecast));
        MAPPING.set(ModellingDictionary.SA_CMP + SeriesInfo.EF_SUFFIX, TsData.class,
                source -> source.getSeriesDecomposition().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast));

        MAPPING.set(CUTOFF, Boolean.class, source -> source.getModel().isCutOff());
        MAPPING.set(CHANGED, Boolean.class, source -> source.getModel().isChanged());

        MAPPING.delegate("model", ArimaInfo.getMapping(), source -> ArimaModel.create(source.getUcarimaModel().getModel()));
        MAPPING.delegate("tmodel", ArimaInfo.getMapping(), source -> model(source.getUcarimaModel().getComponent(0)));
        MAPPING.delegate("smodel", ArimaInfo.getMapping(), source -> model(source.getUcarimaModel().getComponent(1)));
        MAPPING.delegate("samodel", ArimaInfo.getMapping(), source -> model(source.getUcarimaModel().getComplement(1)));
        MAPPING.delegate("transitorymodel", ArimaInfo.getMapping(), source ->source.getUcarimaModel().getComponentsCount() == 3 ? null : model(source.getUcarimaModel().getComponent(2)));
        MAPPING.delegate("imodel", ArimaInfo.getMapping(), source -> model(source.getUcarimaModel().getComponent(source.getUcarimaModel().getComponentsCount()-1)));
    }
    
    private static ArimaModel model(ArimaModel m){
        return m.isNull() ? null : m;
    }

    public InformationMapping<SeatsResults> getMapping() {
        return MAPPING;
    }

}
