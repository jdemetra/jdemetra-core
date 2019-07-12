/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.maths.matrices.decomposition;

import jdplus.data.DataBlockIterator;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.MatrixException;
import jdplus.maths.matrices.MatrixWindow;

/**
 * Transform a given matrix to a similar upper Hessenberg matrix using
 * Householder reflections
 *
 * @author Jean Palate
 */
@lombok.Value
public class UpperHessenberg {

    private final HouseholderReflection[] householderReflections;
    private final CanonicalMatrix H;

    public static UpperHessenberg of(CanonicalMatrix A) {
        if (! A.isSquare())
            throw new MatrixException(MatrixException.SQUARE);
        else 
            return new UpperHessenberg(A);
    }
    
    private UpperHessenberg(CanonicalMatrix A){
        int n = A.getRowsCount();
        H = A.deepClone();
        if (n <= 2) {
            householderReflections = null;
        } else {
            householderReflections = new HouseholderReflection[n - 2];
            MatrixWindow hwnd = H.bottom(n-1);
            for (int i=1; i<=householderReflections.length; ++i){
                DataBlockIterator cols = hwnd.columnsIterator();
                HouseholderReflection hr = HouseholderReflection.of(cols.next(), true);
                while (cols.hasNext())
                    hr.transform(cols.next());
                hwnd.bshrink();
                MatrixWindow mr = H.right(n-i);
                DataBlockIterator rows = mr.rowsIterator();
                while (rows.hasNext())
                    hr.transform(rows.next());
                householderReflections[i-1]=hr;
            }
        }
    }

}
