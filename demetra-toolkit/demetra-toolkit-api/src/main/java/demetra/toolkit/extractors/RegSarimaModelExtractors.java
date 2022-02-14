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
package demetra.toolkit.extractors;

import demetra.information.InformationDelegate;
import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import demetra.arima.SarimaSpec;
import demetra.timeseries.regression.modelling.GeneralLinearModel;
import demetra.timeseries.regression.modelling.LightweightRegSarimaModel;
import nbbrd.design.Development;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@lombok.experimental.UtilityClass
public class RegSarimaModelExtractors {

    /**
     * Generic extractor. See GeneralLinearModel for more details
     */
    @ServiceProvider(InformationExtractor.class)
    public static class Generic extends InformationDelegate<LightweightRegSarimaModel, GeneralLinearModel> {

        public Generic() {
            super(v -> v);
        }

        @Override
        public Class<GeneralLinearModel> getDelegateClass() {
            return GeneralLinearModel.class;
        }

        @Override
        public Class<LightweightRegSarimaModel> getSourceClass() {
            return LightweightRegSarimaModel.class;
        }

    }

    /**
     * Specific stochastic model
     */
    @ServiceProvider(InformationExtractor.class)
    public static class Specific extends InformationMapping<LightweightRegSarimaModel> {

        public final String SARIMA = "arima";

        public Specific() {
            delegate(SARIMA, SarimaSpec.class, source
                    -> source.getDescription().getStochasticComponent());
        }

        @Override
        public Class<LightweightRegSarimaModel> getSourceClass() {
            return LightweightRegSarimaModel.class;
        }

    }

}
