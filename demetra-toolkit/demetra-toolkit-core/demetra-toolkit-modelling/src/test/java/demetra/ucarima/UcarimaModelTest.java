/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.ucarima;

import demetra.arima.ArimaModel;
import demetra.sarima.SarimaModel;
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
        assertTrue(ucmAirline(-.6, -.8).isValid());
    }

    @Test
    public void testAirline2() {
        for (int period = 3; period <= 300; ++period) {
            UcarimaModel ucm = ucmAirline(period, -.7, -.2);
            System.out.print(ucm.getComponent(0).getInnovationVariance());
            System.out.print('\t');
            System.out.print(ucm.getComponent(1).getInnovationVariance());
            System.out.print('\t');
            System.out.println(ucm.getComponent(3).getInnovationVariance());
            assertTrue(ucm.isValid());
        }
    }

    @Test
    public void test3111() {
        assertTrue(ucm3111(new double[]{.2, -.5, .1}, -.6, -.8).isValid());
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
