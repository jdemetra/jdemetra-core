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


package ec.satoolkit.x11;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;

/**
 * Complete a smoothing algorithms by copying the smoothed values (at the
 * beginning and at the end of the series) with a delay of 1 year.
 * We consider that the output buffer (o[0, n[) is already completed for the range
 * [np, n-np[. This end points processor will put the value o[np] in the range o[0, np[
 * and the value o[n-np-1] in the range o[n-np, n[.
 * np should be strictly smaller than (n-freq)/2 (not checked).
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Release)
public final class CopyYearEndPoints implements IEndPointsProcessor {
    private int npoints, frequency;

    /**
     * @param npoints The number of points that will be copied at the beginning
     * an at the end of the series
     * @param freq The annual frequency of the series. The extended values will
     * be such that x[p-freq] = x[p] (beginning) or x[p+freq] = x[p] (end).
     */
    public CopyYearEndPoints(int npoints, int freq)
    {
	this.npoints = npoints;
	this.frequency = freq;
    }

    @Override
    public void process(DataBlock in, DataBlock out) {
	for (int i = npoints - 1, j = out.getLength() - npoints; i >= 0; --i, ++j) {
	    out.set(i, out.get(i + frequency));
	    out.set(j, out.get(j - frequency));
	}
    }

}
