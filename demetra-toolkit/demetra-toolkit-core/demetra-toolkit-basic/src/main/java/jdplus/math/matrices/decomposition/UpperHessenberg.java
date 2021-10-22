/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.decomposition;

import jdplus.data.DataBlockIterator;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.MatrixException;
import jdplus.math.matrices.MatrixWindow;

/**
 * Transform a given matrix to a similar upper Hessenberg matrix using
 * Householder reflections
 *
 * @author Jean Palate
 */
@lombok.Value
public class UpperHessenberg {

    private final HouseholderReflection[] householderReflections;
    private final FastMatrix H;

    public static UpperHessenberg of(FastMatrix A) {
        if (! A.isSquare())
            throw new MatrixException(MatrixException.SQUARE);
        else 
            return new UpperHessenberg(A);
    }
    
    private UpperHessenberg(FastMatrix A){
        int n = A.getRowsCount();
        H = A.deepClone();
        if (n <= 2) {
            householderReflections = null;
        } else {
            householderReflections = new HouseholderReflection[n - 2];
            MatrixWindow wnd = H.all(), rwnd=H.all();
            for (int i=1; i<=householderReflections.length; ++i){
                DataBlockIterator cols = wnd.bvshrink().columnsIterator();
                HouseholderReflection hr = HouseholderReflection.of(cols.next(), true);
                while (cols.hasNext())
                    hr.transform(cols.next());
                wnd.bhshrink();
                 DataBlockIterator rows = rwnd.bhshrink().rowsIterator();
                while (rows.hasNext())
                    hr.transform(rows.next());
                householderReflections[i-1]=hr;
            }
        }
    }

}
