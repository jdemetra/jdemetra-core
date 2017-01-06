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

import ec.tstoolkit.OperationType;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class DataTransformation implements ITsDataTransformation {

    /**
     *
     */
    public final OperationType op;

    private TsData m_tsdata;

    /**
     * 
     * @param tsdata
     * @param mode
     */
    public DataTransformation(TsData tsdata, OperationType mode)
    {
	m_tsdata = tsdata.clone();
	op = mode;
    }

    /**
     * 
     * @return
     */
    public ITsDataTransformation converse()
    {
	OperationType mode = OperationType.None;
	switch (op) {
	case Diff:
	    mode = OperationType.Sum;
	    break;
	case Sum:
	    mode = OperationType.Diff;
	    break;
	case Ratio:
	    mode = OperationType.Product;
	    break;
	case Product:
	    mode = OperationType.Ratio;
	    break;
	}
	return new DataTransformation(m_tsdata, mode);
    }

    /**
     * 
     * @return
     */
    public TsData getTSData()
    {
	return m_tsdata;
    }

    /**
     * 
     * @param data
     * @param ljacobian
     * @return
     */
    public boolean transform(TsData data, LogJacobian ljacobian)
    {
	TsDomain domain = m_tsdata.getDomain();
	TsDomain common = data.getDomain().intersection(domain);
	int istart = common.getStart().minus(data.getStart());
	int jstart = common.getStart().minus(domain.getStart());
	int iend = istart + common.getLength();
	int jend = jstart + common.getLength();
	DataBlock s = new DataBlock(m_tsdata.internalStorage(), istart, iend, 1);
        IReadDataBlock extract = m_tsdata.rextract(jstart, common.getLength());
	switch (op) {
	case Diff:
	    s.apply(extract, (x,y)->x-y);
	    break;
	case Product:
	    s.apply(extract, (x,y)->x*y);
	    break;
	case Sum:
	    s.apply(extract, (x,y)->x+y);
	    break;
	case Ratio:
	    s.apply(extract, (x,y)->x/y);
	    break;

	default:
	    return false;
	}
        // TODO Should be verified !!!
	if (ljacobian != null
		&& (op == OperationType.Product || op == OperationType.Ratio)) {
	    if (ljacobian.start > istart) {
		jstart += ljacobian.start - istart;
		istart = ljacobian.start;
	    }
	    if (ljacobian.end < iend)
		jend -= iend - ljacobian.end;
	    double l = 0;
	    for (int j = jstart; j < jend; ++j)
		l += Math.log(s.get(j));
	    if (op == OperationType.Product)
		ljacobian.value += l;
	    else
		ljacobian.value -= l;
	}

	return true;
    }
}
