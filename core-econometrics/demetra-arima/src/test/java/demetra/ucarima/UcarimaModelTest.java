/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.ucarima;

import demetra.arima.ArimaModel;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
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
        System.out.println(ucmAirline(-.6, -.8));
    }
    

   public static UcarimaModel ucmAirline(double th, double bth) {
       SarimaSpecification spec=new SarimaSpecification(12);
       spec.airline();
       SarimaModel sarima =SarimaModel.builder(spec)
               .theta(1, th)
               .btheta(1, bth)
               .build();
       
        TrendCycleSelector tsel = new TrendCycleSelector();
        SeasonalSelector ssel = new SeasonalSelector(12);

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel);

        UcarimaModel ucm = decomposer.decompose(sarima);
        ucm=ucm.setVarianceMax(-1, false);
        return ucm;
    }
}
