/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
*/

package ec.tstoolkit.algorithm;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.Information;
import ec.tstoolkit.information.InformationSet;
import java.util.List;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
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
