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
package ec.tstoolkit.jdr.sa;

import demetra.information.InformationMapping;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.ISeriesDecomposition;
import ec.tstoolkit.modelling.ComponentInformation;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.modelling.SeriesInfo;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class SaDecompositionInfo {

   
    final InformationMapping<ISeriesDecomposition> MAPPING = new InformationMapping<>(ISeriesDecomposition.class);
    
    public InformationMapping<ISeriesDecomposition> getMapping() {
        return MAPPING;
    }

    static {
        MAPPING.set(ModellingDictionary.Y, TsData.class, source -> source.getSeries(ComponentType.Series, ComponentInformation.Value));
        MAPPING.set(ModellingDictionary.Y + SeriesInfo.F_SUFFIX, TsData.class, source -> source.getSeries(ComponentType.Series, ComponentInformation.Forecast));
        MAPPING.set(ModellingDictionary.T, TsData.class, source -> source.getSeries(ComponentType.Trend, ComponentInformation.Value));
        MAPPING.set(ModellingDictionary.T + SeriesInfo.F_SUFFIX, TsData.class, source -> source.getSeries(ComponentType.Trend, ComponentInformation.Forecast));
        MAPPING.set(ModellingDictionary.SA, TsData.class, source -> source.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
        MAPPING.set(ModellingDictionary.SA + SeriesInfo.F_SUFFIX, TsData.class, source -> source.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast));
        MAPPING.set(ModellingDictionary.S, TsData.class, source -> source.getSeries(ComponentType.Seasonal, ComponentInformation.Value));
        MAPPING.set(ModellingDictionary.S + SeriesInfo.F_SUFFIX, TsData.class, source -> source.getSeries(ComponentType.Seasonal, ComponentInformation.Forecast));
        MAPPING.set(ModellingDictionary.I, TsData.class, source -> source.getSeries(ComponentType.Irregular, ComponentInformation.Value));
        MAPPING.set(ModellingDictionary.I + SeriesInfo.F_SUFFIX, TsData.class, source -> source.getSeries(ComponentType.Irregular, ComponentInformation.Forecast));
        MAPPING.set(ModellingDictionary.MODE, DecompositionMode.class, source -> source.getMode());
    }

}
