/*
 * Copyright 2016 National Bank of Belgium
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
package jdplus.ssf.dk;

import jdplus.data.DataBlock;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.univariate.DefaultFilteringResults;
import jdplus.ssf.DataBlockResults;
import jdplus.ssf.DataResults;
import jdplus.ssf.StateInfo;
import jdplus.likelihood.DeterminantalTerm;
import jdplus.ssf.ISsfInitialization;
import jdplus.ssf.State;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
public class BaseDiffuseFilteringResults extends DefaultFilteringResults implements IDiffuseFilteringResults {

    private final DataBlockResults Ci;
    private final DataResults fi;
    private int enddiffuse;

    protected BaseDiffuseFilteringResults(boolean var) {
        super(var);
        Ci = new DataBlockResults();
        fi=new DataResults();
    }

    @Override
    public void prepare(ISsf ssf, final int start, final int end) {
        super.prepare(ssf, start, end);
        ISsfInitialization initialization = ssf.initialization();
        int dim = initialization.getStateDim(), n = initialization.getDiffuseDim();
        fi.prepare(start, n);
        Ci.prepare(dim, start, n);
    }

    @Override
    public void save(int t, DiffuseUpdateInformation pe) {
        super.save(t, pe);
        fi.save(t, pe.getDiffuseVariance());
        Ci.save(t, pe.Mi());
    }

    @Override
    public void close(int pos) {
        enddiffuse = pos;
    }

    @Override
    public void save(final int t, final DiffuseState state, final StateInfo info) {
        if (info != StateInfo.Forecast) {
            return;
        }
        super.save(t, state, info);
    }

    @Override
    public DoubleSeq errors(boolean normalized, boolean clean) {
        DataBlock r = DataBlock.of(errors());
        // set diffuse elements to Double.NaN
        r.range(0, enddiffuse).apply(fi.extract(0, enddiffuse), (x,y)->y!=0 ? Double.NaN : x);
        if (normalized) {
            DoubleSeq allf = errorVariances();
            r.apply(allf, (x, y) -> Double.isFinite(x) && Double.isFinite(y) ? x / Math.sqrt(y) : Double.NaN);
        }
        if (clean){
            r=DataBlock.select(r, (x)->Double.isFinite(x));
        }
        return r;
    }

    public double diffuseNorm2(int pos) {
        return fi.get(pos);
    }

    public DataBlock Mi(int pos) {
        return Ci.datablock(pos);
    }
 
    @Override
    public void clear() {
        super.clear();
        Ci.clear();
        fi.clear();
        enddiffuse = 0;
    }

    @Override
    public int getEndDiffusePosition() {
        return enddiffuse;
    }
    
   @Override
    public double var() {
        int m = 0;
        double ssq = 0;
        int nd = getEndDiffusePosition();
        for (int i = 0; i < nd; ++i) {
            double e = error(i);
            if (Double.isFinite(e) && diffuseNorm2(i) == 0) {
                ++m;
                ssq += e * e / errorVariance(i);
            }
        }
        int n=size();
        for (int i = nd; i < n; ++i) {
            double e = error(i);
            if (Double.isFinite(e)) {
                ++m;
                ssq += e * e / errorVariance(i);
            }
        }
        return ssq / m;
    }

    @Override
    public double logDeterminant() {
        DeterminantalTerm det = new DeterminantalTerm();
        for (int i = 0; i < getEndDiffusePosition(); ++i) {
            if (Double.isFinite(error(i))) {
                double d = diffuseNorm2(i);
                if (d == 0) {
                    double e = errorVariance(i);
                    if (e > State.ZERO) {
                        det.add(e);
                    }
                }
            }
        }
        int n=size();
        for (int i = getEndDiffusePosition(); i < n; ++i) {
            if (Double.isFinite(error(i))) {
                double e = errorVariance(i);
                if (e > State.ZERO) {
                    det.add(e);
                }
            }
        }
        return det.getLogDeterminant();

    }
    
}
