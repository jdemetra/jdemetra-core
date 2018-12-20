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
package demetra.descriptors.stats;

import demetra.design.Development;
import demetra.information.InformationMapping;
import demetra.stats.TestResult;
import demetra.stats.TestResult;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
@Development(status = Development.Status.Beta)
public class TestDescriptor {

    private final String VALUE = "value", PVALUE = "pvalue", DESC = "description";

    private final InformationMapping<TestResult> MAPPING = new InformationMapping<>(TestResult.class);

    static {
        MAPPING.set(VALUE, Double.class, source -> source.getValue());
        MAPPING.set(PVALUE, Double.class, source -> source.getPvalue());
        MAPPING.set(DESC, String.class, source -> source.getDescription());
    }

    public InformationMapping<TestResult> getMapping() {
        return MAPPING;
    }
}
