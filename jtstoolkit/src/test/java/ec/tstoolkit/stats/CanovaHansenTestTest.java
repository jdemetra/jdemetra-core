/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.stats;

import data.Data;
import data.MatrixReader;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.data.TrigonometricSeries;
import ec.tstoolkit.dstats.Chi2;
import ec.tstoolkit.dstats.F;
import ec.tstoolkit.dstats.ProbabilityType;
import ec.tstoolkit.maths.matrices.LowerTriangularMatrix;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class CanovaHansenTestTest {

    public CanovaHansenTestTest() {
    }

    @Test
    public void testUnempl_dummy() {
//        System.out.println("dummies");
        DataBlock y = new DataBlock(Data.US_UNEMPL);
        CanovaHansenTest ch = new CanovaHansenTest();
        ch.setTruncationLag(4);
        ch.process(y, 4, 0);
//        for (int i = 0; i < 4; ++i) {
//            System.out.println(ch.test(i));
//        }
//        System.out.println(ch.testAll());
    }

    @Test
    public void testUnempl_trig() {
//        System.out.println("trig");
        DataBlock y = new DataBlock(Data.US_UNEMPL);
        CanovaHansenTest ch = new CanovaHansenTest();
        ch.setType(CanovaHansenTest.Variables.Trigonometric);
        ch.setTruncationLag(4);
        ch.process(y, 4, 0);
//        System.out.println(ch.test(0, 2));
//        System.out.println(ch.test(2));
//        System.out.println(ch.testAll());
    }

    @Test
    public void testP_dummy() {
//        System.out.println("dummies");
        DataBlock y = new DataBlock(Data.P.log().delta(1));
        CanovaHansenTest ch = new CanovaHansenTest();
        ch.setLag1(false);
        ch.setTruncationLag(12);
        ch.process(y, 12, 1);
//        for (int i = 0; i < 12; ++i) {
//            System.out.println(ch.test(i));
//        }
//        System.out.println(ch.testAll());
    }

    @Test
    public void testP_trig() {
//        System.out.println("trig");
        DataBlock y = new DataBlock(Data.P.log().delta(1));
        CanovaHansenTest ch = new CanovaHansenTest();
        ch.setLag1(false);
        ch.setTruncationLag(12);
        ch.process(y, 12, 0);
        ch.setType(CanovaHansenTest.Variables.Trigonometric);
//        for (int i = 0; i < 6; ++i) {
//            System.out.println(ch.test(2 * i, 2));
//        }
//        System.out.println(ch.test(10));
//        System.out.println(ch.testAll());
    }

    @Test
    public void testW_trig() throws IOException, URISyntaxException {
        URL resource = CanovaHansenTest.class.getResource("/uspetroleum.txt");
        Matrix pet = MatrixReader.read(new File(resource.toURI()));
        for (int c = 0; c < pet.getColumnsCount(); ++c) {
//            System.out.println("week trig" + (c + 1));
            DataBlock y = pet.column(c).deepClone();
            y.difference();
            y = y.drop(1, 0);
            CanovaHansenTest ch = new CanovaHansenTest();
            ch.setLag1(false);
            ch.setTruncationLag(52);
            ch.setType(CanovaHansenTest.Variables.UserDefined);
            TrigonometricSeries all = TrigonometricSeries.all(365.25 / 7, 6);
            Matrix m = all.matrix(19, y.getLength());
            ch.setX(m);
            ch.process(y);
//            for (int i = 0; i < 6; ++i) {
//                System.out.println(ch.test(2 * i, 2));
//            }
//            System.out.println(ch.testAll());
//            System.out.println(ch.robustTestCoefficients());
        }
    }

    @Test
    @Ignore
    public void testW_random() throws IOException, URISyntaxException {
        double[] t = new double[1000];
        double[] u = new double[1000];
        TrigonometricSeries all = TrigonometricSeries.all(365.25 / 7, 6);
        Matrix m = all.matrix(19, 1302);
        for (int c = 0; c < t.length; ++c) {
            DataBlock y = new DataBlock(m.getRowsCount());
            y.randomize();
            CanovaHansenTest ch = new CanovaHansenTest();
            ch.setLag1(false);
            ch.setTruncationLag(52);
            ch.setType(CanovaHansenTest.Variables.UserDefined);
            ch.setX(m);
            ch.process(y);
            t[c] = ch.robustTestCoefficients();
            u[c] = ch.olsTestCoefficients();
        }
        Arrays.sort(t);
        Arrays.sort(u);
        System.out.println(new ReadDataBlock(t));
        System.out.println(new ReadDataBlock(u));
    }
}
