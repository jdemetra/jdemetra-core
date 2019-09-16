/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.maths.linearfilters;

import demetra.data.Data;
import demetra.data.DoubleSeq;
import java.util.function.DoubleUnaryOperator;
import jdplus.data.analysis.DiscreteKernel;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class AsymmetricFiltersTest {

    public AsymmetricFiltersTest() {
    }
    
    @Test
    public void testMusgrave(){
        int h=6;
        SymmetricFilter lp = LocalPolynomialFilters.ofDefault(h, 2, DiscreteKernel.henderson(h));
        double[] c=new double[]{2/Math.sqrt(Math.PI)/3.5};
        IFiniteFilter f1 = AsymmetricFilters.mmsreFilter(lp, 0, 0, c, null);
        IFiniteFilter f2 = AsymmetricFilters.musgraveFilter(lp, 0, 3.5);
        assertTrue(DoubleSeq.of(f1.weightsToArray()).distance(DoubleSeq.of(f2.weightsToArray()))<1e-9);
    }

    public static void main(String[] arg) {
        int h = 6;
        double tw=5;
        double[] c=new double[]{1};
//        double[] c=new double[]{2/Math.sqrt(Math.PI)/3.5};
        DoubleSeq input = DoubleSeq.of(Data.NILE);
        SymmetricFilter lp = LocalPolynomialFilters.ofDefault(h, 2, DiscreteKernel.henderson(h));
        IFiniteFilter[] f1 = AsymmetricFilters.musgraveFilters(lp, 3.5);
        IFiniteFilter[] f2 = AsymmetricFilters.mmsreFilters(lp, 0, null, null);
        IFiniteFilter[] f3 = AsymmetricFilters.mmsreFilters(lp, 0, null, null, Math.PI/8, tw);
        for (int i = 0; i < h; ++i) {
//            System.out.println(DoubleSeq.of(f.weightsToArray()));
            DoubleUnaryOperator p = f1[i].gainFunction();
            double[] pw = new double[100];
            for (int k = 0; k < 100; ++k) {
                pw[k] = p.applyAsDouble(k * Math.PI / 100);
            }
            System.out.println(DoubleSeq.of(pw));
        }
        for (int i = 0; i < h; ++i) {
//            System.out.println(DoubleSeq.of(f.weightsToArray()));
            DoubleUnaryOperator p = f2[i].gainFunction();
            double[] pw = new double[100];
            for (int k = 0; k < 100; ++k) {
                pw[k] = p.applyAsDouble(k * Math.PI / 100);
            }
            System.out.println(DoubleSeq.of(pw));
        }
        for (int i = 0; i < h; ++i) {
//            System.out.println(DoubleSeq.of(f.weightsToArray()));
            DoubleUnaryOperator p = f3[i].gainFunction();
            double[] pw = new double[100];
            for (int k = 0; k < 100; ++k) {
                pw[k] = p.applyAsDouble(k * Math.PI / 100);
            }
            System.out.println(DoubleSeq.of(pw));
        }
        System.out.println();
        for (int i = 0; i < h; ++i) {
//            System.out.println(DoubleSeq.of(f.weightsToArray()));
            DoubleUnaryOperator p = f1[i].phaseFunction();
            double[] pw = new double[100];
            for (int k = 0; k < 100; ++k) {
                pw[k] = p.applyAsDouble(k * Math.PI / 800);
            }
            System.out.println(DoubleSeq.of(pw));
        }
        for (int i = 0; i < h; ++i) {
//            System.out.println(DoubleSeq.of(f.weightsToArray()));
            DoubleUnaryOperator p = f2[i].phaseFunction();
            double[] pw = new double[100];
            for (int k = 0; k < 100; ++k) {
                pw[k] = p.applyAsDouble(k * Math.PI / 800);
            }
            System.out.println(DoubleSeq.of(pw));
        }
        for (int i = 0; i < h; ++i) {
//            System.out.println(DoubleSeq.of(f.weightsToArray()));
            DoubleUnaryOperator p = f3[i].phaseFunction();
            double[] pw = new double[100];
            for (int k = 0; k < 100; ++k) {
                pw[k] = p.applyAsDouble(k * Math.PI / 800);
            }
            System.out.println(DoubleSeq.of(pw));
        }
        System.out.println();
        for (int i = 0; i < h; ++i) {
            System.out.println(DoubleSeq.of(f1[i].weightsToArray()));
        }
        for (int i = 0; i < h; ++i) {
            IFiniteFilter f = AsymmetricFilters.mmsreFilter(lp, i, 0, new double[]{}, null, Math.PI / 8, 10);
            System.out.println(DoubleSeq.of(f2[i].weightsToArray()));
        }
        for (int i = 0; i < h; ++i) {
            IFiniteFilter f = AsymmetricFilters.mmsreFilter(lp, i, 0, new double[]{}, null, Math.PI / 8, 10);
            System.out.println(DoubleSeq.of(f3[i].weightsToArray()));
        }
        System.out.println();

        int n = input.length();
        DoubleSeq e = input.range(n-h-1, n);
        double[] f = AsymmetricFilters.implicitForecasts(lp, f1, e);
        double[] all=new double[2*h+1];
        e.copyTo(all, 0);
        System.arraycopy(f, 0, all, h+1, h);
        System.out.println(DoubleSeq.of(all));
        f = AsymmetricFilters.implicitForecasts(lp, f2, e);
        System.arraycopy(f, 0, all, h+1, h);
        System.out.println(DoubleSeq.of(all));
        f = AsymmetricFilters.implicitForecasts(lp, f3, e);
        System.arraycopy(f, 0, all, h+1, h);
        System.out.println(DoubleSeq.of(all));
    }
}
