/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.extractors;

import nbbrd.design.Development;
import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import demetra.modelling.ComponentInformation;
import demetra.modelling.ModellingDictionary;
import demetra.modelling.SeriesInfo;
import demetra.sa.ComponentType;
import demetra.sa.DecompositionMode;
import demetra.sa.SaDictionary;
import demetra.timeseries.TsData;
import jdplus.modelling.extractors.ArimaExtractor;
import jdplus.modelling.extractors.SarimaExtractor;
import jdplus.regarima.extractors.RegSarimaModelExtractor;
import jdplus.tramoseats.TramoSeatsResults;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@lombok.experimental.UtilityClass
public class TramoSeatsExtractor {

    private final InformationMapping<TramoSeatsResults> MAPPING = new InformationMapping<>(TramoSeatsResults.class);

    private final String DECOMP = "decomposition" + InformationExtractor.SEP, FINAL = "";

    static {
        MAPPING.set(SaDictionary.MODE, DecompositionMode.class, source -> source.getFinals().getMode());
        MAPPING.set(DECOMP + ModellingDictionary.Y_LIN, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Series, ComponentInformation.Value));
        MAPPING.set(DECOMP + ModellingDictionary.Y_LIN + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Series, ComponentInformation.Forecast));
        MAPPING.set(DECOMP + ModellingDictionary.Y_LIN + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Series, ComponentInformation.StdevForecast));
        MAPPING.set(DECOMP + ModellingDictionary.Y_LIN + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Series, ComponentInformation.Backcast));
        MAPPING.set(DECOMP + ModellingDictionary.Y_LIN + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Series, ComponentInformation.StdevBackcast));

        MAPPING.set(DECOMP + SaDictionary.T_LIN, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Trend, ComponentInformation.Value));
        MAPPING.set(DECOMP + SaDictionary.T_LIN + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Trend, ComponentInformation.Forecast));
        MAPPING.set(DECOMP + SaDictionary.T_LIN + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Trend, ComponentInformation.StdevForecast));
        MAPPING.set(DECOMP + SaDictionary.T_LIN + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Trend, ComponentInformation.Backcast));
        MAPPING.set(DECOMP + SaDictionary.T_LIN + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Trend, ComponentInformation.StdevBackcast));

        MAPPING.set(DECOMP + SaDictionary.SA_LIN, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
        MAPPING.set(DECOMP + SaDictionary.SA_LIN + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast));
        MAPPING.set(DECOMP + SaDictionary.SA_LIN + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast));
        MAPPING.set(DECOMP + SaDictionary.SA_LIN + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Backcast));
        MAPPING.set(DECOMP + SaDictionary.SA_LIN + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevBackcast));

        MAPPING.set(DECOMP + SaDictionary.S_LIN, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Value));
        MAPPING.set(DECOMP + SaDictionary.S_LIN + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Forecast));
        MAPPING.set(DECOMP + SaDictionary.S_LIN + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Seasonal, ComponentInformation.StdevForecast));
        MAPPING.set(DECOMP + SaDictionary.S_LIN + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Backcast));
        MAPPING.set(DECOMP + SaDictionary.S_LIN + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Seasonal, ComponentInformation.StdevBackcast));

        MAPPING.set(DECOMP + SaDictionary.I_LIN, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Irregular, ComponentInformation.Value));
        MAPPING.set(DECOMP + SaDictionary.I_LIN + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Irregular, ComponentInformation.Forecast));
        MAPPING.set(DECOMP + SaDictionary.I_LIN + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Irregular, ComponentInformation.StdevForecast));
        MAPPING.set(DECOMP + SaDictionary.I_LIN + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Irregular, ComponentInformation.Backcast));
        MAPPING.set(DECOMP + SaDictionary.I_LIN + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getDecomposition().getInitialComponents().getSeries(ComponentType.Irregular, ComponentInformation.StdevBackcast));

        MAPPING.set(DECOMP + ModellingDictionary.L, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Series, ComponentInformation.Value));
        MAPPING.set(DECOMP + ModellingDictionary.L + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Series, ComponentInformation.Forecast));
        MAPPING.set(DECOMP + ModellingDictionary.L + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Series, ComponentInformation.StdevForecast));
        MAPPING.set(DECOMP + ModellingDictionary.L + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Series, ComponentInformation.Backcast));
        MAPPING.set(DECOMP + ModellingDictionary.L + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Series, ComponentInformation.StdevBackcast));

        MAPPING.set(DECOMP + SaDictionary.T_CMP, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Trend, ComponentInformation.Value));
        MAPPING.set(DECOMP + SaDictionary.T_CMP + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Trend, ComponentInformation.Forecast));
        MAPPING.set(DECOMP + SaDictionary.T_CMP + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Trend, ComponentInformation.StdevForecast));
        MAPPING.set(DECOMP + SaDictionary.T_CMP + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Trend, ComponentInformation.Backcast));
        MAPPING.set(DECOMP + SaDictionary.T_CMP + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Trend, ComponentInformation.StdevBackcast));

        MAPPING.set(DECOMP + SaDictionary.SA_CMP, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
        MAPPING.set(DECOMP + SaDictionary.SA_CMP + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast));
        MAPPING.set(DECOMP + SaDictionary.SA_CMP + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast));
        MAPPING.set(DECOMP + SaDictionary.SA_CMP + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Backcast));
        MAPPING.set(DECOMP + SaDictionary.SA_CMP + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevBackcast));

        MAPPING.set(DECOMP + SaDictionary.S_CMP, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Value));
        MAPPING.set(DECOMP + SaDictionary.S_CMP + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Forecast));
        MAPPING.set(DECOMP + SaDictionary.S_CMP + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Seasonal, ComponentInformation.StdevForecast));
        MAPPING.set(DECOMP + SaDictionary.S_CMP + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Backcast));
        MAPPING.set(DECOMP + SaDictionary.S_CMP + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Seasonal, ComponentInformation.StdevBackcast));

        MAPPING.set(DECOMP + SaDictionary.I_CMP, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Irregular, ComponentInformation.Value));
        MAPPING.set(DECOMP + SaDictionary.I_CMP + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Irregular, ComponentInformation.Forecast));
        MAPPING.set(DECOMP + SaDictionary.I_CMP + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Irregular, ComponentInformation.StdevForecast));
        MAPPING.set(DECOMP + SaDictionary.I_CMP + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Irregular, ComponentInformation.Backcast));
        MAPPING.set(DECOMP + SaDictionary.I_CMP + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getDecomposition().getFinalComponents().getSeries(ComponentType.Irregular, ComponentInformation.StdevBackcast));

        MAPPING.set(FINAL + ModellingDictionary.Y, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.Value));
        MAPPING.set(FINAL + ModellingDictionary.Y + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.Forecast));
        MAPPING.set(FINAL + ModellingDictionary.Y + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.StdevForecast));
        MAPPING.set(FINAL + ModellingDictionary.Y + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.Backcast));
        MAPPING.set(FINAL + ModellingDictionary.Y + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.StdevBackcast));

        MAPPING.set(FINAL + SaDictionary.T, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.Value));
        MAPPING.set(FINAL + SaDictionary.T + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.Forecast));
        MAPPING.set(FINAL + SaDictionary.T + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.StdevForecast));
        MAPPING.set(FINAL + SaDictionary.T + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.Backcast));
        MAPPING.set(FINAL + SaDictionary.T + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.StdevBackcast));

        MAPPING.set(FINAL + SaDictionary.SA, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
        MAPPING.set(FINAL + SaDictionary.SA + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast));
        MAPPING.set(FINAL + SaDictionary.SA + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast));
        MAPPING.set(FINAL + SaDictionary.SA + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Backcast));
        MAPPING.set(FINAL + SaDictionary.SA + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevBackcast));

        MAPPING.set(FINAL + SaDictionary.S, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.Value));
        MAPPING.set(FINAL + SaDictionary.S + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.Forecast));
        MAPPING.set(FINAL + SaDictionary.S + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.StdevForecast));
        MAPPING.set(FINAL + SaDictionary.S + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.Backcast));
        MAPPING.set(FINAL + SaDictionary.S + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.StdevBackcast));

        MAPPING.set(FINAL + SaDictionary.I, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.Value));
        MAPPING.set(FINAL + SaDictionary.I + SeriesInfo.F_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.Forecast));
        MAPPING.set(FINAL + SaDictionary.I + SeriesInfo.EF_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.StdevForecast));
        MAPPING.set(FINAL + SaDictionary.I + SeriesInfo.B_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.Backcast));
        MAPPING.set(FINAL + SaDictionary.I + SeriesInfo.EB_SUFFIX, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.StdevBackcast));

        MAPPING.delegate(DECOMP + "model", ArimaExtractor.getMapping(), source
                -> source.getDecomposition().getUcarimaModel().getModel());
        MAPPING.delegate(DECOMP + "tmodel", ArimaExtractor.getMapping(), source
                -> source.getDecomposition().getUcarimaModel().getComponent(0));
        MAPPING.delegate(DECOMP + "smodel", ArimaExtractor.getMapping(), source
                -> source.getDecomposition().getUcarimaModel().getComponent(1));
        MAPPING.delegate(DECOMP + "samodel", ArimaExtractor.getMapping(), source
                -> source.getDecomposition().getUcarimaModel().getComponent(1));
        MAPPING.delegate(DECOMP + "transitorymodel", ArimaExtractor.getMapping(), source
                -> source.getDecomposition().getUcarimaModel().getComponent(2));
        MAPPING.delegate(DECOMP + "imodel", ArimaExtractor.getMapping(), source
                -> source.getDecomposition().getUcarimaModel().getComponent(3));

        MAPPING.delegate(DECOMP + "initialmodel", SarimaExtractor.getMapping(), source
                -> source.getDecomposition().getOriginalModel());
        MAPPING.delegate(DECOMP + "finalmodel", SarimaExtractor.getMapping(), source
                -> source.getDecomposition().getFinalModel());

        MAPPING.delegate("preprocessing", RegSarimaModelExtractor.getMapping(), source -> source.getPreprocessing());
    }

    public InformationMapping<TramoSeatsResults> getMapping() {
        return MAPPING;
    }
}
