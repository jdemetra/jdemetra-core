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
package jdplus.maths.matrices;

/**
 * This class corresponds to the Lapack routine DGEBAL. DGEBAL balances a
 * general real matrix A. This involves, first, permuting A by a similarity
 * transformation to isolate eigenvalues in the first 0 to LOW (excluded) and
 * last HIGH to N (excluded) elements on the diagonal and second, applying a
 * diagonal similarity transformation to rows and columns LOW to HIGH (excluded)
 * to make the rows and columns as close in norm as possible. Both steps are
 * optional.
 *
 * Balancing may reduce the 1-norm of the matrix, and improve the accuracy of
 * the computed eigenvalues and/or eigenvectors.
 *
 * @author Jean Palate
 */
public class Balancing {

    private double[] scales;
    private int[] permutations;
    private int low, high;

    private static final double SCLFAC = 2, FACTOR = .95;

    public void balance(CanonicalMatrix M) {
        balance(M, true, true);
    }

    public void balance(CanonicalMatrix M, boolean permute, boolean scale) {
        int n = M.getColumnsCount(), m = M.getRowsCount();
        if (n != m) {
            throw new MatrixException(MatrixException.DIM);
        }
        // Initialization
        low=0; high=n;
        scales = new double[n];
        permutations=new int[n];
        for (int i = 0; i < n; ++i) {
            scales[i] = 1;
            permutations[i]=i;
        }
        if (permute){
            // push rows down;
            
            for (int row=m-1; row >=0; --row ){
                
            }
        }
        
    }

    public double[] getScales() {
        return scales;
    }

    public int[] getPermutation() {
        return permutations;
    }

    public int getLow() {
        return low;
    }

    public int getHigh() {
        return high;
    }
}
