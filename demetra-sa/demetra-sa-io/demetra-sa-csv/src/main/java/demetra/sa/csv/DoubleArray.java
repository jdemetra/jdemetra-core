/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demetra.sa.csv;

import demetra.data.DoubleSeq;
import demetra.math.matrices.Matrix;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
public class DoubleArray {
    
    private static final DoubleArray EMPTY=new DoubleArray(new int[0], DoubleSeq.EMPTYARRAY);
    
    @lombok.NonNull
    int[] dimensions;
    @lombok.NonNull
    double[] data;
    
    public boolean isEmpty(){
        return dimensions.length == 0;
    }
    
    public static DoubleArray of(DoubleSeq d){
        if (d == null || d.isEmpty())
            return empty();
        return new DoubleArray(new int[]{d.length()}, d.toArray());
    }
    
   public static DoubleArray of(double[] d){
        if (d == null || d.length == 0)
            return empty();
        return new DoubleArray(new int[]{d.length}, d);
    }

   public static DoubleArray of(Matrix m){
        if (m == null || m.isEmpty())
            return empty();
        return new DoubleArray(new int[]{m.getRowsCount(), m.getColumnsCount()}, m.toArray());
    }

    public static DoubleArray empty(){
        return EMPTY;
    }

}
