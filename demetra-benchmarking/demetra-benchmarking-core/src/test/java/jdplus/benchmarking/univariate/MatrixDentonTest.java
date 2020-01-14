/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.benchmarking.univariate;

import demetra.benchmarking.univariate.DentonSpec;
import jdplus.data.DataBlock;
import org.junit.Test;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class MatrixDentonTest {

    @Test
    public void MatrixDentonTest() {
        DataBlock y = DataBlock.make(20);
        y.set(i -> (1 + i));
        DataBlock x = DataBlock.make(80);
        x.set(i -> (1 + i) * (1 + i));

        DentonSpec spec = DentonSpec.builder().build();
        MatrixDenton denton = new MatrixDenton(spec, 4, 0);

        double[] rslt = denton.process(x, y);
//        System.out.println(Matrix.columnOf(DataBlock.ofInternal(rslt)));
    }

}
