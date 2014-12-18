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
package ec.tstoolkit.ssf.multivariate;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.ssf.SsfData;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class M2uData extends SsfData
{

    static M2uData create(final IMSsfData data) {
	int nvars = data.getVarsCount();
	int n = data.count(0);
	for (int j = 1; j < nvars; ++j)
	    if (data.count(j) != n)
		return null;
	Matrix m = new Matrix(nvars, n);
	for (int i = 0; i < n; ++i)
	    for (int j = 0; j < nvars; ++j)
		m.set(i, j, data.get(i, j));
	return new M2uData(m, data.getInitialState());
    }

    /**
     * 
     * @param data
     * @param a0
     */
    public M2uData(final Matrix data, final double[] a0)
    {
	super(data.internalStorage(), a0);
    }
    
    public M2uData(final double[][] data, final double[] a0)
    {
	super(merge(data), a0);
    }

    private static double[] merge(final double[][] data){
        Matrix m=new Matrix(data.length, data[0].length);
        for (int i=0; i<data.length; ++i){
            m.row(i).copyFrom(data[i], 0);
        }
        return m.internalStorage();
    }
    
}
