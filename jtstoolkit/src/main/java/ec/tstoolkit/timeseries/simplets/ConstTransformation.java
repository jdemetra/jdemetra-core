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
import ec.tstoolkit.design.Development;
import ec.tstoolkit.OperationType;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public final class ConstTransformation implements ITsDataTransformation {

    /**
     * 
     * @param c
     * @return
     */
    public static ConstTransformation difference(double c)
    {
	return new ConstTransformation(OperationType.Diff, c);
    }

    /**
     * 
     * @param c
     * @return
     */
    public static ConstTransformation product(double c)
    {
	return new ConstTransformation(OperationType.Product, c);
    }

    /**
     * 
     * @param c
     * @return
     */
    public static ConstTransformation ratio(double c)
    {
	return new ConstTransformation(OperationType.Ratio, c);
    }

    /**
     * 
     * @param c
     * @return
     */
    public static ConstTransformation sum(double c)
    {
	return new ConstTransformation(OperationType.Sum, c);
    }

    /**
     * 
     * @param y
     * @return
     */
    public static ConstTransformation unit(double u)
    {
	return new ConstTransformation(OperationType.Product, u);
    }

    /**
     *
     */
    public final OperationType op;

    /**
     *
     */
    public final double value;

    private ConstTransformation(OperationType type, double val) {
	this.op = type;
	this.value = val;
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
	return new ConstTransformation(mode, value);
    }

    /**
     * 
     * @param data
     * @param ljacobian
     * @return
     */
    public boolean transform(TsData data, LogJacobian ljacobian)
    {
	DataBlock val = new DataBlock(data.internalStorage());
	switch (op) {
	case Diff:
	    val.sub(value);
	    break;
	case Product:
	    val.mul(value);
	    if (ljacobian != null)
		ljacobian.value += (ljacobian.end - ljacobian.start)
			* Math.log(value);
	    break;
	case Sum:
	    val.add(value);
	    break;
	case Ratio:
	    if (value == 0)
		return false;
	    else {
		val.div(value);
		if (ljacobian != null)
		    ljacobian.value -= (ljacobian.end - ljacobian.start)
			    * Math.log(value);
	    }
	    break;
	default:
	    return false;
	}
	return true;
    }
}
