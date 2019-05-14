/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demetra.data;

//import ec.tstoolkit.maths.matrices.*;
import demetra.design.Development;
import demetra.maths.matrices.SubMatrix;
import java.util.Arrays;
import demetra.maths.matrices.Matrix;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class DataBlockStorage {

    /**
     *
     * @param capacity
     * @return
     */
    public static int calcSize(final int capacity) {
        return (1 + (capacity - 1) / ATOM) * ATOM;
    }

    /**
     * Storage copyOf the bolcks
     */
    private double[] storage;

    /**
     * Number of blocks in storage and number of actually used blocks
     */
    private int n,

    /**
     * Number copyOf blocks in storage and number copyOf actually used blocks
     */

    /**
     * Number ofInternal blocks in storage and number ofInternal actually used blocks
     */

    /**
     * Number ofInternal blocks in storage and number ofInternal actually used blocks
     */

    /**
     * Number ofInternal blocks in storage and number ofInternal actually used blocks
     */
    nused;

    /**
     * Size copyOf a block
     */
    private final int dim;

    private final static int ATOM = 16;

    /**
     *
     * @param dim
     * @param capacity
     */
    public DataBlockStorage(final int dim, final int capacity) {
        n = calcSize(capacity);
        this.dim = dim;
        storage = new double[this.dim * n];
    }

    /**
     * Returns the block at the given position
     * @param pos The position copyOf the block
     * @return
     */
    public DataBlock block(final int pos) {
        int start = dim * pos;
        return DataBlock.of(storage, start, start + dim, 1);
    }

    /**
     * Returns the capacity copyOf the storage (including possible unused data blocks)
     * @return
     */
    public int getCapacity() {
        return n;
    }

    /**
     * Returns the current size copyOf the storage (which is the number copyOf used data blocks)
     * @return
     */
    public int getCurrentSize() {
        return nused;
    }

    /**
     *
     * @return
     */
    public int getDim() {
        return dim;
    }

    /**
     *
     * @param index
     * @return
     */
    public DataBlock item(final int index) {
        return DataBlock.of(storage, index, index + dim * nused, dim);
    }

    /**
     *
     * @param ncapacity
     */
    public void resize(final int ncapacity) {
        int n = calcSize(ncapacity);
        if (n <= this.n) {
            return;
        }
        double[] data = new double[dim * n];
        System.arraycopy(storage, 0, data, 0, storage.length);
        storage = data;
        this.n = n;
    }

    /**
     *
     * @param ncapacity
     * @param value
     */
    public void resize(final int ncapacity, final double value) {
        int n = calcSize(ncapacity);
        if (n <= this.n) {
            return;
        }
        double[] data = new double[dim * n];
        Arrays.fill(data, storage.length, data.length, value);
        System.arraycopy(storage, 0, data, 0, storage.length);
        storage = data;
        this.n = n;
    }
    /**
     *
     * @param pos
     * @param rc
     */
    public void save(final int pos, final DataBlock rc) {
        rc.copyTo(storage, pos * dim);
        if (pos >= nused) {
            nused = pos + 1;
        }
    }

    /**
     *
     * @param pos
     * @param v
     */
    public void save(final int pos, final double[] v) {
        System.arraycopy(v, 0, storage, pos * dim, dim);
        if (pos >= nused) {
            nused = pos + 1;
        }
    }

    /**
     *
     * @param start
     * @param end
     * @return
     */
    public DataBlock storage(int start, int end) {
        int p0 = dim * start, p1 = dim * end;
        return DataBlock.of(storage, p0, p1, 1);

    }

    public double[] storage() {
        return storage;
    }

    /**
     * Gets a matrix representation of the saved data blocks, from the position reader (included)
 to the position end (excluded). 
     * The successive data blocks are stored in the columns copyOf the sub-matrix
     * @param start First position (included)
     * @param end Last position (excluded)
     * @return
     */
    public SubMatrix matrix(final int start, final int end) {
        return SubMatrix.builder(storage).nrows(dim).ncolumns(end-start).start(dim*start).columnIncrement(dim).build();
    }
    
    /**
     * Gets a matrix representation copyOf all the saved data blocks. 
     * The successive data blocks are stored in the columns copyOf the sub-matrix
     * @return
     */
    public SubMatrix matrix() {
        return SubMatrix.builder(storage).nrows(dim).ncolumns(nused).columnIncrement(dim).build();
    }

    /**
     * Multiplies the current data blocks by a given factor
     * @param factor 
     */
    public void rescale(double factor) {
        if (factor == 1)
            return;
        int n=dim*nused;
        for (int i=0; i<n; ++i){
            storage[i]*=factor;
        }
    }
}
