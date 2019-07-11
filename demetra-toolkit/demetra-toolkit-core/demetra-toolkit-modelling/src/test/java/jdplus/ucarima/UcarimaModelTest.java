/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.ucarima;

import jdplus.sarima.SarimaModel;
import demetra.arima.SarimaSpecification;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class UcarimaModelTest {

    public UcarimaModelTest() {
    }

    @Test
    public void testAirline() {
        assertTrue(ucmAirline(-.6, -.6).isValid());
    }

    public static void testAirline2(double th, double bth) {
        for (int period = 950; period <= 1000; period+=5) {
            System.out.print(period);
            System.out.print('\t');
            UcarimaModel ucm = ucmAirline(period, th, bth);
            System.out.print(ucm.getComponent(0).getInnovationVariance());
            System.out.print('\t');
            System.out.print(ucm.getComponent(1).getInnovationVariance());
            System.out.print('\t');
            System.out.println(ucm.getComponent(3).getInnovationVariance());
        }
    }

    public static void main(String[] args) {
        long t0=System.currentTimeMillis();
//        for (double th = -.2; th >= -.9; th -= .1) {
            double th=-.6;
            for (double bth = -.2; bth >= -.2; bth -= .05) {
                StringBuilder builder=new StringBuilder();
                builder.append("th=").append(th).append(", bth=").append(bth);
                System.out.println(builder.toString());
                testAirline2(th, bth);
                System.out.println();
            }
//         }
        long t1=System.currentTimeMillis();
        System.out.println();
        System.out.println("Processing time");
        System.out.println(t1-t0);
    }

    @Test
    public void test3111() {
        assertTrue(ucm3111(new double[]{.2, -.5, .1}, -.6, -.8).isValid());
    }

    @Test
    public void testHighFreq() {
            UcarimaModel ucm = ucmAirline(501, -.8, -.9);
            System.out.print(ucm.getComponent(0).getInnovationVariance());
            System.out.print('\t');
            System.out.print(ucm.getComponent(1).getInnovationVariance());
            System.out.print('\t');
            System.out.println(ucm.getComponent(3).getInnovationVariance());
        
    }

    public static UcarimaModel ucmAirline(double th, double bth) {
        return ucmAirline(12, th, bth);
    }

    public static UcarimaModel ucmAirline(int period, double th, double bth) {
        SarimaSpecification spec = new SarimaSpecification(period);
        spec.airline(true);
        SarimaModel sarima = SarimaModel.builder(spec)
                .theta(1, th)
                .btheta(1, bth)
                .build();

        TrendCycleSelector tsel = new TrendCycleSelector();
        SeasonalSelector ssel = new SeasonalSelector(period);

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel);

        UcarimaModel ucm = decomposer.decompose(sarima);
        ucm = ucm.setVarianceMax(-1, false);
        return ucm;
    }

    public static UcarimaModel ucm3111(double[] phi, double th, double bth) {
        SarimaSpecification spec = new SarimaSpecification(12);
        spec.airline(true);
        SarimaModel sarima = SarimaModel.builder(spec)
                .phi(phi)
                .theta(1, th)
                .btheta(1, bth)
                .build();

        TrendCycleSelector tsel = new TrendCycleSelector();
        SeasonalSelector ssel = new SeasonalSelector(12);

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel);

        UcarimaModel ucm = decomposer.decompose(sarima);
        ucm = ucm.setVarianceMax(-1, false);
        return ucm;
    }

}
