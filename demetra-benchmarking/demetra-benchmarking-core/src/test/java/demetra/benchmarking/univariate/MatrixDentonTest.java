/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.benchmarking.univariate;

import demetra.benchmarking.univariate.DentonSpec;
import jd.data.DataBlock;
import org.junit.Test;
import jd.maths.matrices.FastMatrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class MatrixDentonTest {

    public MatrixDentonTest() {
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
