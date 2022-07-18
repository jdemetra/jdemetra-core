/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.businesscycle;

import demetra.arima.SarimaOrders;
import demetra.data.Data;
import demetra.data.DoubleSeq;
import jdplus.data.DataBlockStorage;
import jdplus.sarima.SarimaModel;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.composite.CompositeSsf;
import jdplus.ssf.univariate.SsfData;
import jdplus.ucarima.ModelDecomposer;
import jdplus.ucarima.SeasonalSelector;
import jdplus.ucarima.TrendCycleSelector;
import jdplus.ucarima.UcarimaModel;
import jdplus.ssf.arima.SsfUcarima;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author palatej
 */
public class ModifiedHodrickPrescottFilterTest {
    
    public ModifiedHodrickPrescottFilterTest() {
    }

    @Test
    public void testMonthly() {
        UcarimaModel ucmAirline = ucmAirline(12, -.6, -.8);
        CompositeSsf ssf = SsfUcarima.of(ucmAirline);
        DoubleSeq y=DoubleSeq.of(Data.PROD);
        DoubleSeq ly=y.fn(x->Math.log(x));
        DataBlockStorage all = DkToolkit.fastSmooth(ssf, new SsfData(ly));
        ModifiedHodrickPrescottFilter mhp=new ModifiedHodrickPrescottFilter(150000);
        DoubleSeq[] tc = mhp.process(ucmAirline.getComponent(0), all.item(0));
        HodrickPrescottFilter hp=new HodrickPrescottFilter(150000);
        DoubleSeq[] tc2 = hp.process(all.item(0));
//        System.out.println(all.item(0));
//        System.out.println(tc[0]);
//        System.out.println(tc2[0]);
//        System.out.println(tc[1]);
//        System.out.println(tc2[1]);

        assertTrue(all.item(0).distance(tc[0].op(tc[1], (a,b)->a+b))<1e-9);
        assertTrue(all.item(0).distance(tc2[0].op(tc2[1], (a,b)->a+b))<1e-9);
    }
    
    public static UcarimaModel ucmAirline(int period, double th, double bth) {
        SarimaOrders spec=SarimaOrders.airline(period);
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
}
