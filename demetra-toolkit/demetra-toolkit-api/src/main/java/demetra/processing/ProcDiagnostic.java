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

import demetra.design.Development;
import demetra.information.Information;
import demetra.information.InformationSet;
import java.util.List;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
public class ProcDiagnostic {

    public ProcDiagnostic(double value, ProcQuality quality) {
        this.value = value;
        this.quality = quality;
    }
    public final double value;
    public final ProcQuality quality;
    public static final String QUALITY = "quality";

    public static ProcQuality summary(InformationSet summary) {
        ProcQuality rslt = ProcQuality.Undefined;
        int sum = 0, n = 0;
        ProcQuality q = summary.get(QUALITY, ProcQuality.class);
        if (q != null) {
            return q;
        }
        List<Information<InformationSet>> subsets = summary.select(InformationSet.class);

        for (Information<InformationSet> subset : subsets) {
            List<Information<ProcDiagnostic>> infos = subset.value.select(ProcDiagnostic.class);
            for (Information<ProcDiagnostic> info : infos) {
                ProcQuality quality = info.value.quality;
                switch (quality) {
                    case Error:
                        summary.set(QUALITY, ProcQuality.Error);
                        return ProcQuality.Error;
                    case Severe:
                        summary.set(QUALITY, ProcQuality.Severe);
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
        summary.set(QUALITY, rslt);
        return rslt;
    }
}
