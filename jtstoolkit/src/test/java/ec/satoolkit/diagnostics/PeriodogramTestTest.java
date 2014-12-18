/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.satoolkit.diagnostics;

import ec.tstoolkit.arima.ArimaModelBuilder;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.stats.StatisticalTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author PCUser
 */
public class PeriodogramTestTest {

    public PeriodogramTestTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of compute method, of class PeriodogramTest.
     */
    //@Test
    public void testCompute() {
        int N = 600, M = 100000;
        ArimaModelBuilder builder = new ArimaModelBuilder();
        double m=0, m2=0;
        for (int i = 0; i < M; ++i) {
            double[] x = builder.generate(builder.createModel(Polynomial.ONE, Polynomial.ONE, 1), N);
            StatisticalTest test = PeriodogramTest.computeSum(new DataBlock(x), 4);
            m+=test.getValue();
            m2+=test.getValue()*test.getValue();
        }
        System.out.println(m/M);
        System.out.println((m2-m*m/M)/M);
    }

}
