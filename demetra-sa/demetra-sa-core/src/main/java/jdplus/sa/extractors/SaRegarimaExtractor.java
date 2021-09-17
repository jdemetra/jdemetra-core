/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.sa.extractors;

import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import demetra.modelling.ModellingDictionary;
import demetra.modelling.SeriesInfo;
import demetra.sa.ComponentType;
import demetra.sa.SaDictionary;
import demetra.sa.SaVariable;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.regression.IOutlier;
import static jdplus.regarima.extractors.RegSarimaModelExtractors.NBCAST;
import static jdplus.regarima.extractors.RegSarimaModelExtractors.NFCAST;
import jdplus.regsarima.regular.RegSarimaModel;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(InformationExtractor.class)
public class SaRegarimaExtractor extends InformationMapping<RegSarimaModel> {

    @Override
    public Class getSourceClass() {
        return RegSarimaModel.class;
    }

    private static TsData outlier(RegSarimaModel source, ComponentType type, TsDomain domain) {
        return source.getDeterministicEffect(domain, v -> v.getCore() instanceof IOutlier && v.isAttribute(SaVariable.REGEFFECT, type.name()));
    }

    private static TsData det(RegSarimaModel source, ComponentType type, TsDomain domain) {
        return source.getDeterministicEffect(domain, v -> v.isAttribute(SaVariable.REGEFFECT, type.name()));
    }

    public SaRegarimaExtractor() {
        set(SaDictionary.OUT_I, TsData.class, source -> outlier(source, ComponentType.Irregular, null));
        setArray(SaDictionary.OUT_I + SeriesInfo.F_SUFFIX, NFCAST, TsData.class,
                (source, i) -> outlier(source, ComponentType.Irregular, source.forecastDomain(i)));
        setArray(SaDictionary.OUT_I + SeriesInfo.B_SUFFIX, NBCAST, TsData.class,
                (source, i) -> outlier(source, ComponentType.Irregular, source.backcastDomain(i)));
    }
}
