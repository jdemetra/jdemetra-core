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

package ec.tstoolkit.timeseries.simplets;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;import ec.tstoolkit.timeseries.simplets.TsData;


/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class ExpTransformation implements ITsDataTransformation
{

    /**
     * 
     * @return
     */
    @Override
    public ITsDataTransformation converse()
    {
	return new LogTransformation();
    }

    /**
     * 
     * @param data
     * @param ljacobian
     * @return
     */
    @Override
    public boolean transform(TsData data, LogJacobian ljacobian)
    {
	data.apply(x->Math.exp(x));
	if (ljacobian != null) {
	    DataBlock rc = new DataBlock(data.internalStorage())
		    .range(ljacobian.start, ljacobian.end);
	    ljacobian.value += rc.sum();
	}
	return true;
    }
}
