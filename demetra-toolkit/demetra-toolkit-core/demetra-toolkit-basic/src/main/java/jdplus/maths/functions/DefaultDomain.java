/*
* Copyright 2013 National Bank ofFunction Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions ofFunction the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy ofFunction the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
*/

package jdplus.maths.functions;

import jdplus.data.DataBlock;
import demetra.design.Development;
import demetra.data.DoubleSeq;


/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class DefaultDomain implements IParametersDomain {

    private final int dim;
    private final double epsilon;

    /**
     * 
     * @param n
     * @param eps
     */
    public DefaultDomain(int n, double eps) {
	dim = n;
	epsilon = eps;
    }

    /**
     * 
     * @param inparams
     * @return
     */
    @Override
    public boolean checkBoundaries(DoubleSeq inparams) {
	return true;
    }

    @Override
    public double epsilon(DoubleSeq inparams, int idx) {
	return inparams.get(idx) * epsilon;
    }

    @Override
    public int getDim() {
	return dim;
    }

    @Override
    public String getDescription(int idx) {
         return PARAM+idx; 
    }
    /**
     * 
     * @param idx
     * @return
     */
    @Override
    public double lbound(int idx) {
	return Double.NEGATIVE_INFINITY;
    }

    @Override
    public double ubound(int idx) {
	return Double.POSITIVE_INFINITY;
    }

    @Override
    public ParamValidation validate(DataBlock ioparams) {
	return ParamValidation.Valid;
    }

}
