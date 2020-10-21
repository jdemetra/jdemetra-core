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


package jdplus.x11plus;

import jdplus.data.DataBlock;
import nbbrd.design.Development;
import demetra.data.DoubleSeq;


/**
 * Complete a smoothing algorithms by copying the last smoothed values (at the
 * beginning and at the end of the series).
 * We consider that the output buffer (o[0, n[) is already completed for the range
 * [np, n-np[. This end points processor will put the value o[np] in the range o[0, np[
 * and the value o[n-np-1] in the range o[n-np, n[.
 * np should be strictly smaller than n/2 (not checked).
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Release)
public final class CopyEndPoints implements IEndPointsProcessor {

    private int npoints;

    /**
     * Creates a new end points processor
     * @param npoints The number of points that will be copied at the beginning
     * an at the end of the series
     */
    public CopyEndPoints(int npoints)
    {
	this.npoints = npoints;
    }

    @Override
    public void process(final DoubleSeq in, final DataBlock out) {
	out.range(0, npoints).set(out.get(npoints));
	int n = out.length();
	out.range(n - npoints, n).set(out.get(n - npoints - 1));
    }

}
