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
package jdplus.sa.diagnostics;

import demetra.data.DoubleSeq;
import demetra.stats.StatisticalTest;
import jdplus.stats.DescriptiveStatistics;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class SaDiagnostics {

    private final double E_LIMIT = .005;

    public boolean isSignificant(DoubleSeq s, DoubleSeq ref, double limit) {
        if (s.isEmpty()) {
            return false;
        }
        DescriptiveStatistics sdesc = DescriptiveStatistics.of(s);
        DescriptiveStatistics refdesc = DescriptiveStatistics.of(ref);
        double se = sdesc.getStdev();
        double refe = refdesc.getRmse();
        return refe == 0 || se / refe > limit;
    }

    public boolean isSignificant(DoubleSeq s, DoubleSeq ref) {
        return isSignificant(s, ref, E_LIMIT);
    }

    public boolean isSignificant(DoubleSeq s, double limit) {
        if (s == null) {
            return false;
        }
        DescriptiveStatistics sdesc = DescriptiveStatistics.of(s);
        double se = sdesc.getStdev();
        return se > limit;
    }

    public boolean isSignificant(DoubleSeq s) {
        return isSignificant(s, E_LIMIT);
    }
    
    

}
