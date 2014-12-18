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

package ec.benchmarking.ssf.multivariate;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.ssf.multivariate.IMSsfData;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class MultivariateSsfData implements IMSsfData, Cloneable {

    private int c_, ncnt_;
    private Matrix x_;

    /**
     * 
     * @param m
     * @param conv
     * @param nconstraints
     */
    public MultivariateSsfData(Matrix m, int conv, int nconstraints) {
        x_ = m;
        c_ = conv;
        ncnt_ = nconstraints;
    }

    /**
     * 
     * @param data
     */
    public MultivariateSsfData(MultivariateSsfData data) {
        c_ = data.c_;
        ncnt_ = data.ncnt_;
        x_ = data.x_;
    }

    @Override
    public Object clone() {
        return new MultivariateSsfData(this);
    }

    /**
     * 
     * @param v
     * @return
     */
    public int count(int v) {
        return c_ * x_.getRowsCount();
    }

    /**
     * 
     * @param v
     * @param pos
     * @return
     */
    public double get(int v, int pos) {
        if (v < x_.getColumnsCount()) {
            if ((pos + 1) % c_ != 0) {
                return Double.NaN;
            } else {
                return x_.get(pos / c_, v);
            }
        } else {
            return 0;
        }
    }

    /**
     * 
     * @return
     */
    public double[] getInitialState() {
        return null;
    }

    /**
     * 
     * @return
     */
    public int getVarsCount() {
        return x_.getColumnsCount() + ncnt_;
    }

    /**
     * 
     * @return
     */
    public boolean hasData() {
        return true;
    }

    /**
     * 
     * @param v
     * @param pos
     * @return
     */
    public boolean isMissing(int v, int pos) {
        if (v < x_.getColumnsCount()) {
            pos /= c_;
            if (pos >= x_.getRowsCount() || Double.isNaN(x_.get(pos, v))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 
     * @param v
     * @return
     */
    public int obsCount(int v) {
        if (v < x_.getColumnsCount()) {
            int n = 0;
            DataBlock col = x_.column(v);
            for (int i = 0; i < col.getLength(); ++i) {
                if (!Double.isNaN(col.get(i))) {
                    ++n;
                }
            }
            return n;
        } else {
            return count(v);
        }

    }
}
