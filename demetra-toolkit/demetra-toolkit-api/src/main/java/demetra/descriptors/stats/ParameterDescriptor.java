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
package demetra.descriptors.stats;

import demetra.design.Development;
import demetra.information.InformationMapping;
import demetra.stats.ParameterEstimation;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
@Development(status = Development.Status.Beta)
public class ParameterDescriptor {
    
    private final String VALUE = "value", STDE = "stde", DESC="description";

    private final InformationMapping<ParameterEstimation> MAPPING = new InformationMapping<>(ParameterEstimation.class);

    static {
        MAPPING.set(VALUE, Double.class, source -> source.getValue());
        MAPPING.set(STDE, Double.class, source -> source.getStandardError());
        MAPPING.set(DESC, String.class, source -> source.getDescription());
    }
    
}
