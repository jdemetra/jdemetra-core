/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.filters;

import demetra.data.Data;
import demetra.data.DoubleSeq;
import jdplus.math.linearfilters.IFiniteFilter;
import jdplus.math.matrices.Matrix;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class FilteringTest {

    public FilteringTest() {
    }

    @Test
    public void testSymmetric() {
        LocalPolynomialFilterSpec spec = new LocalPolynomialFilterSpec();
        IFiltering lf = LocalPolynomialFilterFactory.of(spec);

        DoubleSeq s = DoubleSeq.of(Data.NILE);

        DoubleSeq lout = lf.process(s);
        IFiniteFilter cf = lf.centralFilter();

        double[] cw = cf.weightsToArray();
        Matrix M = Matrix.make(cw.length - 1, cf.getUpperBound());
        IFiniteFilter[] af = lf.leftEndPointsFilters();
        for (int i = 0; i < af.length; ++i) {
            M.column(i).drop(0, i).copyFrom(af[i].weightsToArray(), 0);
        }
//        System.out.println(M);
        Matrix N = Matrix.make(cw.length - 1, cf.getUpperBound());
        IFiniteFilter[] bf = lf.rightEndPointsFilters();
        for (int i = 0; i < bf.length; ++i) {
            N.column(i).drop(i, 0).copyFrom(bf[i].weightsToArray(), 0);
        }
        Filtering F = Filtering.of(DoubleSeq.of(cw), M);
        DoubleSeq fout = F.process(s);
        assertTrue(lout.distance(fout) < 1e-9);
        Filtering G = Filtering.of(DoubleSeq.of(cw), M, N);
        DoubleSeq gout = G.process(s);
        assertTrue(lout.distance(gout) < 1e-9);
    }

}
