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
package demetra.ssf.dk;

import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import demetra.ssf.UpdateInformation;
import demetra.ssf.State;
import demetra.ssf.StateInfo;

/**
 *
 * @author Jean Palate
 */
public class CompositeDiffuseFilteringResults implements IDiffuseFilteringResults {

    private final IDiffuseFilteringResults[] subresults;

    public CompositeDiffuseFilteringResults(final IDiffuseFilteringResults... subresults) {
        this.subresults = subresults;
    }

    @Override
    public void close(int pos) {
        for (int i = 0; i < subresults.length; ++i) {
            subresults[i].close(pos);
        }
    }

    @Override
    public void clear() {
        for (int i = 0; i < subresults.length; ++i) {
            subresults[i].clear();
        }
    }

    @Override
    public void save(int t, DiffuseUpdateInformation pe) {
        for (int i = 0; i < subresults.length; ++i) {
            subresults[i].save(t, pe);
        }
    }

    @Override
    public void save(int t, DiffuseState state, StateInfo info) {
        for (int i = 0; i < subresults.length; ++i) {
            subresults[i].save(t, state, info);
        }
    }

    @Override
    public void save(int t, UpdateInformation pe) {
        for (int i = 0; i < subresults.length; ++i) {
            subresults[i].save(t, pe);
        }
    }

    @Override
    public void save(final int t, final State state, final StateInfo info) {
        for (int i = 0; i < subresults.length; ++i) {
            subresults[i].save(t, state, info);
        }
    }

    @Override
    public int getEndDiffusePosition() {
        for (int i = 0; i < subresults.length; ++i) {
            int epos = subresults[i].getEndDiffusePosition();
            if (epos >= 0) {
                return epos;
            }
        }
        return -1;

    }

    @Override
    public DataBlock a(int pos) {
        for (int i = 0; i < subresults.length; ++i) {
            DataBlock a = subresults[i].a(pos);
            if (a != null) {
                return a;
            }
        }
        return null;
    }

    @Override
    public Matrix Pi(int pos) {
        for (int i = 0; i < subresults.length; ++i) {
            Matrix P = subresults[i].Pi(pos);
            if (P != null) {
                return P;
            }
        }
        return null;
    }

    @Override
    public DataBlock M(int pos) {
        for (int i = 0; i < subresults.length; ++i) {
            DataBlock c = subresults[i].M(pos);
            if (c != null) {
                return c;
            }
        }
        return null;
    }

    @Override
    public DataBlock Mi(int pos) {
        for (int i = 0; i < subresults.length; ++i) {
            DataBlock c = subresults[i].Mi(pos);
            if (c != null) {
                return c;
            }
        }
        return null;
    }

    @Override
    public Matrix P(int pos) {
        for (int i = 0; i < subresults.length; ++i) {
            Matrix P = subresults[i].P(pos);
            if (P != null) {
                return P;
            }
        }
        return null;
    }

    @Override
    public double error(int pos) {
        for (int i = 0; i < subresults.length; ++i) {
            double d = subresults[i].error(pos);
            if (Double.isFinite(d)) {
                return d;
            }
        }
        return Double.NaN;
    }

    @Override
    public double errorVariance(int pos) {
        for (int i = 0; i < subresults.length; ++i) {
            double d = subresults[i].errorVariance(pos);
            if (Double.isFinite(d)) {
                return d;
            }
        }
        return Double.NaN;
    }

    @Override
    public double diffuseNorm2(int pos) {
        for (int i = 0; i < subresults.length; ++i) {
            double d = subresults[i].diffuseNorm2(pos);
            if (Double.isFinite(d)) {
                return d;
            }
        }
        return Double.NaN;
    }

}
