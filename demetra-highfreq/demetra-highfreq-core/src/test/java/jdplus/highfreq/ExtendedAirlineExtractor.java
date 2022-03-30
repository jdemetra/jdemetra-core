/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.highfreq;

import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import demetra.modelling.ComponentInformation;
import demetra.modelling.ModellingDictionary;
import demetra.modelling.SeriesInfo;
import demetra.sa.ComponentType;
import demetra.sa.DecompositionMode;
import demetra.sa.SaDictionaries;
import demetra.timeseries.TsData;
import jdplus.modelling.GeneralLinearModel;
import jdplus.regsarima.regular.RegSarimaModel;
import nbbrd.design.Development;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class ExtendedAirlineExtractor extends InformationMapping<ExtendedAirlineResults> {

    public static final String FINAL = "";

    public ExtendedAirlineExtractor() {
        set(SaDictionaries.MODE, DecompositionMode.class, source -> source.getFinals().getMode());

        set(ModellingDictionary.Y, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Series, ComponentInformation.Value));
        set(SaDictionaries.T, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Trend, ComponentInformation.Value));
        set(SaDictionaries.SA, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
        set(SaDictionaries.S, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.Value));
        set(SaDictionaries.I, TsData.class, source
                -> source.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.Value));

        delegate(null, GeneralLinearModel.class, source -> source.getPreprocessing());
    }

    @Override
    public Class<ExtendedAirlineResults> getSourceClass() {
        return ExtendedAirlineResults.class;
    }
}
