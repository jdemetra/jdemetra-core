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
package demetra.sa;

import demetra.information.Explorable;
import demetra.processing.ProcDiagnostic;
import demetra.processing.ProcQuality;
import demetra.timeseries.Ts;
import java.util.List;

/**
 *
 * @author PALATEJ
 */
@lombok.Getter
@lombok.AllArgsConstructor
public class SaDocument {
    private final String name;
    private final Ts series;
    private final SaSpecification specification;
    private final Explorable results;
    private final List<ProcDiagnostic>  diagnostics;
    private final ProcQuality quality;
}
