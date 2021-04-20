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
package demetra.processing;

import nbbrd.design.Development;
import java.util.List;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.Value
@lombok.Builder
public class ProcDiagnostic {

    double value;
    @lombok.NonNull
    ProcQuality quality;
    @lombok.NonNull
    String diagnostic;
    @lombok.NonNull
    String category;
    @lombok.Singular
    List<String> warnings;
    Object details;

    public static ProcQuality summary(@lombok.NonNull List<ProcDiagnostic> diagnostics) {
        ProcQuality rslt = ProcQuality.Undefined;
        int sum = 0, n = 0;

        if (diagnostics.isEmpty()) {
            return rslt;
        }

        for (ProcDiagnostic diag : diagnostics) {
            ProcQuality quality = diag.quality;
            switch (quality) {
                case Error:
                    return ProcQuality.Error;
                case Severe:
                    return ProcQuality.Severe;
                case Good:
                    ++n;
                    sum += 3;
                    break;
                case Uncertain:
                    ++n;
                    sum += 2;
                    break;
                case Bad:
                    ++n;
                    break;
            }
        }
        if (n > 0) {
            double val = sum;
            val /= n;
            if (val >= 2.5) {
                rslt = ProcQuality.Good;
            } else if (val >= 1.5) {
                rslt = ProcQuality.Uncertain;
            } else {
                rslt = ProcQuality.Bad;
            }
        }
        return rslt;
    }
}
