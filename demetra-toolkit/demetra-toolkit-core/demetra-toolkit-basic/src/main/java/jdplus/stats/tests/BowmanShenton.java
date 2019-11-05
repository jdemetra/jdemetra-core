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

import demetra.design.BuilderPattern;
import demetra.design.Development;
import jdplus.dstats.Chi2;
import jdplus.stats.DescriptiveStatistics;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@BuilderPattern(StatisticalTest.class)
public class BowmanShenton 
{

    private final DescriptiveStatistics stats;

    /**
     * 
     */
    public BowmanShenton(DoubleSeq data)
    {
        stats=DescriptiveStatistics.of(data);
    }

    public StatisticalTest build() {
	int n = stats.getObservationsCount();
	double m3 = stats.getSkewness();
	double m4 = stats.getKurtosis() - 3.0;
	double val = n / 6.0 * m3 * m3 + n / 24.0 * m4 * m4;
	Chi2 chi = new Chi2(2);
        return new StatisticalTest(chi, val, TestType.Upper, true);
    }

}
