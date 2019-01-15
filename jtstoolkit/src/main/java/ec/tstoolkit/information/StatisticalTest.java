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

package ec.tstoolkit.information;

import ec.tstoolkit.dstats.IDistribution;
import java.util.Formatter;

/**
 * 
 * @author Jean Palate
 */
public class StatisticalTest {
    /**
     *
     */
    public final String description;
    /**
     *
     */
    public final double value;
    /**
     *
     */
    public final double pvalue;

    public static StatisticalTest of(ec.tstoolkit.stats.StatisticalTest test){
        if (test == null)
            return null;
        else
            return new StatisticalTest(test);
    }

    @Deprecated
    public static StatisticalTest create(ec.tstoolkit.stats.StatisticalTest test){
        if (test == null)
            return null;
        else
            return new StatisticalTest(test);
    }
    /**
     * 
     * @param test
     */
    private StatisticalTest(ec.tstoolkit.stats.StatisticalTest test)
    {
	IDistribution d = test.getDistribution();
	if (d != null) {
	    description = d.getDescription();
	    pvalue = test.getPValue();
	} else {
	    description = null;
	    pvalue = Double.NaN;
	}
	value = test.getValue();
    }

    /**
     * 
     * @param desc
     * @param val
     * @param pval
     */
    public StatisticalTest(String desc, double val, double pval)
    {
	description = desc;
	value = val;
	pvalue = pval;
    }

    @Override
    public String toString() {
	return new Formatter().format("%g4", value).toString();
    }
}
