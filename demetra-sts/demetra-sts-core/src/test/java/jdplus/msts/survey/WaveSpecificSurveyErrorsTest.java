/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.msts.survey;

import jdplus.data.DataBlock;
import jdplus.math.matrices.FastMatrix;
import java.util.Random;
import java.util.function.DoubleSupplier;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class WaveSpecificSurveyErrorsTest {

    public WaveSpecificSurveyErrorsTest() {
    }

    @Test
    public void testTx() {
        tx(.4, .3, .3, 8);
    }

    @Test
    public void testTx2() {
        tx(new double[][]{new double[0], new double[]{.3}, new double[]{.2}, new double[]{.4}}, 1);
        tx(new double[][]{new double[0], new double[]{.3}, new double[]{.2}, new double[]{.4}}, 3);
        tx(new double[][]{new double[0], new double[]{.3}, new double[]{.2, .4}, new double[]{.2, .4}, new double[]{.2, .4}}, 1);
        tx(new double[][]{new double[0], new double[]{.3}, new double[]{.2, .4}}, 3);
    }

    @Test
    public void testxT() {
        xt(.4, .3, .3, 8);
    }

    @Test
    public void testTxc() {
        WaveSpecificSurveyErrors.Data data = new WaveSpecificSurveyErrors.Data(.3, .4, .2, 8);
        WaveSpecificSurveyErrors.Dynamics dyn = new WaveSpecificSurveyErrors.Dynamics(data);
        WaveSpecificSurveyErrors.Initialization init = new WaveSpecificSurveyErrors.Initialization(data);
        double[][] ar = new double[8][];
        ar[0] = new double[0];
        ar[1] = new double[]{.3};
        double[] car = new double[]{.4, .2};
        for (int i = 2; i < 8; ++i) {
            ar[i] = car;
        }
        WaveSpecificSurveyErrors.Data2 data2 = new WaveSpecificSurveyErrors.Data2(ar, 1);
        WaveSpecificSurveyErrors.Dynamics2 dyn2 = new WaveSpecificSurveyErrors.Dynamics2(data2);
        int dim = init.getStateDim();

        DataBlock x = DataBlock.make(dim);
        Random rnd = new Random(0);
        x.set((DoubleSupplier)rnd::nextDouble);
        DataBlock y = x.deepClone();
        dyn.TX(0, x);
        dyn2.TX(0, y);
        assertTrue(y.distance(x) < 1e-9);
        dyn.XT(0, x);
        dyn2.XT(0, y);
        assertTrue(y.distance(x) < 1e-9);

    }

    private void tx(double ar11, double ar21, double ar22, int nwaves) {
        WaveSpecificSurveyErrors.Data data = new WaveSpecificSurveyErrors.Data(ar11, ar21, ar22, nwaves);
        WaveSpecificSurveyErrors.Dynamics dyn = new WaveSpecificSurveyErrors.Dynamics(data);
        WaveSpecificSurveyErrors.Initialization init = new WaveSpecificSurveyErrors.Initialization(data);
        int dim = init.getStateDim();
        FastMatrix T = FastMatrix.square(dim);
        dyn.T(0, T);

        DataBlock x = DataBlock.make(dim);
        Random rnd = new Random(0);
        x.set((DoubleSupplier)rnd::nextDouble);
        DataBlock y = DataBlock.make(dim);
        y.product(T.rowsIterator(), x);
        dyn.TX(0, x);
        assertTrue(y.distance(x) < 1e-9);
    }

    private void xt(double ar11, double ar21, double ar22, int nwaves) {
        WaveSpecificSurveyErrors.Data data = new WaveSpecificSurveyErrors.Data(ar11, ar21, ar22, nwaves);
        WaveSpecificSurveyErrors.Dynamics dyn = new WaveSpecificSurveyErrors.Dynamics(data);
        WaveSpecificSurveyErrors.Initialization init = new WaveSpecificSurveyErrors.Initialization(data);
        int dim = init.getStateDim();
        FastMatrix T = FastMatrix.square(dim);
        dyn.T(0, T);

        DataBlock x = DataBlock.make(dim);
        Random rnd = new Random(0);
        x.set((DoubleSupplier)rnd::nextDouble);
        DataBlock y = DataBlock.make(dim);
        y.product(T.columnsIterator(), x);
        dyn.XT(0, x);
        assertTrue(y.distance(x) < 1e-9);
    }

    private void tx(double[][] ar, int lag) {
        WaveSpecificSurveyErrors.Data2 data = new WaveSpecificSurveyErrors.Data2(ar, lag);
        WaveSpecificSurveyErrors.Dynamics2 dyn = new WaveSpecificSurveyErrors.Dynamics2(data);
        WaveSpecificSurveyErrors.Initialization2 init = new WaveSpecificSurveyErrors.Initialization2(data);
        int dim = init.getStateDim();
        FastMatrix T = FastMatrix.square(dim);
        dyn.T(0, T);
//        System.out.println();
//        System.out.println(T);

        DataBlock x = DataBlock.make(dim);
        Random rnd = new Random(0);
        x.set((DoubleSupplier)rnd::nextDouble);
        DataBlock y = DataBlock.make(dim);
        y.product(T.rowsIterator(), x);
        dyn.TX(0, x);
        assertTrue(y.distance(x) < 1e-9);
        
        FastMatrix Q = FastMatrix.square(dim);
        init.Pf0(Q);
//        System.out.println();
//        System.out.println(Q);
    }

    private void xt(double[][] ar, int lag) {
        WaveSpecificSurveyErrors.Data2 data = new WaveSpecificSurveyErrors.Data2(ar, lag);
        WaveSpecificSurveyErrors.Dynamics2 dyn = new WaveSpecificSurveyErrors.Dynamics2(data);
        WaveSpecificSurveyErrors.Initialization2 init = new WaveSpecificSurveyErrors.Initialization2(data);
        int dim = init.getStateDim();
        FastMatrix T = FastMatrix.square(dim);
        dyn.T(0, T);

        DataBlock x = DataBlock.make(dim);
        Random rnd = new Random(0);
        x.set((DoubleSupplier)rnd::nextDouble);
        DataBlock y = DataBlock.make(dim);
        y.product(T.columnsIterator(), x);
        dyn.XT(0, x);
        assertTrue(y.distance(x) < 1e-9);
    }

    @Test
    public void testxT2() {
        xt(new double[][]{new double[0], new double[]{.3}, new double[]{2}, new double[]{.4}}, 1);
        xt(new double[][]{new double[0], new double[]{.3}, new double[]{2}, new double[]{.4}}, 3);
        xt(new double[][]{new double[0], new double[]{.3}, new double[]{.2, .4}}, 1);
        xt(new double[][]{new double[0], new double[]{.3}, new double[]{.2, .4}}, 3);
    }

    //@Test
    public void stressTest() {
        double[][] ar = new double[][]{new double[0], new double[]{.3}, new double[]{2}, new double[]{.4}};
        WaveSpecificSurveyErrors.Data2 data = new WaveSpecificSurveyErrors.Data2(ar, 3);
        WaveSpecificSurveyErrors.Dynamics2 dyn = new WaveSpecificSurveyErrors.Dynamics2(data);
        WaveSpecificSurveyErrors.Initialization2 init = new WaveSpecificSurveyErrors.Initialization2(data);
        int dim = init.getStateDim();
        FastMatrix T = FastMatrix.square(dim);
        dyn.T(0, T);

        DataBlock x = DataBlock.make(dim);
        Random rnd = new Random(0);
        x.set((DoubleSupplier)rnd::nextDouble);
        DataBlock y = DataBlock.make(dim);
        DataBlock z = DataBlock.make(dim);
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 100000000; ++i) {
            z.copy(x);
            dyn.XT(0, z);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < 100000000; ++i) {
            y.product(T.columnsIterator(), x);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }
}
