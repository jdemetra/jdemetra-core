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
package ec.tstoolkit.maths.realfunctions;

import ec.tstoolkit.data.IDataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SingleParameter implements IDataBlock
{

    private double value;

    /**
     * 
     * @param d
     */
    public SingleParameter(final double d)
    {
	value = d;
    }

    /**
     * 
     * @param buffer
     * @param start
     */
    public void copyFrom(double[] buffer, int start)
    {
	value = buffer[start];
    }

    public void copyTo(double[] buffer, int start) {
	buffer[start] = value;
    }

    public IDataBlock extract(int start, int length) {
	if (start == 0 && length == 1)
	    return this;
	else
	    return null;
    }

    public double get(final int idx) {
	return value;
    }

    @Override
    public int getLength() {
	return 1;
    }

    @Override
    public IReadDataBlock rextract(int start, int length) {
	if (start == 0 && length == 1)
	    return this;
	else
	    return null;
    }

    @Override
    public void set(final int idx, final double value) {
	this.value = value;
    }
    
    @Override
    public String toString(){
        return Double.toString(value);
    }
}
