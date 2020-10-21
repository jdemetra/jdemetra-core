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
package jdplus.stats.tests;

import nbbrd.design.BuilderPattern;
import nbbrd.design.Development;
import jdplus.dstats.Normal;
import jdplus.stats.DescriptiveStatistics;
import demetra.data.DoubleSeq;


/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@BuilderPattern(StatisticalTest.class)
public class Kurtosis {

    private final DescriptiveStatistics stats;
    
    public Kurtosis(DoubleSeq data)
    {
        this.stats=DescriptiveStatistics.of(data);
    }

    public Kurtosis(DescriptiveStatistics stats)
    {
        this.stats=stats;
    }

    public StatisticalTest build() {
	int n = stats.getObservationsCount();
	Normal dist = new Normal(3, Math.sqrt(24.0 / n));
        return new StatisticalTest(dist, stats.getKurtosis(), TestType.TwoSided, true);
    }
}
