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

package ec.tstoolkit.data;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class DescriptiveStatisticsTest {
    
    @Test
    public void testIsFinite() {
        Assert.assertTrue(DescriptiveStatistics.isFinite(0d));
        Assert.assertTrue(DescriptiveStatistics.isFinite(123.123));
        Assert.assertTrue(DescriptiveStatistics.isFinite(-123.123));
        Assert.assertFalse(DescriptiveStatistics.isFinite(Double.NEGATIVE_INFINITY));
        Assert.assertFalse(DescriptiveStatistics.isFinite(Double.POSITIVE_INFINITY));
        Assert.assertFalse(DescriptiveStatistics.isFinite(Double.NaN));
    }
}
