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
package jdplus.ssf;

import jdplus.data.DataBlockIterator;
import jdplus.data.DataBlockStorage;
import jdplus.math.matrices.Matrix;

/**
 *
 * @author Jean Palate
 */
public class DataBlocksResults {

    private DataBlockStorage data;
    private int[] cdims;
    private int start;

    /**
     *
     */
    public DataBlocksResults() {
    }

    /**
     *
     */
    public void clear() {
        data = null;
    }

    /**
     *
     * @return
     */
    public int getRowsDim() {
        return data.getDim();
    }

    public int getColumnsDim(int t) {
        int st = t - start;
        if (st < 0) {
            return 0;
        } else {
            return cdims[st + 1] - cdims[st];
        }
    }

    /**
     *
     * @param dim
     * @param cmax
     * @param n
     */
    public void prepare(final int dim, final int cmax, final int n) {
        clear();
        data = new DataBlockStorage(dim, cmax * (n - start));
        cdims = new int[n - start + 1];
    }

    /**
     *
     * @param t
     * @return
     */
    public Matrix matrix(final int t) {
        int st = t - start;
        if (st < 0) {
            return null;
        } else {
            int c0 = cdims[st], c1 = cdims[st + 1];
            return data.matrix(c0, c1);
        }
    }

    public void save(final int t, final Matrix P) {
        int st = t - start;
        if (st < 0) {
            return;
        }
        int icur = cdims[st];
        redimc(st + 2);
        int capacity = data.getCapacity();
        int ncapacity = icur + P.getColumnsCount();
        if (capacity <= ncapacity) {
            data.resize(ncapacity << 1);
        }
        DataBlockIterator cols = P.columnsIterator();
        while (cols.hasNext()) {
            data.save(icur++, cols.next());
        }
        cdims[st + 1] = icur;
    }

    /**
     *
     * @return
     */
    public int getStartSaving() {
        return start;
    }

    /**
     *
     * @param p
     */
    public void setStartSaving(int p) {
        start = p;
        data = null;
    }

    private void redimc(int ndim) {
        if (ndim <= cdims.length) {
            return;
        }
        int n = Math.max(cdims.length << 1, DataBlockStorage.calcSize(ndim));
        int[] tmp = new int[n];
        System.arraycopy(cdims, 0, tmp, 0, cdims.length);
        cdims = tmp;
    }

    public void rescale(double factor) {
        data.rescale(factor);
    }

}
