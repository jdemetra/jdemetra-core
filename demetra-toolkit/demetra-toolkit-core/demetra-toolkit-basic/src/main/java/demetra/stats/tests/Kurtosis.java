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
package demetra.stats.tests;

import demetra.design.BuilderPattern;
import demetra.design.Development;
import jdplus.dstats.Normal;
import demetra.stats.DescriptiveStatistics;
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
