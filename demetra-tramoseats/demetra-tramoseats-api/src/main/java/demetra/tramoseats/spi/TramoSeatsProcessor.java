/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demetra.tramoseats.spi;

import demetra.design.Algorithm;
import nbbrd.service.ServiceDefinition;
import demetra.modelling.regression.ModellingContext;
import demetra.processing.ProcResults;
import demetra.timeseries.TsData;
import demetra.tramoseats.TramoSeatsSpec;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author palatej
 */
@Algorithm
@ServiceDefinition
public interface TramoSeatsProcessor {

    ProcResults process(@NonNull TsData series, @NonNull TramoSeatsSpec spec, ModellingContext context);
}
