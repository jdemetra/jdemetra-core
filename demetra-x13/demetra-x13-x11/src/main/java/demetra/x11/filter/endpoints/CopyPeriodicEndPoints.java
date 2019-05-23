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
package demetra.x11.filter.endpoints;

import jdplus.data.DataBlock;
import demetra.design.Development;
import demetra.data.DoubleSeq;

/**
 * Complete a smoothing algorithms by copying the smoothed values (at the
 * beginning and at the end of the series) with a delay of n periods.
 * We consider that the output buffer (o[0, n[) is already completed for the range
 * [np, n-np[. This end points processor will put the value o[np] in the range o[0, np[
 * and the value o[n-np-1] in the range o[n-np, n[.
 * np should be strictly smaller than (n-freq)/2 (not checked).
 *
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Release)
public final class CopyPeriodicEndPoints implements IEndPointsProcessor {

    private final int npoints, period;

    /**
     * @param npoints The number of points that will be copied at the beginning
     *                an at the end of the series
     * @param period  The period of the series. The extended values will
     *                be such that x[p-period] = x[p] (beginning) or x[p+freq] = x[p] (end).
     */
    public CopyPeriodicEndPoints(int npoints, int period) {
        this.npoints = npoints;
        this.period = period;
    }

    @Override
    public void process(DoubleSeq in, DataBlock out) {
        for (int i = npoints - 1, j = out.length() - npoints; i >= 0; --i, ++j) {
            out.set(i, out.get(i + period));
            out.set(j, out.get(j - period));
        }
    }

}
