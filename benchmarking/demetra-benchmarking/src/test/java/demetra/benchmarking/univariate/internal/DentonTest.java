/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.benchmarking.univariate.internal;

import demetra.benchmarking.univariate.DentonSpecification;
import demetra.benchmarking.univariate.internal.Denton;
import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import org.junit.Test;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */

public class DentonTest {
    
    public DentonTest() {
        DataBlock y=DataBlock.make(20);
        y.set(i->(1+i));
        DataBlock x=DataBlock.make(80);
        x.set(i->(1+i)*(1+i));
        
        DentonSpecification spec=new DentonSpecification();
        Denton denton=new Denton(spec, 4, 0);
        
        double[] rslt = denton.process(x, y);
        System.out.println(Matrix.columnOf(DataBlock.ofInternal(rslt)));
    }

}
