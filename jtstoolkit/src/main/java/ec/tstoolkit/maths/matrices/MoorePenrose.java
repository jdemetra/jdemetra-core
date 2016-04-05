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

package ec.tstoolkit.maths.matrices;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.design.Development;

/**
 * Moore-Penrose generalised inverse
 * Given a Matrix G, the generalised inverse G+ of G
 * is the unique matrix such that
 * G G+ G = G
 * G+ G G+ = G+
 * (G G+)' = G G+
 * (G+ G)' = G+ G
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class MoorePenrose implements IGeneralizedInverse{

    /**
     * Computes the Moore-Penrose following an algorithm proposed by Pierre Courrieu
     * "Fast computation of Moore-Penrose Inverse Matrices"
     * Neural information processing, Letters and Reviews, vol. 8, n°2 August 2005
     * @param G The matrix being inverted
     * @return The generalised inverse.
     */
    @Override
    public Matrix inverse(Matrix G) {
        int m = G.getRowsCount(), n = G.getColumnsCount();
        // computes g'g. g'g is n x n
        Matrix S = SymmetricMatrix.XtX(G);
        SymmetricMatrix.lcholesky(S, zero);
        // removes 0 columns:
        DataBlock d = S.diagonal();
        int r = 0;
        for (int i = 0; i < n; ++i) {
            if (d.get(i) != 0) {
                ++r;
            }
        }
        Matrix L = new Matrix(n, r);
        DataBlockIterator scols = S.columns(), lcols = L.columns();
        DataBlock scol = scols.getData(), lcol = lcols.getData();
        do {
            if (d.get(scols.getPosition()) != 0) {
                lcol.copy(scol);
                lcols.next();
            }
        } while (scols.next());

        Matrix K = SymmetricMatrix.XtX(L);
        SymmetricMatrix.lcholesky(K);
        // A =L(LL')^-1 or A * (KK') = L 
        // A*K) * K' = L  or B * K' = L
        // K * B' = L' or B' = rsolbve(K, L')
        LowerTriangularMatrix.rsolve(K, L.all().transpose());
        // L' contains B' or L contains B
        // A * K = B or A= lsolve B
        LowerTriangularMatrix.lsolve(K, L.all());
        // L contains now A

        Matrix I = new Matrix(n, m);
        I.all().product(SymmetricMatrix.XXt(L).all(), G.all().transpose());
        I.clean(zero);
        return I;
    }
    
    public void setZero(double val){
        zero=val;
    }
    
    public double getZero(){
        return zero;
    }
    
    private double zero = ZERO;
    
    
    private static final double ZERO = 1e-12;
}
