/*
 * Copyright 2013-2014 National Bank of Belgium
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
package demetra.ssf.univariate;

import demetra.ssf.DataResults;
import demetra.ssf.StateInfo;
import demetra.ssf.StateStorage;
import demetra.data.DoubleSequence;

/**
 *
 * @author Jean Palate
 */
public class DefaultSmoothingResults extends StateStorage implements ISmoothingResults {

    private final DataResults e, f;

    protected DefaultSmoothingResults(final boolean cov, final boolean err) {
        super(StateInfo.Smoothed, cov);
        if (err) {
            e = new DataResults();
            f = new DataResults();
        } else {
            e = null;
            f = null;
        }
    }

    public static DefaultSmoothingResults full() {
        return new DefaultSmoothingResults(true, true);
    }

    public static DefaultSmoothingResults light() {
        return new DefaultSmoothingResults(false, false);
    }

    public void saveSmoothedError(int t, double err, double v) {
        if (e == null) {
            return;
        }
        e.save(t, err);
        f.save(t, v);
    }

    public DoubleSequence errors() {
        return e == null ? null : e.asDoublesReader(true);
    }

    public DoubleSequence errorVariances() {
        return f == null ? null : f.asDoublesReader(true);
    }

    @Override
    public void prepare(ISsf ssf, int start, int end) {
        super.prepare(ssf, start, end);
        if (e != null && ssf.getMeasurement().hasErrors()) {
            e.prepare(start, end);
            f.prepare(start, end);
        } 
        
    }

    @Override
    public void rescaleVariances(double factor){
        super.rescaleVariances(factor);
        if (f != null)
            f.rescale(factor);
    }

}
