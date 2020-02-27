/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.descrptors.tramoseats;

import demetra.data.DoubleSeq;
import demetra.descriptors.arima.ArimaDescriptor;
import demetra.descriptors.arima.SarimaDescriptor;
import demetra.design.Development;
import demetra.information.InformationMapping;
import demetra.modelling.ComponentInformation;
import demetra.modelling.ModellingDictionary;
import demetra.modelling.SeriesInfo;
import demetra.sa.ComponentType;
import demetra.sa.SaDictionary;
import demetra.seats.SeatsResults;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@lombok.experimental.UtilityClass
public class SeatsDescriptor {

    private final InformationMapping<SeatsResults> MAPPING = new InformationMapping<>(SeatsResults.class);

    private double[] asArray(DoubleSeq d) {
        return d == null ? null : d.toArray();
    }

    static {
        MAPPING.set(ModellingDictionary.Y_LIN, double[].class, source -> asArray(source.getInitialComponents().getSeries(ComponentType.Series, ComponentInformation.Value)));
        MAPPING.set(ModellingDictionary.Y_LIN + SeriesInfo.F_SUFFIX, double[].class, source -> asArray(source.getInitialComponents().getSeries(ComponentType.Series, ComponentInformation.Forecast)));
        MAPPING.set(ModellingDictionary.Y_LIN + SeriesInfo.EF_SUFFIX, double[].class, source -> asArray(source.getInitialComponents().getSeries(ComponentType.Series, ComponentInformation.StdevForecast)));
        MAPPING.set(ModellingDictionary.Y_LIN + SeriesInfo.B_SUFFIX, double[].class, source -> asArray(source.getInitialComponents().getSeries(ComponentType.Series, ComponentInformation.Backcast)));
        MAPPING.set(ModellingDictionary.Y_LIN + SeriesInfo.EB_SUFFIX, double[].class, source -> asArray(source.getInitialComponents().getSeries(ComponentType.Series, ComponentInformation.StdevBackcast)));

        MAPPING.set(SaDictionary.T_LIN, double[].class, source -> asArray(source.getInitialComponents().getSeries(ComponentType.Trend, ComponentInformation.Value)));
        MAPPING.set(SaDictionary.T_LIN + SeriesInfo.F_SUFFIX, double[].class, source -> asArray(source.getInitialComponents().getSeries(ComponentType.Trend, ComponentInformation.Forecast)));
        MAPPING.set(SaDictionary.T_LIN + SeriesInfo.EF_SUFFIX, double[].class, source -> asArray(source.getInitialComponents().getSeries(ComponentType.Trend, ComponentInformation.StdevForecast)));
        MAPPING.set(SaDictionary.T_LIN + SeriesInfo.B_SUFFIX, double[].class, source -> asArray(source.getInitialComponents().getSeries(ComponentType.Trend, ComponentInformation.Backcast)));
        MAPPING.set(SaDictionary.T_LIN + SeriesInfo.EB_SUFFIX, double[].class, source -> asArray(source.getInitialComponents().getSeries(ComponentType.Trend, ComponentInformation.StdevBackcast)));

        MAPPING.set(SaDictionary.SA_LIN, double[].class, source -> asArray(source.getInitialComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value)));
        MAPPING.set(SaDictionary.SA_LIN + SeriesInfo.F_SUFFIX, double[].class, source -> asArray(source.getInitialComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast)));
        MAPPING.set(SaDictionary.SA_LIN + SeriesInfo.EF_SUFFIX, double[].class, source -> asArray(source.getInitialComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast)));
        MAPPING.set(SaDictionary.SA_LIN + SeriesInfo.B_SUFFIX, double[].class, source -> asArray(source.getInitialComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Backcast)));
        MAPPING.set(SaDictionary.SA_LIN + SeriesInfo.EB_SUFFIX, double[].class, source -> asArray(source.getInitialComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevBackcast)));

        MAPPING.set(SaDictionary.S_LIN, double[].class, source -> asArray(source.getInitialComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Value)));
        MAPPING.set(SaDictionary.S_LIN + SeriesInfo.F_SUFFIX, double[].class, source -> asArray(source.getInitialComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Forecast)));
        MAPPING.set(SaDictionary.S_LIN + SeriesInfo.EF_SUFFIX, double[].class, source -> asArray(source.getInitialComponents().getSeries(ComponentType.Seasonal, ComponentInformation.StdevForecast)));
        MAPPING.set(SaDictionary.S_LIN + SeriesInfo.B_SUFFIX, double[].class, source -> asArray(source.getInitialComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Backcast)));
        MAPPING.set(SaDictionary.S_LIN + SeriesInfo.EB_SUFFIX, double[].class, source -> asArray(source.getInitialComponents().getSeries(ComponentType.Seasonal, ComponentInformation.StdevBackcast)));

        MAPPING.set(SaDictionary.I_LIN, double[].class, source -> asArray(source.getInitialComponents().getSeries(ComponentType.Irregular, ComponentInformation.Value)));
        MAPPING.set(SaDictionary.I_LIN + SeriesInfo.F_SUFFIX, double[].class, source -> asArray(source.getInitialComponents().getSeries(ComponentType.Irregular, ComponentInformation.Forecast)));
        MAPPING.set(SaDictionary.I_LIN + SeriesInfo.EF_SUFFIX, double[].class, source -> asArray(source.getInitialComponents().getSeries(ComponentType.Irregular, ComponentInformation.StdevForecast)));
        MAPPING.set(SaDictionary.I_LIN + SeriesInfo.B_SUFFIX, double[].class, source -> asArray(source.getInitialComponents().getSeries(ComponentType.Irregular, ComponentInformation.Backcast)));
        MAPPING.set(SaDictionary.I_LIN + SeriesInfo.EB_SUFFIX, double[].class, source -> asArray(source.getInitialComponents().getSeries(ComponentType.Irregular, ComponentInformation.StdevBackcast)));

        MAPPING.set(ModellingDictionary.L, double[].class, source -> asArray(source.getFinalComponents().getSeries(ComponentType.Series, ComponentInformation.Value)));
        MAPPING.set(ModellingDictionary.L + SeriesInfo.F_SUFFIX, double[].class, source -> asArray(source.getFinalComponents().getSeries(ComponentType.Series, ComponentInformation.Forecast)));
        MAPPING.set(ModellingDictionary.L + SeriesInfo.EF_SUFFIX, double[].class, source -> asArray(source.getFinalComponents().getSeries(ComponentType.Series, ComponentInformation.StdevForecast)));
        MAPPING.set(ModellingDictionary.L + SeriesInfo.B_SUFFIX, double[].class, source -> asArray(source.getFinalComponents().getSeries(ComponentType.Series, ComponentInformation.Backcast)));
        MAPPING.set(ModellingDictionary.L + SeriesInfo.EB_SUFFIX, double[].class, source -> asArray(source.getFinalComponents().getSeries(ComponentType.Series, ComponentInformation.StdevBackcast)));

        MAPPING.set(SaDictionary.T_CMP, double[].class, source -> asArray(source.getFinalComponents().getSeries(ComponentType.Trend, ComponentInformation.Value)));
        MAPPING.set(SaDictionary.T_CMP + SeriesInfo.F_SUFFIX, double[].class, source -> asArray(source.getFinalComponents().getSeries(ComponentType.Trend, ComponentInformation.Forecast)));
        MAPPING.set(SaDictionary.T_CMP + SeriesInfo.EF_SUFFIX, double[].class, source -> asArray(source.getFinalComponents().getSeries(ComponentType.Trend, ComponentInformation.StdevForecast)));
        MAPPING.set(SaDictionary.T_CMP + SeriesInfo.B_SUFFIX, double[].class, source -> asArray(source.getFinalComponents().getSeries(ComponentType.Trend, ComponentInformation.Backcast)));
        MAPPING.set(SaDictionary.T_CMP + SeriesInfo.EB_SUFFIX, double[].class, source -> asArray(source.getFinalComponents().getSeries(ComponentType.Trend, ComponentInformation.StdevBackcast)));

        MAPPING.set(SaDictionary.SA_CMP, double[].class, source -> asArray(source.getFinalComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value)));
        MAPPING.set(SaDictionary.SA_CMP + SeriesInfo.F_SUFFIX, double[].class, source -> asArray(source.getFinalComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast)));
        MAPPING.set(SaDictionary.SA_CMP + SeriesInfo.EF_SUFFIX, double[].class, source -> asArray(source.getFinalComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast)));
        MAPPING.set(SaDictionary.SA_CMP + SeriesInfo.B_SUFFIX, double[].class, source -> asArray(source.getFinalComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Backcast)));
        MAPPING.set(SaDictionary.SA_CMP + SeriesInfo.EB_SUFFIX, double[].class, source -> asArray(source.getFinalComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevBackcast)));

        MAPPING.set(SaDictionary.S_CMP, double[].class, source -> asArray(source.getFinalComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Value)));
        MAPPING.set(SaDictionary.S_CMP + SeriesInfo.F_SUFFIX, double[].class, source -> asArray(source.getFinalComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Forecast)));
        MAPPING.set(SaDictionary.S_CMP + SeriesInfo.EF_SUFFIX, double[].class, source -> asArray(source.getFinalComponents().getSeries(ComponentType.Seasonal, ComponentInformation.StdevForecast)));
        MAPPING.set(SaDictionary.S_CMP + SeriesInfo.B_SUFFIX, double[].class, source -> asArray(source.getFinalComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Backcast)));
        MAPPING.set(SaDictionary.S_CMP + SeriesInfo.EB_SUFFIX, double[].class, source -> asArray(source.getFinalComponents().getSeries(ComponentType.Seasonal, ComponentInformation.StdevBackcast)));

        MAPPING.set(SaDictionary.I_CMP, double[].class, source -> asArray(source.getFinalComponents().getSeries(ComponentType.Irregular, ComponentInformation.Value)));
        MAPPING.set(SaDictionary.I_CMP + SeriesInfo.F_SUFFIX, double[].class, source -> asArray(source.getFinalComponents().getSeries(ComponentType.Irregular, ComponentInformation.Forecast)));
        MAPPING.set(SaDictionary.I_CMP + SeriesInfo.EF_SUFFIX, double[].class, source -> asArray(source.getFinalComponents().getSeries(ComponentType.Irregular, ComponentInformation.StdevForecast)));
        MAPPING.set(SaDictionary.I_CMP + SeriesInfo.B_SUFFIX, double[].class, source -> asArray(source.getFinalComponents().getSeries(ComponentType.Irregular, ComponentInformation.Backcast)));
        MAPPING.set(SaDictionary.I_CMP + SeriesInfo.EB_SUFFIX, double[].class, source -> asArray(source.getFinalComponents().getSeries(ComponentType.Irregular, ComponentInformation.StdevBackcast)));

        MAPPING.delegate("model", ArimaDescriptor.getMapping(), source -> source.getDecomposition().getSum());
        MAPPING.delegate("tmodel", ArimaDescriptor.getMapping(), source -> source.getDecomposition().getComponent(0));
        MAPPING.delegate("smodel", ArimaDescriptor.getMapping(), source -> source.getDecomposition().getComponent(1));
//        MAPPING.delegate("samodel", ArimaDescriptor.getMapping(), source -> source.getDecomposition().getComponent(1));
        MAPPING.delegate("transitorymodel", ArimaDescriptor.getMapping(), source -> source.getDecomposition().getComponent(2));
        MAPPING.delegate("imodel", ArimaDescriptor.getMapping(), source -> source.getDecomposition().getComponent(3));

        MAPPING.delegate("initialmodel", SarimaDescriptor.getMapping(), source -> source.getInitialModel());
        MAPPING.delegate("finalmodel", SarimaDescriptor.getMapping(), source -> source.getFinalModel());
    }
 
    public InformationMapping<SeatsResults> getMapping() {
        return MAPPING;
    }

}
